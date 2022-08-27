package com.github.thamid_gamer.locatereborn.shared.api.data

import kotlinx.serialization.Serializable

@Serializable
data class StudentPeriodData(val studentId: String,
                             val schoologyCourseId: String,
                             val period: Int,
                             val day: Int)