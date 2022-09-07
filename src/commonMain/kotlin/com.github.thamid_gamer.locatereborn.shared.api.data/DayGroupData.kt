package com.github.thamid_gamer.locatereborn.shared.api.data

import kotlinx.serialization.Serializable

@Serializable
data class DayGroupData(val schoologyCourseId: String, val period: Int, val days: Collection<Int>)
