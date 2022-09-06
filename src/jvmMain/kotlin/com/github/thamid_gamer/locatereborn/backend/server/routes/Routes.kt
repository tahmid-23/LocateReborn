package com.github.thamid_gamer.locatereborn.backend.server.routes

import com.github.thamid_gamer.locatereborn.backend.db.tables.Course
import com.github.thamid_gamer.locatereborn.backend.db.tables.StudentPeriod
import com.github.thamid_gamer.locatereborn.backend.db.tables.Student
import com.github.thamid_gamer.locatereborn.backend.server.session.LocateSession
import com.github.thamid_gamer.locatereborn.shared.api.data.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
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

            val periodResponse = StudentPeriod
                .innerJoin(Student)
                .innerJoin(Course)
                .select((StudentPeriod.schoologyCourseId eq Course.schoologyCourseId) and
                        (StudentPeriod.studentId eq Student.studentId) and
                        (Student.studentId eq id))
                .groupBy({ CourseData(it[Course.schoologyCourseId], it[Course.fullCourseName], it[Course.simpleCourseName], CourseType.courseTypeMap[it[Course.courseType]] ?: CourseType.UNKNOWN ) }) {
                    StudentPeriodData(
                        it[Student.studentId],
                        it[Student.firstName],
                        it[StudentPeriod.period],
                        it[StudentPeriod.day]
                    )
                }

            call.respond(Pair(student, periodResponse))
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
            val response = Course
                .selectAll()
                .map {
                    CourseData(
                        it[Course.schoologyCourseId],
                        it[Course.fullCourseName],
                        it[Course.simpleCourseName],
                        CourseType.courseTypeMap[it[Course.courseType]] ?: CourseType.UNKNOWN
                    )
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

        val schoologyCourseId = call.request.queryParameters["schoologyCourseId"]
        val period = call.request.queryParameters["period"]?.toIntOrNull()

        if (schoologyCourseId == null || period == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        newSuspendedTransaction(Dispatchers.IO, db) {
            val courseResponse = Course
                .select(Course.schoologyCourseId eq schoologyCourseId)
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

            val studentResponse = StudentPeriod
                .innerJoin(Student)
                .innerJoin(Course)
                .select((StudentPeriod.schoologyCourseId eq schoologyCourseId) and
                        (StudentPeriod.period eq period) and
                        (StudentPeriod.studentId eq Student.studentId) and
                        (StudentPeriod.schoologyCourseId eq Course.schoologyCourseId))
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