package com.example.util

import android.util.Log
import com.example.transpose.core.utils.BuildConfig

object Logger {
    private const val TAG = "CustomLog"

    fun d(message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "✅ $message")
    }

    fun d(tag: String, message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(tag, "✅ $message")
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, "❌ $message", throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, "❌ $message", throwable)
    }
}
