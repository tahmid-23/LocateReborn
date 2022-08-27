package com.github.thamid_gamer.locatereborn.backend.datagen.generator

import com.github.thamid_gamer.locatereborn.shared.api.data.CourseData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentData
import com.github.thamid_gamer.locatereborn.shared.api.data.StudentPeriodData

data class DataGenerationResult(
    val studentMap: Map<String, Pair<StudentData, Collection<StudentPeriodData>>>,
    val courseMap: Map<String, CourseData>
)