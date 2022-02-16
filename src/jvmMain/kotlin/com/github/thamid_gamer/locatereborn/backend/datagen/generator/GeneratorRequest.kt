package com.github.thamid_gamer.locatereborn.backend.datagen.generator

data class GeneratorRequest(val username: String, val password: String, val groups: Iterable<String>)