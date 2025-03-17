package com.example.data.remote.mapper

import okhttp3.ResponseBody

object SuggestionKeywordMapper{
    fun map(responseBody: ResponseBody): List<String> {
        val rawString = responseBody.string()
        // rawString 파싱 로직. 예를 들어 JSON 응답이라면 JSONArray 파싱 등 수행.
        // 만약 단순히 split하여 extractor 사용한다면:

        val splitList = rawString.split(",") // 실제 로직에 맞게
        val keywords = addSubstringToSuggestionKeyword(splitList)
        // unicode 변환 필요시 convertStringUnicodeToKorean 사용
        return keywords.map { convertStringUnicodeToKorean(it) }
    }

    fun addSubstringToSuggestionKeyword(splitList: List<String>): List<String> {
        return splitList.filter { it.length >= 3 }
            .map {
                if (it.last() == ']') it.substring(1, it.length - 2)
                else it.substring(1, it.length - 1)
            }
    }

    fun convertStringUnicodeToKorean(data: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < data.length) {
            if (i + 5 < data.length && data[i] == '\\' && data[i + 1] == 'u') {
                val word = data.substring(i + 2, i + 6).toInt(16).toChar()
                sb.append(word)
                i += 5
            } else {
                sb.append(data[i])
            }
            i++
        }
        return sb.toString()
    }
}