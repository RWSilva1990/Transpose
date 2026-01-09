// RoomConvention.kt
package com.example.convention

import androidx.room.gradle.RoomExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureRoom(
    commonExtension: CommonExtension<*, *, *, *, *>
) {

    dependencies {
        add("implementation", libs.findLibrary("androidx.room.runtime").get())
        add("implementation", libs.findLibrary("androidx.room.ktx").get())
        add("kapt", libs.findLibrary("androidx.room.compiler").get())
    }
}