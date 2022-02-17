package com.github.thamid_gamer.locatereborn.shared.api.data

enum class CourseType {
    MATH,
    ENGLISH,
    HISTORY,
    GYM,
    WORLD_LANGUAGE,
    LUNCH,
    PHYSICS,
    BIO,
    CHEM,
    CS,
    ENGINEERING,
    CULINARY,
    MEDICAL,
    ART,
    BUSINESS,
    SPECIAL,
    LAB,
    IGS,
    ELECTIVE,
    UNKNOWN;

    companion object {

        val courseTypeMap = values().associateBy {
            it.name
        }

    }

}