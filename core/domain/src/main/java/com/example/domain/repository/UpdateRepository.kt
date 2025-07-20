package com.example.domain.repository

interface UpdateRepository {
    suspend fun checkForUpdate(): UpdateInfo?
}

data class UpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val isUpdateAvailable: Boolean,
    val updateUrl: String = "https://github.com/yourusername/yourrepo/releases/latest"
)