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

    fun i(message: String) {
        if (!BuildConfig.DEBUG) return
        Log.i(TAG, "ℹ️ $message")
    }

    fun i(tag: String, message: String) {
        if (!BuildConfig.DEBUG) return
        Log.i(tag, "ℹ️ $message")
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (!BuildConfig.DEBUG) return
        Log.e(TAG, "❌ $message", throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (!BuildConfig.DEBUG) return
        Log.e(tag, "❌ $message", throwable)
    }
}
