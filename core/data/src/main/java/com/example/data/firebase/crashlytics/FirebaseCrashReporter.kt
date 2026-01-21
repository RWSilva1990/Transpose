package com.example.data.firebase.crashlytics

import com.example.util.CrashReporter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashReporter @Inject constructor() : CrashReporter {

    private val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    override fun setCustomKey(key: String, value: String) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Exception) {
            // Silently fail if Crashlytics is not available
        }
    }

    override fun setCustomKey(key: String, value: Int) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Exception) {
            // Silently fail if Crashlytics is not available
        }
    }

    override fun log(message: String) {
        try {
            crashlytics.log(message)
        } catch (e: Exception) {
            // Silently fail if Crashlytics is not available
        }
    }

    override fun recordException(throwable: Throwable) {
        try {
            crashlytics.recordException(throwable)
        } catch (e: Exception) {
            // Silently fail if Crashlytics is not available
        }
    }
}
