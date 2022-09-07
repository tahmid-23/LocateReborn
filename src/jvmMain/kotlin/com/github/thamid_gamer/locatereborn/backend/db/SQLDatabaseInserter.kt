package com.github.thamid_gamer.locatereborn.backend.db

import com.github.thamid_gamer.locatereborn.backend.datagen.generator.DataGenerationResult
import com.github.thamid_gamer.locatereborn.backend.db.tables.*
import com.github.thamid_gamer.locatereborn.shared.api.data.DayGroupData
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.IdentityHashMap

class SQLDatabaseInserter(private val db: Database) : DatabaseInserter {

    override suspend fun updateData(dataGenerationResult: DataGenerationResult) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            SchemaUtils.drop(Student, Course, DayGroup, Day, DayGroupStudent)
            SchemaUtils.create(Student, Course, DayGroup, Day, DayGroupStudent)

            val dayGroupDataMap = mutableMapOf<DayGroupData, Int>()
            for (course in dataGenerationResult.courseMap.values) {
                Course.insert {
                    it[schoologyCourseId] = course.first.schoologyCourseId
                    it[simpleCourseName] = course.first.simpleCourseName
                    it[fullCourseName] = course.first.fullCourseName
                    it[courseType] = course.first.courseType.name
                }
                for (dayGroupData in course.second) {
                    if (dayGroupData !in dayGroupDataMap) {
                        val dayGroupId = DayGroup.insert {
                            it[schoologyCourseId] = dayGroupData.schoologyCourseId
                            it[period] = dayGroupData.period
                        }[DayGroup.groupId]
                        for (dayIndex in dayGroupData.days) {
                            Day.insert {
                                it[groupId] = dayGroupId
                                it[day] = dayIndex
                            }
                        }
                        dayGroupDataMap[dayGroupData] = dayGroupId
                    }
                }
            }
            for (student in dataGenerationResult.studentMap.values) {
                Student.insert {
                    it[studentId] = student.first.studentId
                    it[firstName] = student.first.firstName
                    it[lastName] = student.first.lastName
                    it[isTeacher] = student.first.isTeacher
                    it[studentType] = student.first.studentType.name
                }
                for (dayGroupData in student.second) {
                    dayGroupDataMap[dayGroupData]?.let { cachedGroupId ->
                        DayGroupStudent.insert {
                            it[studentId] = student.first.studentId
                            it[groupId] = cachedGroupId
                        }
                    }
                }
            }
        }
    }

}