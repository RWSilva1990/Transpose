package com.example.media.audio

import androidx.annotation.OptIn
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessorChain
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
class SignalsmithAudioProcessorChain(
    vocalRemovalProcessor: VocalRemovalProcessor,
    private val signalsmithProcessor: SignalsmithAudioProcessor
) : AudioProcessorChain {

    private val processors: Array<AudioProcessor> = arrayOf(
        vocalRemovalProcessor,
        signalsmithProcessor
    )

    override fun getAudioProcessors(): Array<AudioProcessor> = processors

    override fun applyPlaybackParameters(playbackParameters: PlaybackParameters): PlaybackParameters {
        return PlaybackParameters.DEFAULT
    }

    override fun applySkipSilenceEnabled(skipSilenceEnabled: Boolean): Boolean {
        return false
    }

    override fun getMediaDuration(playoutDuration: Long): Long {
        return signalsmithProcessor.getMediaDurationUs(playoutDuration)
    }

    override fun getSkippedOutputFrameCount(): Long = 0L
}
