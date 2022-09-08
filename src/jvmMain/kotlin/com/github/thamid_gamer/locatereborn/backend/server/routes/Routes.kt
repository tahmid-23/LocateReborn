package com.github.thamid_gamer.locatereborn.backend.server.routes

import com.github.thamid_gamer.locatereborn.backend.db.tables.*
import com.github.thamid_gamer.locatereborn.backend.server.session.LocateSession
import com.github.thamid_gamer.locatereborn.shared.api.data.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Route.studentListRoute(db: Database) {
    get("/students") {
        val session = call.sessions.get<LocateSession>()

        if (session == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        newSuspendedTransaction(Dispatchers.IO, db) {
            val response = Student
                .selectAll()
                .map {
                    StudentData(
                        it[Student.studentId],
                        it[Student.firstName],
                        it[Student.lastName],
                        it[Student.isTeacher],
                        StudentType.studentTypeMap[it[Student.studentType]] ?: StudentType.UNKNOWN,
                        it[Student.roomNumber]
                    )
                }

            call.respond(response)
        }
    }
}

fun Route.studentCoursesRoute(db: Database) {
    get("/student-courses") {
        val session = call.sessions.get<LocateSession>()

        if (session == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val id = call.request.queryParameters["id"]
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        newSuspendedTransaction(Dispatchers.IO, db) {
            val studentResponse = Student
                .select(Student.studentId eq id)
                .map {
                    StudentData(
                        it[Student.studentId],
                        it[Student.firstName],
                        it[Student.lastName],
                        it[Student.isTeacher],
                        StudentType.studentTypeMap[it[Student.studentType]] ?: StudentType.UNKNOWN,
                        it[Student.roomNumber]
                    )
                }

            if (studentResponse.size > 1) {
                call.respond(HttpStatusCode.InternalServerError)
            }

            val student = studentResponse.firstOrNull()
            if (student == null) {
                call.respond(HttpStatusCode.InternalServerError)
                return@newSuspendedTransaction
            }

            val courseResponse = DayGroupStudent
                .join(DayGroup, JoinType.INNER) {
                    DayGroupStudent.groupId eq DayGroup.groupId
                }
                .join(Student, JoinType.INNER) {
                    DayGroupStudent.studentId eq Student.studentId
                }
                .join(Course, JoinType.INNER) {
                    DayGroup.schoologyCourseId eq Course.schoologyCourseId
                }
                .select(Student.studentId eq id)
                .map {
                    val courseData = CourseData(
                        it[Course.schoologyCourseId],
                        it[Course.fullCourseName],
                        it[Course.simpleCourseName],
                        CourseType.courseTypeMap[it[Course.courseType]] ?: CourseType.UNKNOWN,
                    )
                    val dayGroupData = DayGroupData(it[Course.schoologyCourseId], it[DayGroup.period],
                        Day.join(DayGroup, JoinType.INNER) {
                            Day.groupId eq DayGroup.groupId
                        }.select(DayGroup.groupId eq it[DayGroupStudent.groupId]).map { row ->
                        row[Day.day]
                    })
                    Pair(courseData, Pair(it[DayGroupStudent.groupId], dayGroupData))
                }

            call.respond(Pair(student, courseResponse))
        }
    }
}

fun Route.courseListRoute(db: Database) {
    get("/courses") {
        val session = call.sessions.get<LocateSession>()

        if (session == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        newSuspendedTransaction(Dispatchers.IO, db) {
            val dayResult = Day.join(DayGroup, JoinType.INNER) {
                Day.groupId eq DayGroup.groupId
            }.selectAll()
            val dayMap = buildMap<Int, Pair<Int, MutableCollection<Int>>> {
                for (row in dayResult) {
                    getOrPut(row[DayGroup.groupId]) {
                        Pair(row[DayGroup.period], mutableListOf())
                    }.second.add(row[Day.day])
                }
            }
            
            val response = DayGroup.join(Course, JoinType.INNER) {
                DayGroup.schoologyCourseId eq Course.schoologyCourseId
            }.selectAll().groupBy({ CourseData(
                it[Course.schoologyCourseId],
                it[Course.fullCourseName],
                it[Course.simpleCourseName],
                CourseType.courseTypeMap[it[Course.courseType]] ?: CourseType.UNKNOWN
            ) }) {
                it[DayGroup.groupId]
            }.mapValues {
                buildList {
                    for (groupId in it.value) {
                        val dayInfo = dayMap[groupId]
                        if (dayInfo != null) {
                            add(Pair(groupId, DayGroupData(it.key.schoologyCourseId, dayInfo.first, dayInfo.second)))
                        }
                    }
                }
            }

            call.respond(response)
        }
    }
}

fun Route.courseStudentsRoute(db: Database) {
    get("/course-students") {
        val session = call.sessions.get<LocateSession>()

        if (session == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val groupId = call.request.queryParameters["groupId"]?.toIntOrNull()

        if (groupId == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        newSuspendedTransaction(Dispatchers.IO, db) {
            val courseResponse = DayGroup
                .join(Course, JoinType.INNER) {
                    DayGroup.schoologyCourseId eq Course.schoologyCourseId
                }
                .select(DayGroup.groupId eq groupId)
                .map {
                    CourseData(
                        it[Course.schoologyCourseId],
                        it[Course.fullCourseName],
                        it[Course.simpleCourseName],
                        CourseType.courseTypeMap[it[Course.courseType]] ?: CourseType.UNKNOWN
                    )
                }
                .firstOrNull()
            if (courseResponse == null) {
                call.respond(HttpStatusCode.InternalServerError)
                return@newSuspendedTransaction
            }

            val studentResponse = DayGroupStudent
                .join(Student, JoinType.INNER) {
                    DayGroupStudent.studentId eq Student.studentId
                }
                .select((DayGroupStudent.groupId eq groupId))
                .map {
                    StudentData(
                        it[Student.studentId],
                        it[Student.firstName],
                        it[Student.lastName],
                        it[Student.isTeacher],
                        StudentType.studentTypeMap[it[Student.studentType]] ?: StudentType.UNKNOWN,
                        it[Student.roomNumber]
                    )
                }
                .distinct()

            call.respond(Pair(courseResponse, studentResponse))
        }
    }
}