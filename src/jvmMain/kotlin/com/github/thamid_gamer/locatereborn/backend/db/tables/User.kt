package com.github.thamid_gamer.locatereborn.backend.db.tables

import org.jetbrains.exposed.sql.Table

object User : Table() {

    val username = varchar("username", 100)

    val email = varchar("email", 25).nullable()

    override val primaryKey = PrimaryKey(username, name = "PK_USERS_ID")

}