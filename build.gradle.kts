plugins {
    kotlin("jvm") version "1.8.22" apply false
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false
}

repositories {
    google()
    mavenCentral()
}

tasks.register("clean") {
    doLast {
        delete(rootProject.buildDir)
    }
}
