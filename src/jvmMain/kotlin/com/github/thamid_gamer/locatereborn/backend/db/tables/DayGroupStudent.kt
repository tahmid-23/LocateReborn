package com.github.thamid_gamer.locatereborn.backend.db.tables

import org.jetbrains.exposed.sql.Table

object DayGroupStudent : Table() {

    private val dayGroupStudentId = integer("day_group_student_id").autoIncrement()

    val studentId = varchar("student_id", 9) references Student.studentId

    val groupId = integer("group_id") references DayGroup.groupId

    override val primaryKey = PrimaryKey(dayGroupStudentId, name = "PK_DAY_GROUP_STUDENT_ID")

}