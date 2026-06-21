package com.example.media.audio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink

@OptIn(UnstableApi::class)
class ProcessorRenderersFactory(
    context: Context,
    private val vocalRemovalProcessor: VocalRemovalProcessor,
    private val signalsmithProcessor: SignalsmithAudioProcessor
) : DefaultRenderersFactory(context) {

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ): AudioSink {
        return DefaultAudioSink.Builder(context)
            .setAudioProcessorChain(
                SignalsmithAudioProcessorChain(vocalRemovalProcessor, signalsmithProcessor)
            )
            // Our custom processors currently support PCM_16BIT only.
            // For correctness, force 16-bit output from the sink.
            .setEnableFloatOutput(false)
            .setEnableAudioTrackPlaybackParams(false)
            .build()
    }
}
