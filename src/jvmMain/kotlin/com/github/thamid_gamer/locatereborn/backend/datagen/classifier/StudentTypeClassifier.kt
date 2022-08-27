package com.github.thamid_gamer.locatereborn.backend.datagen.classifier

import com.github.thamid_gamer.locatereborn.shared.api.data.CourseData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentType

interface StudentTypeClassifier {

    fun classify(isTeacher: Boolean, courses: Iterable<CourseData>): StudentType

}