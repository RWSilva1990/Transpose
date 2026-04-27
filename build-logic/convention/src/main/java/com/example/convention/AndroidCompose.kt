package com.example.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *>
) {
    with(pluginManager) {
        apply("org.jetbrains.kotlin.plugin.compose")
    }

    commonExtension.run {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = libs.findVersion("composeCompiler").get().toString()
        }

        dependencies {
            val bom = libs.findLibrary("androidx.compose.bom").get()
            add("implementation", project.libs.findLibrary("androidx.activity.compose").get())
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
            add("testImplementation", project.libs.findLibrary("junit").get())
            add("debugImplementation", libs.findLibrary("androidx.ui.tooling.preview").get())
        }
    }

    // Stability configuration for Compose compiler
    val stabilityConfigFile = project.rootProject.file("compose-stability.conf")
    if (stabilityConfigFile.exists()) {
        project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
            compilerOptions {
                freeCompilerArgs.addAll(
                    "-P", "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${stabilityConfigFile.absolutePath}"
                )
            }
        }
    }

    // Compose Compiler Metrics: run with -Ptranspose.enableComposeCompilerReports=true
    if (project.findProperty("transpose.enableComposeCompilerReports") == "true") {
        val metricsDir = project.layout.buildDirectory.dir("compose_metrics").get().asFile
        project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
            compilerOptions {
                freeCompilerArgs.addAll(
                    "-P", "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${metricsDir.absolutePath}",
                    "-P", "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${metricsDir.absolutePath}"
                )
            }
        }
    }
}
