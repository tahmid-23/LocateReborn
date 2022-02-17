package com.github.thamid_gamer.locatereborn.backend.db

import com.github.thamid_gamer.locatereborn.backend.db.tables.StudentPeriods
import com.github.thamid_gamer.locatereborn.backend.db.tables.Students
import com.github.thamid_gamer.locatereborn.backend.datagen.generator.LocateDataGenerator
import com.github.thamid_gamer.locatereborn.backend.datagen.generator.GeneratorRequest
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class SQLDatabaseInserter(private val db: Database,
                          private val dataGenerator: LocateDataGenerator,
                          private val generatorRequest: GeneratorRequest
) {

    fun refreshData() {
        transaction(db) {
            SchemaUtils.drop(Students, StudentPeriods)
            SchemaUtils.create(Students, StudentPeriods)

            runBlocking {
                val data = dataGenerator.generateData(generatorRequest)

                for (student in data.studentMap) {
                    Students.insert {
                        it[studentId] = student.key
                        it[firstName] = student.value.firstName
                        it[lastName] = student.value.lastName
                        it[isTeacher] = student.value.isTeacher
                        it[studentType] = student.value.studentType.name
                    }
                }

                for (studentPeriods in data.studentPeriods) {
                    for (studentPeriod in studentPeriods.value) {
                        StudentPeriods.insert {
                            it[studentId] = studentPeriods.key
                            it[schoologyCourseId] = studentPeriod.schoologyCourseId
                            it[fullCourseName] = studentPeriod.fullCourseName
                            it[simpleCourseName] = studentPeriod.simpleCourseName
                            it[courseType] = studentPeriod.courseType.name
                            it[day] = studentPeriod.day
                            it[period] = studentPeriod.period
                        }
                    }
                }
            }
        }
    }

}