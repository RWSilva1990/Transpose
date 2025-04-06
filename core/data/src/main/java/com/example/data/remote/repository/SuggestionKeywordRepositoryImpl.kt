package com.example.data.remote.repository

import com.example.data.remote.mapper.SuggestionKeywordMapper
import com.example.domain.repository.SuggestionKeywordRepository
import com.example.data.remote.api.SuggestionKeywordApiService
import javax.inject.Inject


class SuggestionKeywordRepositoryImpl @Inject constructor(
    private val suggestionKeywordApiService: SuggestionKeywordApiService,
) : SuggestionKeywordRepository {

    override suspend fun getSuggestionKeywords(query: String): Result<List<String>> = runCatching {
        val responseBody = suggestionKeywordApiService.getSuggestionKeyword("firefox", "yt", query)
        SuggestionKeywordMapper.map(responseBody)
    }
}