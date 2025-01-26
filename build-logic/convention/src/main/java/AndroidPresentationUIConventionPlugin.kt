import com.example.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidPresentationUIConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.run {
            pluginManager.run {
                apply("transpose.android.library.compose")
            }

            dependencies {
                add("implementation", libs.findLibrary("androidx.core.ktx").get())
                add("implementation", libs.findLibrary("androidx.appcompat").get())
                add("implementation", libs.findLibrary("material").get())

                add("implementation", project.libs.findBundle("compose").get())
                add("debugImplementation", project.libs.findBundle("compose.debug").get())
                add("androidTestImplementation", project.libs.findLibrary("androidx.ui.test.junit4").get())

                add("implementation", project.libs.findLibrary("androidx.compose.material").get())
                add("implementation", project.libs.findLibrary("androidx.material3.android").get())
                add("implementation", project.libs.findLibrary("androidx.material3.v121").get())
                add("implementation", project.libs.findLibrary("androidx.material").get())

                add("implementation", project.libs.findLibrary("androidx.navigation.compose").get())
                add("implementation", project.libs.findLibrary("androidx.navigation.runtime.ktx").get())
                add("implementation", project.libs.findLibrary("androidx.hilt.navigation.compose").get())

                add("implementation", project.libs.findLibrary("androidx.constraintlayout.compose").get())
                add("implementation", project.libs.findLibrary("compose.shimmer").get())
                add("implementation", project.libs.findLibrary("accompanist.systemuicontroller").get())
                add("implementation", project.libs.findLibrary("coil.compose").get())
            }
        }
    }
}
