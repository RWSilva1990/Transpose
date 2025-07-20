package com.example.data.remote.repository

import android.util.Log
import com.example.data.remote.api.SuggestionKeywordApiService
import com.example.domain.repository.SuggestionKeywordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import javax.inject.Inject


class SuggestionKeywordRepositoryImpl @Inject constructor(
    private val suggestionKeywordApiService: SuggestionKeywordApiService,
) : SuggestionKeywordRepository {

    override fun getSuggestionKeywords(query: String): Flow<List<String>> = flow {
        try {
            val responseBody =
                suggestionKeywordApiService.getSuggestionKeyword("firefox", "yt", query)
            val parsedSuggestionKeywords = parseSuggestionKeywords(responseBody)
            emit(parsedSuggestionKeywords)
        } catch (e: Exception) {
            Log.e("SuggestionKeywordRepository", "Error fetching suggestion keywords", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    private fun parseSuggestionKeywords(responseBody: okhttp3.ResponseBody): List<String> {
        return try {
            val jsonString = responseBody.string()
            val jsonArray = JSONArray(jsonString)

            if (jsonArray.length() > 1) {
                val suggestionsArray = jsonArray.getJSONArray(1)
                val suggestions = mutableListOf<String>()

                for (i in 0 until suggestionsArray.length()) {
                    suggestions.add(suggestionsArray.getString(i))
                }

                return suggestions
            }

            emptyList()
        } catch (e: Exception) {
            Log.e("SuggestionMapper", "JSON parsing error", e)
            emptyList()
        }
    }
}