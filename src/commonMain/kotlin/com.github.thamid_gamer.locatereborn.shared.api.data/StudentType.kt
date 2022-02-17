package com.github.thamid_gamer.locatereborn.shared.api.data

enum class StudentType {
    FRESHMAN,
    SOPHOMORE,
    JUNIOR,
    SENIOR,
    STAFF,
    UNKNOWN;

    companion object {

        val studentTypeMap = values().associateBy {
            it.name
        }

    }

}