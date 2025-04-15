package com.example.media.manager

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.media.MediaService
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaControllerProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _mediaController = MutableStateFlow<MediaController?>(null)
    val mediaController: StateFlow<MediaController?> = _mediaController.asStateFlow()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, MediaService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            val controller = future.get()
            _mediaController.value = controller
        }, MoreExecutors.directExecutor())
    }

    fun release() {
        _mediaController.value?.release()
        _mediaController.value = null
    }
}
