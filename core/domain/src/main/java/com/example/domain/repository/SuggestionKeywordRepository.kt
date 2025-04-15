package com.example.domain.repository

interface SuggestionKeywordRepository {
    suspend fun getSuggestionKeywords(query: String): Result<List<String>>
}