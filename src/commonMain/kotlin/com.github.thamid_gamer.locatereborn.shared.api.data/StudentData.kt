package com.github.thamid_gamer.locatereborn.shared.api.data

import kotlinx.serialization.Serializable

@Serializable
data class StudentData(
    val studentId: String,
    val firstName: String,
    val lastName: String,
    val isTeacher: Boolean,
    val studentType: StudentType,
    val roomNumber: String?,
)
