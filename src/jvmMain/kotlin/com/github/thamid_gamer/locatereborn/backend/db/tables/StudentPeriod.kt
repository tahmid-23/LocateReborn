package com.github.thamid_gamer.locatereborn.backend.db.tables

import org.jetbrains.exposed.sql.Table

object StudentPeriod : Table() {

    private val studentPeriodId = integer("student_period_id").autoIncrement()

    val studentId = varchar("student_id", 9) references Student.studentId

    val schoologyCourseId = varchar("course_id", 10) references Course.schoologyCourseId

    val period = integer("period")

    val day = integer("day")

    override val primaryKey = PrimaryKey(studentPeriodId, name = "PK_STUDENT_PERIOD_ID")

}