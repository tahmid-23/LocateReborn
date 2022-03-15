plugins {
    kotlin("multiplatform") version "1.6.20-RC"
    kotlin("plugin.serialization") version "1.6.20-M1-106"
    application
}

group = "com.github.thamid_gamer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("ch.qos.logback:logback-classic:1.3.0-alpha13")
                implementation("com.google.api-client:google-api-client:1.33.2")
                implementation("io.ktor:ktor-client-apache:2.0.0-eap-329")
                implementation("io.ktor:ktor-client-logging:2.0.0-eap-329")
                implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.0.0-eap-329")
                implementation("io.ktor:ktor-server-content-negotiation-jvm:2.0.0-eap-329")
                implementation("io.ktor:ktor-server-call-logging-jvm:2.0.0-eap-329")
                implementation("io.ktor:ktor-server-sessions-jvm:2.0.0-eap-329")
                implementation("io.ktor:ktor-server-netty-jvm:2.0.0-eap-329")
                implementation("io.ktor:ktor-html-builder-jvm:2.0.0-eap-278")
                implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.3.2")
                implementation("org.jsoup:jsoup:1.14.3")
                implementation("org.xerial:sqlite-jdbc:3.36.0.3")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:2.0.0-eap-329")
                implementation("io.ktor:ktor-client-content-negotiation-js:2.0.0-eap-329")
                implementation("io.ktor:ktor-serialization-kotlinx-json-js:2.0.0-eap-329")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.293-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.293-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.2.1-pre.293-kotlin-1.6.10")
                implementation(npm("react-helmet-async", "1.2.2"))
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