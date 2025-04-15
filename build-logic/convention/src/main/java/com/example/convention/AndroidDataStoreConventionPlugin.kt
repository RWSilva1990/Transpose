package com.example.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidDataStoreConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                pluginManager.apply("androidx.datastore:datastore-preferences")
            }
        }
    }
}