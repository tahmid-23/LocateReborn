package com.github.thamid_gamer.locatereborn.backend.datagen.generator

import com.github.thamid_gamer.locatereborn.shared.api.data.PeriodData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentData

data class DataGenerationResult(
    val studentMap: Map<String, StudentData>,
    val studentPeriods: Map<String, Iterable<PeriodData>>
)