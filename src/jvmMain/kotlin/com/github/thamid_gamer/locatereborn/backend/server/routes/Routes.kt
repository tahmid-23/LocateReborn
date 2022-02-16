package com.github.thamid_gamer.locatereborn.backend.server.routes

import com.github.thamid_gamer.locatereborn.backend.db.tables.StudentPeriods
import com.github.thamid_gamer.locatereborn.backend.db.tables.Students
import com.github.thamid_gamer.locatereborn.backend.server.session.LocateSession
import com.github.thamid_gamer.locatereborn.shared.api.data.CourseType
import com.github.thamid_gamer.locatereborn.shared.api.data.PeriodData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentData
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
            val response = Students
                .selectAll()
                .associateBy({ it[Students.studentId] }) {
                    StudentData(it[Students.firstName], it[Students.lastName], it[Students.isTeacher])
                }

            call.respond(response)
        }
    }
}

fun Route.studentInfoRoute(db: Database) {
    get("/student") {
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
            val response = Students
                .slice(Students.columns - Students.studentId)
                .select(Students.studentId eq id)
                .map {
                    StudentData(it[Students.firstName], it[Students.lastName], it[Students.isTeacher])
                }

            if (response.size > 1) {
                call.respond(HttpStatusCode.InternalServerError)
            }

            call.respond(response.firstOrNull() ?: Any())
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
            val response = StudentPeriods
                .slice(StudentPeriods.columns - StudentPeriods.studentId)
                .select(StudentPeriods.studentId eq id)
                .map {
                    PeriodData(
                        it[StudentPeriods.schoologyCourseId],
                        it[StudentPeriods.fullCourseName],
                        it[StudentPeriods.simpleCourseName],
                        CourseType.courseTypeMap[it[StudentPeriods.courseType]],
                        it[StudentPeriods.day],
                        it[StudentPeriods.period]
                    )
                }

            call.respond(response)
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
            val response = StudentPeriods
                .slice(StudentPeriods.columns - StudentPeriods.studentId)
                .selectAll()
                .groupBy(*(StudentPeriods.columns - StudentPeriods.studentId).toTypedArray())
                .map {
                    PeriodData(
                        it[StudentPeriods.schoologyCourseId],
                        it[StudentPeriods.fullCourseName],
                        it[StudentPeriods.simpleCourseName],
                        CourseType.courseTypeMap[it[StudentPeriods.courseType]],
                        it[StudentPeriods.day],
                        it[StudentPeriods.period]
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
        val day = call.request.queryParameters["day"]?.toIntOrNull()
        val period = call.request.queryParameters["period"]?.toIntOrNull()

        if (schoologyCourseId == null || day == null || period == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        newSuspendedTransaction(Dispatchers.IO, db) {
            val response = (StudentPeriods innerJoin Students)
                .slice(Students.columns)
                .select((StudentPeriods.schoologyCourseId eq schoologyCourseId) and
                        (StudentPeriods.day eq day) and
                        (StudentPeriods.period eq period) and
                        (StudentPeriods.studentId eq Students.studentId))
                .associateBy({ it[Students.studentId] }) {
                    StudentData(
                        it[Students.firstName],
                        it[Students.lastName],
                        it[Students.isTeacher]
                    )
                }

            call.respond(response)
        }
    }
}