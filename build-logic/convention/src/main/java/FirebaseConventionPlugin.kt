import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class FirebaseConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("com.google.gms.google-services")
            pluginManager.apply("com.google.firebase.crashlytics")

            dependencies {
                val libs = project.extensions.getByType<VersionCatalogsExtension>()
                    .named("libs")

                add("implementation", platform(libs.findLibrary("firebase.bom").get()))
                add("implementation", libs.findLibrary("firebase.analytics").get())
                add("implementation", libs.findLibrary("firebase.crashlytics").get())
            }
        }
    }
}