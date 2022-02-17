package com.github.thamid_gamer.locatereborn.frontend.data

import com.github.thamid_gamer.locatereborn.shared.api.data.CourseType

interface CourseTypeColorizer {

    fun colorize(courseType: CourseType): String

}