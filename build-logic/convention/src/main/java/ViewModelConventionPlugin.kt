import com.android.build.api.dsl.ApplicationExtension
import com.example.convention.configureViewModel
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ViewModelConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<ApplicationExtension> {
                configureViewModel(this)
            }
        }
    }
}