package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface SuggestionKeywordRepository {
    fun getSuggestionKeywords(query: String): Flow<List<String>>
}