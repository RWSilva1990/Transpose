package com.example.domain.repository

import com.example.domain.model.local_file.LocalFileData

interface LocalFileRepository {
    fun getAudioFiles(): Result<List<LocalFileData>>
    fun getVideoFiles(): Result<List<LocalFileData>>
    fun deleteFile(file: LocalFileData): Result<Boolean>

}