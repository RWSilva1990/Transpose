package com.example.media

import java.io.Serializable
import java.util.*

class Localization(
    val languageCode: String,
    val countryCode: String? = null
) : Serializable {

    companion object {
        val DEFAULT = Localization("en", "GB")

        fun listFrom(vararg localizationCodeList: String): List<Localization> {
            return localizationCodeList.map { code ->
                fromLocalizationCode(code).orElseThrow {
                    IllegalArgumentException("Not a localization code: $code")
                }
            }
        }

        private fun fromLocalizationCode(localizationCode: String): Optional<Localization> {
            val locale = forLanguageTag(localizationCode)
            return if (locale != null) {
                Optional.of(fromLocale(locale))
            } else {
                Optional.empty()
            }
        }


        private fun fromLocale(locale: Locale): Localization {
            return Localization(locale.language, locale.country)
        }

        fun getLocaleFromThreeLetterCode(code: String): Locale {
            val languages = Locale.getISOLanguages()
            val localeMap = HashMap<String, Locale>(languages.size)
            for (language in languages) {
                val locale = Locale(language)
                localeMap[locale.isO3Language] = locale
            }
            return localeMap[code]
                ?: throw Exception("No locale found for three-letter code: $code")
        }

        private fun forLanguageTag(str: String): Locale? {
            if (str.contains("-")) {
                val args = str.split("-", ignoreCase = false, limit = -1)
                return when {
                    args.size > 2 -> Locale(args[0], args[1], args[2])
                    args.size > 1 -> Locale(args[0], args[1])
                    args.size == 1 -> Locale(args[0])
                    else -> null
                }
            } else if (str.contains("_")) {
                val args = str.split("_", ignoreCase = false, limit = -1)
                return when {
                    args.size > 2 -> Locale(args[0], args[1], args[2])
                    args.size > 1 -> Locale(args[0], args[1])
                    args.size == 1 -> Locale(args[0])
                    else -> null
                }
            } else {
                return Locale(str)
            }
        }
    }

    private val localizationCode: String
        get() = languageCode + (countryCode?.let { "-$it" } ?: "")

    override fun toString(): String {
        return "Localization[$localizationCode]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Localization) return false

        if (languageCode != other.languageCode) return false
        if (countryCode != other.countryCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = languageCode.hashCode()
        result = 31 * result + (countryCode?.hashCode() ?: 0)
        return result
    }


}
