// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin_version by extra("2.0.21")
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}

plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
