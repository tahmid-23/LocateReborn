package com.github.thamid_gamer.locatereborn.backend.db.tables

import org.jetbrains.exposed.sql.Table

object DayGroup : Table() {

    val groupId = integer("group_id").autoIncrement()

    val schoologyCourseId = varchar("course_id", 10) references Course.schoologyCourseId

    val period = integer("period")

    override val primaryKey = PrimaryKey(groupId, name = "PK_PERIOD_GROUP")

}