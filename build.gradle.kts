plugins {
    application
    kotlin("multiplatform") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
}

group = "com.github.thamid_gamer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.exposed)
                implementation(libs.google.api.client)
                implementation(libs.jsoup)
                implementation(libs.kotlinx.html)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.apache)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.server.contentNegotiation)
                implementation(libs.ktor.server.logging)
                implementation(libs.ktor.server.htmlBuilder)
                implementation(libs.ktor.server.httpRedirect)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.sessions)
                implementation(libs.logback)
                implementation(libs.sqlite)
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.react)
                implementation(libs.react.dom)
                implementation(libs.react.router)
                implementation(npm("react-helmet-async", "1.3.0"))
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("$group.locatereborn.backend.server.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))

    workingDir = File("/run")
}

val cleanDistribution = tasks.register<Delete>("cleanDistribution") {
    delete(
        File(projectDir, "$/build/distributions/LocateReborn-1.0-SNAPSHOT.tar"),
        File(projectDir, "$/build/distributions/LocateReborn-1.0-SNAPSHOT.zip")
    )
}

tasks.named<Jar>("jvmJar") {
    dependsOn(cleanDistribution)
}