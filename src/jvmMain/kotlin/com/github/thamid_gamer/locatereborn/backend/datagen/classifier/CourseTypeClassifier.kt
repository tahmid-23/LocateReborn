package com.github.thamid_gamer.locatereborn.backend.datagen.classifier

import com.github.thamid_gamer.locatereborn.shared.api.data.CourseType

interface CourseTypeClassifier {

    fun classify(simpleCourseName: String): CourseType?

}