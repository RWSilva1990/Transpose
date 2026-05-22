package com.example.data.firebase.repository


import android.content.Context
import com.example.domain.repository.UpdateInfo
import com.example.domain.repository.UpdateRepository
import com.example.transpose.core.data.BuildConfig
import com.example.util.Logger
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UpdateRepository {

    private val remoteConfig = Firebase.remoteConfig.apply {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        setConfigSettingsAsync(configSettings)

        setDefaultsAsync(
            mapOf(
                "latest_version" to getCurrentVersion(),
                "update_url" to "https://github.com/joh9911/Transpose/releases/latest",
                "release_notes_en" to "",
                "release_notes_ko" to ""
            )
        )
    }

    override suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            remoteConfig.fetchAndActivate().await()

            // 값 확인
            val latestVersion = remoteConfig.getString("latest_version").ifEmpty {
                getCurrentVersion()
            }

            Logger.d("Latest version = $latestVersion")

            // 5. 모든 파라미터 확인
            remoteConfig.all.forEach { (key, value) ->
                Logger.d("UpdateRepository: $key = ${value.asString()} (source: ${value.source})")
            }

            val currentVersion = getCurrentVersion()
            val updateUrl = remoteConfig.getString("update_url")
            val releaseNotesEn = remoteConfig.getString("release_notes_en")
            val releaseNotesKo = remoteConfig.getString("release_notes_ko")

            UpdateInfo(
                currentVersion = currentVersion,
                latestVersion = latestVersion,
                isUpdateAvailable = isVersionLower(currentVersion, latestVersion),
                updateUrl = updateUrl,
                releaseNotesEn = releaseNotesEn,
                releaseNotesKo = releaseNotesKo
            )
        } catch (e: Exception) {
            Logger.e("UpdateRepository: Error", e)
            null
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun isVersionLower(current: String, latest: String): Boolean {
        try {
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }

            for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
                val currentPart = currentParts.getOrNull(i) ?: 0
                val latestPart = latestParts.getOrNull(i) ?: 0

                if (currentPart < latestPart) return true
                if (currentPart > latestPart) return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }
}
