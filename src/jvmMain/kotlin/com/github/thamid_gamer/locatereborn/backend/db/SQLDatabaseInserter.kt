package com.github.thamid_gamer.locatereborn.backend.db

import com.github.thamid_gamer.locatereborn.backend.datagen.generator.DataGenerationResult
import com.github.thamid_gamer.locatereborn.backend.db.tables.Course
import com.github.thamid_gamer.locatereborn.backend.db.tables.StudentPeriod
import com.github.thamid_gamer.locatereborn.backend.db.tables.Student
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class SQLDatabaseInserter(private val db: Database) : DatabaseInserter {

    override suspend fun updateData(dataGenerationResult: DataGenerationResult) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            SchemaUtils.drop(Student, StudentPeriod, Course)
            SchemaUtils.create(Student, StudentPeriod, Course)

            for (student in dataGenerationResult.studentMap.values) {
                Student.insert {
                    it[studentId] = student.first.studentId
                    it[firstName] = student.first.firstName
                    it[lastName] = student.first.lastName
                    it[isTeacher] = student.first.isTeacher
                    it[studentType] = student.first.studentType.name
                }
                for (periodData in student.second) {
                    StudentPeriod.insert {
                        it[studentId] = periodData.studentId
                        it[schoologyCourseId] = periodData.schoologyCourseId
                        it[period] = periodData.period
                        it[day] = periodData.day
                    }
                }
            }
            for (course in dataGenerationResult.courseMap.values) {
                Course.insert {
                    it[schoologyCourseId] = course.schoologyCourseId
                    it[simpleCourseName] = course.simpleCourseName
                    it[fullCourseName] = course.fullCourseName
                    it[courseType] = course.courseType.name
                }
            }
        }
    }

}