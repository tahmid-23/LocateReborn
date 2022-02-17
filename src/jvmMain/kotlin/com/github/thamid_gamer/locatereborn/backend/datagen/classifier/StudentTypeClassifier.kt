package com.github.thamid_gamer.locatereborn.backend.datagen.classifier

import com.github.thamid_gamer.locatereborn.shared.api.data.PeriodData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentType

interface StudentTypeClassifier {

    fun classify(isTeacher: Boolean, periods: Iterable<PeriodData>): StudentType

}