package com.github.thamid_gamer.locatereborn.frontend.data

import com.github.thamid_gamer.locatereborn.shared.api.data.CourseType

class BasicCourseTypeColorizer : CourseTypeColorizer {

    private val colorMap = buildMap {
        put(CourseType.MATH, "math")
        put(CourseType.ENGLISH, "english")
        put(CourseType.HISTORY, "history")
        put(CourseType.GYM, "gym")
        put(CourseType.WORLD_LANGUAGE, "language")
        put(CourseType.LUNCH, "lunch")
        put(CourseType.PHYSICS, "physics")
        put(CourseType.BIO, "biology")
        put(CourseType.CHEM, "chem")
        put(CourseType.CS, "cs")
        put(CourseType.ENGINEERING, "engineering")
        put(CourseType.CULINARY, "culinary")
        put(CourseType.MEDICAL, "medical")
        put(CourseType.ART, "art")
        put(CourseType.BUSINESS, "business")
        put(CourseType.SPECIAL, "special")
        put(CourseType.LAB, "lab")
        put(CourseType.IGS, "igs")
        put(CourseType.ELECTIVE, "elective")
    }

    override fun colorize(courseType: CourseType?): String {
        return colorMap[courseType] ?: colorMap[CourseType.ELECTIVE]!!
    }

}