package com.github.thamid_gamer.locatereborn.backend.datagen.generator

interface LocateDataGenerator {

    suspend fun generateData(request: GeneratorRequest): DataGenerationResult

}