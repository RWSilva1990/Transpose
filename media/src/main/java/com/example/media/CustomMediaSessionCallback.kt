package com.example.media

import android.content.Context
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS
import androidx.media3.datasource.DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.media.audio.SignalsmithAudioProcessor
import com.google.common.util.concurrent.ListenableFuture
import javax.inject.Inject

@UnstableApi
class CustomMediaSessionCallback @Inject constructor(
    private val context: Context,
    private val signalsmithAudioProcessor: SignalsmithAudioProcessor
) : MediaSession.Callback {

    private fun createMyCustomCommands(): List<SessionCommand> {
        return listOf(
            SessionCommand(MediaSessionCallback.PITCH_PLUS, Bundle()),
            SessionCommand(MediaSessionCallback.PITCH_MINUS, Bundle()),
            SessionCommand(MediaSessionCallback.INIT_PITCH_VALUE, Bundle()),
            SessionCommand(MediaSessionCallback.TEMPO_PLUS, Bundle()),
            SessionCommand(MediaSessionCallback.TEMPO_MINUS, Bundle()),
            SessionCommand(MediaSessionCallback.INIT_TEMPO_VALUE, Bundle())
        )
    }

    private fun createCommandButton(): List<CommandButton> {
        val pitchMinusCommand = SessionCommand(MediaSessionCallback.PITCH_MINUS, Bundle())
        val pitchPlusCommand = SessionCommand(MediaSessionCallback.PITCH_PLUS, Bundle())

        val minusButton = CommandButton.Builder()
            .setSessionCommand(pitchMinusCommand)
            .setIconResId(com.example.media.R.drawable.baseline_exposure_neg_1_24)
            .setDisplayName("Minus")
            .build()

        val plusButton = CommandButton.Builder()
            .setSessionCommand(pitchPlusCommand)
            .setIconResId(com.example.media.R.drawable.baseline_exposure_plus_1_24)
            .setDisplayName("Plus")
            .build()

        return listOf(minusButton, plusButton)
    }

    override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
        super.onPostConnect(session, controller)
        session.setCustomLayout(controller, createCommandButton())
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        return super.onAddMediaItems(mediaSession, controller, mediaItems)
    }

    private fun createOptimizedSource(uri: String, dataSourceFactory: DataSource.Factory): MediaSource {
        return when {
            uri.contains(".mpd") -> DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
            uri.contains(".m3u8") -> HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
            else -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
        }
    }

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)

        val commandButtons = createCommandButton()
        val myCustomCommands = createMyCustomCommands()
        val sessionCommands = connectionResult.availableSessionCommands.buildUpon()

        commandButtons.forEach { commandButton ->
            commandButton.sessionCommand?.let {
                sessionCommands.add(it)
            }
        }
        myCustomCommands.forEach { customCommand ->
            sessionCommands.add(customCommand)
        }

        return MediaSession.ConnectionResult.accept(
            sessionCommands.build(), connectionResult.availablePlayerCommands
        )
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (customCommand.customAction) {
            MediaSessionCallback.PITCH_PLUS -> {
                signalsmithAudioProcessor.addPitchSemitone()
            }
            MediaSessionCallback.PITCH_MINUS -> {
                signalsmithAudioProcessor.subtractPitchSemitone()
            }
            MediaSessionCallback.INIT_PITCH_VALUE -> {
                signalsmithAudioProcessor.resetPitch()
            }
            MediaSessionCallback.TEMPO_PLUS -> {
                // Tempo controls are disabled until native time-stretching is wired end-to-end.
            }
            MediaSessionCallback.TEMPO_MINUS -> {
                // Tempo controls are disabled until native time-stretching is wired end-to-end.
            }
            MediaSessionCallback.INIT_TEMPO_VALUE -> {
                // Tempo controls are disabled until native time-stretching is wired end-to-end.
            }
        }
        return super.onCustomCommand(session, controller, customCommand, args)
    }
}
