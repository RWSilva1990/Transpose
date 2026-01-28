package com.example.domain.repository

interface UpdateRepository {
    suspend fun checkForUpdate(): UpdateInfo?
}

data class UpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val isUpdateAvailable: Boolean,
    val updateUrl: String = "https://github.com/joh9911/Transpose/releases/latest",
    val releaseNotesEn: String = "",
    val releaseNotesKo: String = ""
)