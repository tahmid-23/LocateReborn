package com.github.thamid_gamer.locatereborn.backend.datagen.classifier

import com.github.thamid_gamer.locatereborn.shared.api.data.CourseType

class ManualCourseTypeClassifier : CourseTypeClassifier {

    private val keywordMap = buildMap {
        put(CourseType.ART, listOf("acting", "visual", "music", "digital", "directing", "voice", "conducting", "media",
            "theatre", "design prod", "graphic", "theatre", "orchestra", "dance", "concert", "video",
            "interactive design", "creative art", "imaging", "guitar", "theremin", "photoshop", "band", "choir",
            "emerging tech", "art", "fashion", "yoga", "playwriting", "wind ensemble"))
        put(CourseType.MATH, listOf("analysis", "alg", "calc", "discrete", "stat", "math"))
        put(CourseType.ENGLISH, listOf("lit", "writing", "poet"))
        put(CourseType.HISTORY, listOf("hist", "gov", "psych"))
        put(CourseType.GYM, listOf("~pe", "fitness", "driver", "health", "hlth"))
        put(CourseType.WORLD_LANGUAGE, listOf("francais", "espanol", "mandarin"))
        put(CourseType.LUNCH, listOf("lunch"))
        put(CourseType.PHYSICS, listOf("physics", "mechanics", "relativity"))
        put(CourseType.BIO, listOf("bio", "env", "agriscience", "microscopy", "plant"))
        put(CourseType.CHEM, listOf("chem", "nano"))
        put(CourseType.CS, listOf("computer", "program", "data", "capstone_cs", "software", "comp sci", "computation",
            "web", "query", "ux", "machine"))
        put(CourseType.ENGINEERING, listOf("electr", "material", "makerspace", "eng capstone", "printing", "mrl",
            "architect", "u build", "bridge", "eng applications", "engineering", "3d"))
        put(CourseType.CULINARY, listOf("hospt", "hospitality", "hotel", "culinary", "entrep", "prostart", "food",
            "chocolate"))
        put(CourseType.MEDICAL, listOf("pharm", "neuro", "epid", "biotechnology", "anatomy", "surgical"))
        put(CourseType.BUSINESS, listOf("market", "business", "econ", "knowledge", "fed challenge"))
        put(CourseType.SPECIAL, listOf("guidance", "senior", "seminar", "study", "department", "clubs"))
        put(CourseType.LAB, listOf("lab", "research"))
        put(CourseType.IGS, listOf("igs"))
        put(CourseType.ELECTIVE, listOf("wind chimes", "model un", "criminology", "speaking", "screenwriting", "mars",
            "yearbook", "card games", "mouse trap", "journalism", "rocketry", "mythology", "propaganda",
            "conversation", "sign", "east asia", "traditional", "design", "podcast", "earthquake", "passion",
            "sustainable", "leadership", "ptf-mml", "astronomy"))
    }

    override fun classify(simpleCourseName: String): CourseType {
        val lowercase = simpleCourseName.lowercase()
        for (keywordCandidate in keywordMap) {
            if (keywordCandidate.value.any { it in lowercase }) {
                return keywordCandidate.key
            }
        }

        return CourseType.UNKNOWN
    }

}