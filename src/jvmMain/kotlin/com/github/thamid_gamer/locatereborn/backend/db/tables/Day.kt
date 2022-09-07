package com.github.thamid_gamer.locatereborn.backend.db.tables

import org.jetbrains.exposed.sql.Table

object Day : Table() {

    val groupId = integer("group_id") references DayGroup.groupId

    val day = integer("day")

}