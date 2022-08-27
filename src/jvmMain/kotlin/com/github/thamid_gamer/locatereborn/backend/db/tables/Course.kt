package com.github.thamid_gamer.locatereborn.backend.db.tables

import org.jetbrains.exposed.sql.Table

object Course : Table() {

    val schoologyCourseId = varchar("schoology_course_id", 10)

    val fullCourseName = varchar("full_course_name", 150)

    val simpleCourseName = varchar("simple_course_name", 100)

    val courseType = varchar("course_type", 20)

    override val primaryKey = PrimaryKey(schoologyCourseId, name = "PK_COURSE_ID")

}