package com.github.thamid_gamer.locatereborn.backend.db.tables

import org.jetbrains.exposed.sql.Table

object Students : Table() {
    val studentId = varchar("student_id", 9)
    val firstName = varchar("first_name", 30)
    val lastName = varchar("last_name", 70)
    val isTeacher = bool("is_teacher")
    val studentType = varchar("student_type", 9)
    val roomNumber = varchar("room_number", 5).nullable()

    override val primaryKey = PrimaryKey(studentId, name = "PK_STUDENT_ID")
}