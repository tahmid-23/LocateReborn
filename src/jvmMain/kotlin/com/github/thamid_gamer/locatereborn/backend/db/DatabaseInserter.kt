package com.github.thamid_gamer.locatereborn.backend.db

import com.github.thamid_gamer.locatereborn.backend.datagen.generator.DataGenerationResult

interface DatabaseInserter {

    suspend fun updateData(dataGenerationResult: DataGenerationResult)

}