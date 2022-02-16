package com.github.thamid_gamer.locatereborn.shared.api.data

import kotlinx.serialization.Serializable

@Serializable
data class PeriodData(val schoologyCourseId: String,
                      val fullCourseName: String,
                      val simpleCourseName: String,
                      val courseType: CourseType?,
                      val day: Int,
                      val period: Int)
