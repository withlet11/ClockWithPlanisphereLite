// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        classpath(libs.oss.licenses.plugin)
    }
}

plugins {
    id("com.google.devtools.ksp") version "2.3.10" apply false
}

tasks.register("clean", Delete::class) {
    description = "Deletes all generated files."
    delete(rootProject.layout.buildDirectory)
}
