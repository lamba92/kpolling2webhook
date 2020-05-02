@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment")

import com.github.lamba92.gradle.utils.TRAVIS_TAG
import com.github.lamba92.gradle.utils.ktor
import com.github.lamba92.gradle.utils.prepareForPublication
import org.gradle.internal.os.OperatingSystem

buildscript {
    repositories {
        maven("https://dl.bintray.com/lamba92/com.github.lamba92")
        google()
    }
    dependencies {
        classpath("com.github.lamba92", "lamba-gradle-utils", "1.0.6")
    }
}

plugins {
    kotlin("multiplatform") version "1.4-M1"
    id("com.jfrog.bintray") version "1.8.5"
    `maven-publish`
}

group = "com.github.lamba92"
version = TRAVIS_TAG ?: "1.0.0"

repositories {
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    mavenCentral()
    jcenter()
}

kotlin {

    metadata {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
            }
        }
    }

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
            }
        }
    }

    js {
        nodejs()
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    mingwX64 {
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    linuxX64 {
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    macosX64 {
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    sourceSets {

        val ktorVersion: String by project

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api(ktor("client-core", ktorVersion))
                api(ktor("client-serialization", ktorVersion))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                api(ktor("client-apache", ktorVersion))
                api(ktor("client-serialization-jvm", ktorVersion))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                api(ktor("client-js", ktorVersion))
                api(ktor("client-serialization-js", ktorVersion))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val mingwX64Main by getting {
            dependencies {
                api(ktor("client-curl", ktorVersion))
                api(ktor("client-serialization-native", ktorVersion))
            }
        }

        val linuxX64Main by getting {
            dependencies {
                api(ktor("client-curl", ktorVersion))
                api(ktor("client-serialization-native", ktorVersion))
            }
        }

        val macosX64Main by getting {
            dependencies {
                api(ktor("client-curl", ktorVersion))
                api(ktor("client-serialization-native", ktorVersion))
            }
        }

    }
}

prepareForPublication(
    publicationNames = when (OperatingSystem.current().isMacOsX) {
        true -> publishing.publications.map { it.name }.filter { "mac" in it }
        else -> publishing.publications.map { it.name }.filter { "mac" !in it }
    }
)
