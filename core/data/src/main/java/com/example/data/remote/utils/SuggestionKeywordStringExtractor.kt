package com.example.data.remote.utils

class SuggestionKeywordStringExtractor {

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
