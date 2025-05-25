// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("androidx.camera:camera-camera2:1.3.1")
        classpath("androidx.camera:camera-lifecycle:1.3.1")
        classpath("androidx.camera:camera-view:1.3.1")
    }
}