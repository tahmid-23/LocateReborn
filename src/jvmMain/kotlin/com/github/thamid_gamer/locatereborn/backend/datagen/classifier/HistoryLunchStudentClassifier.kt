package com.github.thamid_gamer.locatereborn.backend.datagen.classifier

import com.github.thamid_gamer.locatereborn.shared.api.data.CourseData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentType

class HistoryLunchStudentClassifier : StudentTypeClassifier {

    override fun classify(isTeacher: Boolean, courses: Iterable<CourseData>): StudentType {
        if (isTeacher) {
            return StudentType.STAFF
        }

        for (period in courses) {
            when (period.simpleCourseName) {
                "World History I" -> return StudentType.FRESHMAN
                "World History II" -> return StudentType.SOPHOMORE
                "~Lunch Grade 11" -> return StudentType.JUNIOR
                "~Lunch Grade 12" -> return StudentType.SENIOR
            }
        }

        return StudentType.UNKNOWN
    }

}