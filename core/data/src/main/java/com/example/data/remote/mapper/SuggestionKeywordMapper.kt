package com.example.data.remote.mapper

import com.example.data.remote.utils.SuggestionKeywordStringExtractor
import okhttp3.ResponseBody

class SuggestionKeywordMapper(
    private val extractor: SuggestionKeywordStringExtractor
) {
    fun map(responseBody: ResponseBody): List<String> {
        val rawString = responseBody.string()
        // rawString 파싱 로직. 예를 들어 JSON 응답이라면 JSONArray 파싱 등 수행.
        // 만약 단순히 split하여 extractor 사용한다면:

        val splitList = rawString.split(",") // 실제 로직에 맞게
        val keywords = extractor.addSubstringToSuggestionKeyword(splitList)
        // unicode 변환 필요시 convertStringUnicodeToKorean 사용
        return keywords.map { extractor.convertStringUnicodeToKorean(it) }
    }
}