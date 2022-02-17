package com.github.thamid_gamer.locatereborn.backend.datagen.classifier

import com.github.thamid_gamer.locatereborn.shared.api.data.PeriodData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentType

class LunchStudentTypeClassifier : StudentTypeClassifier {

    override fun classify(isTeacher: Boolean, periods: Iterable<PeriodData>): StudentType {
        if (isTeacher) {
            return StudentType.STAFF
        }

        for (period in periods) {
            when (period.simpleCourseName) {
                "~Lunch Period 4" -> return StudentType.FRESHMAN
                "~Lunch Period 5" -> return StudentType.SOPHOMORE
                "~Lunch Period 6" -> return StudentType.JUNIOR
                "~Lunch Period 6 Seniors" -> return StudentType.SENIOR
            }
        }

        return StudentType.UNKNOWN
    }

}