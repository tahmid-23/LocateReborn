package com.github.thamid_gamer.locatereborn.backend.db.tables

import org.jetbrains.exposed.sql.Table

object Users : Table() {

    val username = varchar("username", 100)

    override val primaryKey = PrimaryKey(username, name = "PK_USERS_ID")

}