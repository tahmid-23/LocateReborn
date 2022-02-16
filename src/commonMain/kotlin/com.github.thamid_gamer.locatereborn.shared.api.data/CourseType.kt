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
    ELECTIVE;

    companion object {

        val courseTypeMap = buildMap {
            for (courseType in values()) {
                put(courseType.name, courseType)
            }
        }

    }

}