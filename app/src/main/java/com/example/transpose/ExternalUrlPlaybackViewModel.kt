package com.example.transpose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.newpipe.repository.base.NewPipeManager
import com.example.domain.model.youtube.video.Video
import com.example.media.manager.MediaPlaybackManager
import com.example.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExternalUrlPlaybackViewModel @Inject constructor(
    private val newPipeManager: NewPipeManager,
    private val mediaPlaybackManager: MediaPlaybackManager
) : ViewModel() {

    fun openUrl(rawUrl: String) {
        val url = rawUrl.trim()
        if (url.isBlank()) return

        viewModelScope.launch {
            try {
                val video = withContext(Dispatchers.IO) {
                    val extractor = newPipeManager.getStreamExtractor(url)
                    extractor.fetchPage()

                    val uploaderId = try {
                        newPipeManager.getChannelId(extractor.uploaderUrl)
                    } catch (_: Exception) {
                        null
                    }

                    Video(
                        id = extractor.id,
                        title = extractor.name,
                        thumbnailUrl = extractor.thumbnails.firstOrNull()?.url,
                        description = extractor.description.content,
                        publishTimestamp = extractor.uploadDate?.offsetDateTime()?.toInstant()?.toEpochMilli(),
                        infoType = "Stream",
                        uploaderName = extractor.uploaderName,
                        uploaderUrl = uploaderId,
                        uploaderAvatarUrl = extractor.uploaderAvatars.firstOrNull()?.url,
                        uploaderVerified = null,
                        duration = extractor.length,
                        viewCount = extractor.viewCount,
                        textualUploadDate = extractor.textualUploadDate,
                        streamType = null,
                        shortFormContent = false
                    )
                }

                // The controller is created asynchronously when the app starts. Wait for
                // it so a link received during cold start is not silently discarded.
                mediaPlaybackManager.mediaControllerFlow.filterNotNull().first()
                mediaPlaybackManager.playSingleVideo(video)
            } catch (error: Exception) {
                Logger.e("Failed to open shared URL: $url", error)
            }
        }
    }
}
