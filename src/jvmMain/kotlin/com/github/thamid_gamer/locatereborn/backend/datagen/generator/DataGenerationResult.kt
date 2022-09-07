package com.github.thamid_gamer.locatereborn.backend.datagen.generator

import com.github.thamid_gamer.locatereborn.shared.api.data.CourseData
import com.github.thamid_gamer.locatereborn.shared.api.data.DayGroupData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentData

data class DataGenerationResult(
    val studentMap: Map<String, Pair<StudentData, Collection<DayGroupData>>>,
    val courseMap: Map<String, Pair<CourseData, Collection<DayGroupData>>>
)