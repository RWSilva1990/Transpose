package com.example.util

/**
 * Interface for crash/error reporting.
 * Implementation can be Firebase Crashlytics, custom logging, etc.
 */
interface CrashReporter {
    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Int)
    fun log(message: String)
    fun recordException(throwable: Throwable)
}
