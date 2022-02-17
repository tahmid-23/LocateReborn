package com.github.thamid_gamer.locatereborn.backend.db.tables

import org.jetbrains.exposed.sql.Table

object StudentPeriods : Table() {
    private val studentPeriodId = integer("student_period_id").autoIncrement()
    val studentId = varchar("student_id", 9) references Students.studentId
    val schoologyCourseId = varchar("schoology_course_id", 10)
    val fullCourseName = varchar("full_course_name", 150)
    val simpleCourseName = varchar("simple_course_name", 100)
    val courseType = varchar("course_type", 20)
    val day = integer("day")
    val period = integer("period")

    override val primaryKey = PrimaryKey(studentPeriodId, name = "PK_STUDENT_PERIOD_ID")
}