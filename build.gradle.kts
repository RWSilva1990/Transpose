// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("androidx.room") version "2.6.1" apply false
    id("com.google.firebase.crashlytics") version "3.0.3" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    id("dev.iurysouza.modulegraph") version "0.12.0"


}
moduleGraphConfig {
    readmePath.set("${rootDir}/module-graph.md") // 원하는 파일명/경로로
    heading.set("### Module Graph")
    excludedModulesRegex.set(".*baselineprofile.*|.*benchmark.*")
    showFullPath.set(false)
}