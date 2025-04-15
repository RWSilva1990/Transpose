package com.example.media.manager;

import android.os.Bundle;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionCommand;
import com.example.media.MediaSessionCallback;
import com.example.media.audio_effect.data.equalizer.EqualizerPresets;
import com.example.media.audio_effect.data.equalizer.EqualizerSettings;
import com.example.media.audio_effect.data.reverb.ReverbPresets;
import com.example.util.Logger;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0018\u0002\n\u0002\b\u0019\n\u0002\u0010\u0002\n\u0002\b\u0019\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0017\n\u0002\u0010\u0007\n\u0002\b\u0013\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010P\u001a\u00020QJ\u0006\u0010R\u001a\u00020QJ\u0006\u0010S\u001a\u00020QJ\u0006\u0010T\u001a\u00020QJ\u0006\u0010U\u001a\u00020QJ\u0006\u0010V\u001a\u00020QJ\u0006\u0010W\u001a\u00020QJ\u0006\u0010X\u001a\u00020QJ\u0006\u0010Y\u001a\u00020QJ\u0006\u0010Z\u001a\u00020QJ\u0006\u0010[\u001a\u00020QJ\u0006\u0010\\\u001a\u00020QJ\u0006\u0010]\u001a\u00020QJ\u0006\u0010^\u001a\u00020QJ\u0006\u0010_\u001a\u00020QJ\u0006\u0010`\u001a\u00020QJ\u0006\u0010a\u001a\u00020QJ\u0006\u0010b\u001a\u00020QJ\u0006\u0010c\u001a\u00020QJ\u0006\u0010d\u001a\u00020QJ\u0006\u0010e\u001a\u00020QJ\u0006\u0010f\u001a\u00020QJ\u0006\u0010g\u001a\u00020QJ\u0006\u0010h\u001a\u00020QJ\u0010\u0010i\u001a\u00020Q2\u0006\u0010j\u001a\u00020kH\u0002J\u0018\u0010l\u001a\u00020Q2\u0006\u0010j\u001a\u00020k2\u0006\u0010m\u001a\u00020nH\u0002J\u0006\u0010o\u001a\u00020QJ\u0006\u0010p\u001a\u00020QJ\u000e\u0010q\u001a\u00020Q2\u0006\u0010r\u001a\u00020\u0007J\b\u0010s\u001a\u00020QH\u0002J\u0010\u0010t\u001a\u00020Q2\u0006\u0010u\u001a\u00020\u0010H\u0002J\u0006\u0010v\u001a\u00020QJ\u0006\u0010w\u001a\u00020QJ\u0006\u0010x\u001a\u00020QJ\u0006\u0010y\u001a\u00020QJ\u0006\u0010z\u001a\u00020QJ\u0006\u0010{\u001a\u00020QJ\u0006\u0010|\u001a\u00020QJ\u000e\u0010}\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000e\u0010\u007f\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u0080\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u0081\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u0082\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u001a\u0010\u0083\u0001\u001a\u00020Q2\u0007\u0010\u0084\u0001\u001a\u00020\u00072\b\u0010\u0085\u0001\u001a\u00030\u0086\u0001J\u0010\u0010\u0087\u0001\u001a\u00020Q2\u0007\u0010\u0088\u0001\u001a\u00020\u0007J\u000f\u0010\u0089\u0001\u001a\u00020Q2\u0006\u0010u\u001a\u00020\u0010J\u0007\u0010\u008a\u0001\u001a\u00020QJ\u0007\u0010\u008b\u0001\u001a\u00020QJ\u0007\u0010\u008c\u0001\u001a\u00020QJ\u000f\u0010\u008d\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u008e\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u008f\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u0090\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u0010\u0010\u0091\u0001\u001a\u00020Q2\u0007\u0010\u0088\u0001\u001a\u00020\u0007J\u000f\u0010\u0092\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u0093\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u0094\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u0095\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u0096\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u0097\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007J\u000f\u0010\u0098\u0001\u001a\u00020Q2\u0006\u0010~\u001a\u00020\u0007R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00100\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00100\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00100\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010#R\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010#R\u0017\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010#R\u0017\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010#R\u0017\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010#R\u0017\u0010.\u001a\b\u0012\u0004\u0012\u00020\u000e0!\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010#R\u0017\u00100\u001a\b\u0012\u0004\u0012\u00020\u00100!\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010#R\u0017\u00101\u001a\b\u0012\u0004\u0012\u00020\u00100!\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010#R\u0017\u00102\u001a\b\u0012\u0004\u0012\u00020\u00100!\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010#R\u0017\u00103\u001a\b\u0012\u0004\u0012\u00020\u00100!\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010#R\u0017\u00104\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010#R\u0016\u00106\u001a\u0004\u0018\u0001078BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b8\u00109R\u0017\u0010:\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010#R\u0017\u0010<\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010#R\u0017\u0010>\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u0010#R\u0017\u0010@\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u0010#R\u0017\u0010B\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010#R\u0017\u0010D\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010#R\u0017\u0010F\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u0010#R\u0017\u0010H\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\bI\u0010#R\u0017\u0010J\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u0010#R\u0017\u0010L\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\bM\u0010#R\u0017\u0010N\u001a\b\u0012\u0004\u0012\u00020\u00070!\u00a2\u0006\b\n\u0000\u001a\u0004\bO\u0010#\u00a8\u0006\u0099\u0001"}, d2 = {"Lcom/example/media/manager/AudioEffectsManager;", "", "controllerProvider", "Lcom/example/media/manager/MediaControllerProvider;", "(Lcom/example/media/manager/MediaControllerProvider;)V", "_bassBoostValue", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_decayHFRatio", "_decayTime", "_density", "_diffusion", "_equalizerCurrentPreset", "_equalizerSettings", "Lcom/example/media/audio_effect/data/equalizer/EqualizerSettings;", "_isEnvironmentalReverbEnabled", "", "_isEqualizerEnabled", "_isHapticGeneratorEnabled", "_isReverbEnabled", "_loudnessEnhancerValue", "_pitchValue", "_reflectionsDelay", "_reflectionsLevel", "_reverbCurrentPreset", "_reverbDelay", "_reverbLevel", "_reverbValue", "_roomHFLevel", "_roomLevel", "_tempoValue", "_virtualizerValue", "bassBoostValue", "Lkotlinx/coroutines/flow/StateFlow;", "getBassBoostValue", "()Lkotlinx/coroutines/flow/StateFlow;", "decayHFRatio", "getDecayHFRatio", "decayTime", "getDecayTime", "density", "getDensity", "diffusion", "getDiffusion", "equalizerCurrentPreset", "getEqualizerCurrentPreset", "equalizerSettings", "getEqualizerSettings", "isEnvironmentalReverbEnabled", "isEqualizerEnabled", "isHapticGeneratorEnabled", "isReverbEnabled", "loudnessEnhancerValue", "getLoudnessEnhancerValue", "mediaController", "Landroidx/media3/session/MediaController;", "getMediaController", "()Landroidx/media3/session/MediaController;", "pitchValue", "getPitchValue", "reflectionsDelay", "getReflectionsDelay", "reflectionsLevel", "getReflectionsLevel", "reverbCurrentPreset", "getReverbCurrentPreset", "reverbDelay", "getReverbDelay", "reverbLevel", "getReverbLevel", "reverbValue", "getReverbValue", "roomHFLevel", "getRoomHFLevel", "roomLevel", "getRoomLevel", "tempoValue", "getTempoValue", "virtualizerValue", "getVirtualizerValue", "disableEnvironmentalReverb", "", "disableEqualizer", "disableReverb", "initBassBoostValue", "initDecayHFRatio", "initDecayTime", "initDensity", "initDiffusion", "initEnvironmentalReverbValues", "initEqualizerValue", "initLoudnessEnhancerValue", "initPitchValue", "initReflectionsDelay", "initReflectionsLevel", "initReverbDelay", "initReverbLevel", "initReverbValue", "initRoomHFLevel", "initRoomLevel", "initTempoValue", "initVirtualizerValue", "pitchMinusOne", "pitchPlusOne", "release", "sendSessionAction", "action", "", "sendSessionCommand", "bundle", "Landroid/os/Bundle;", "setBassBoost", "setEnvironmentalReverb", "setEqualizerWithCustomValue", "changedBand", "setEqualizerWithPreset", "setHapticGenerator", "isEnabled", "setLoudnessEnhancer", "setPitch", "setPresetReverb", "setTempo", "setVirtualizer", "tempoMinusOne", "tempoPlusOne", "updateBassBoostValue", "value", "updateDecayHFRatio", "updateDecayTime", "updateDensity", "updateDiffusion", "updateEqualizerBandLevel", "index", "newValue", "", "updateEqualizerWithPreset", "presetIndex", "updateIsEnvironmentalReverbEnabled", "updateIsEqualizerEnabled", "updateIsHapticGeneratorEnabled", "updateIsReverbEnabled", "updateLoudnessEnhancerValue", "updatePitchValue", "updateReflectionsDelay", "updateReflectionsLevel", "updateReverbCurrentPreset", "updateReverbDelay", "updateReverbLevel", "updateReverbValue", "updateRoomHFLevel", "updateRoomLevel", "updateTempoValue", "updateVirtualizerValue", "media_debug"})
public final class AudioEffectsManager {
    @org.jetbrains.annotations.NotNull()
    private final com.example.media.manager.MediaControllerProvider controllerProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _pitchValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> pitchValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _tempoValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> tempoValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isEqualizerEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isEqualizerEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _equalizerCurrentPreset = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> equalizerCurrentPreset = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.media.audio_effect.data.equalizer.EqualizerSettings> _equalizerSettings = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.media.audio_effect.data.equalizer.EqualizerSettings> equalizerSettings = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isReverbEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isReverbEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _reverbCurrentPreset = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reverbCurrentPreset = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _reverbValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reverbValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _bassBoostValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> bassBoostValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _loudnessEnhancerValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> loudnessEnhancerValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _virtualizerValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> virtualizerValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isHapticGeneratorEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isHapticGeneratorEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isEnvironmentalReverbEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isEnvironmentalReverbEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _roomLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> roomLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _roomHFLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> roomHFLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _decayTime = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> decayTime = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _decayHFRatio = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> decayHFRatio = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _reflectionsLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reflectionsLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _reflectionsDelay = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reflectionsDelay = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _reverbLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reverbLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _reverbDelay = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reverbDelay = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _diffusion = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> diffusion = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _density = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> density = null;
    
    @javax.inject.Inject()
    public AudioEffectsManager(@org.jetbrains.annotations.NotNull()
    com.example.media.manager.MediaControllerProvider controllerProvider) {
        super();
    }
    
    private final androidx.media3.session.MediaController getMediaController() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getPitchValue() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getTempoValue() {
        return null;
    }
    
    public final void updatePitchValue(int value) {
    }
    
    public final void setPitch() {
    }
    
    public final void initPitchValue() {
    }
    
    public final void pitchPlusOne() {
    }
    
    public final void pitchMinusOne() {
    }
    
    public final void setTempo() {
    }
    
    public final void updateTempoValue(int value) {
    }
    
    public final void initTempoValue() {
    }
    
    public final void tempoPlusOne() {
    }
    
    public final void tempoMinusOne() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isEqualizerEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getEqualizerCurrentPreset() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.media.audio_effect.data.equalizer.EqualizerSettings> getEqualizerSettings() {
        return null;
    }
    
    public final void updateIsEqualizerEnabled() {
    }
    
    public final void initEqualizerValue() {
    }
    
    public final void updateEqualizerWithPreset(int presetIndex) {
    }
    
    private final void setEqualizerWithPreset() {
    }
    
    public final void disableEqualizer() {
    }
    
    public final void setEqualizerWithCustomValue(int changedBand) {
    }
    
    public final void updateEqualizerBandLevel(int index, float newValue) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isReverbEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getReverbCurrentPreset() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getReverbValue() {
        return null;
    }
    
    public final void updateIsReverbEnabled() {
    }
    
    public final void updateReverbCurrentPreset(int presetIndex) {
    }
    
    public final void initReverbValue() {
    }
    
    public final void updateReverbValue(int value) {
    }
    
    public final void setPresetReverb() {
    }
    
    public final void disableReverb() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getBassBoostValue() {
        return null;
    }
    
    public final void updateBassBoostValue(int value) {
    }
    
    public final void setBassBoost() {
    }
    
    public final void initBassBoostValue() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getLoudnessEnhancerValue() {
        return null;
    }
    
    public final void updateLoudnessEnhancerValue(int value) {
    }
    
    public final void setLoudnessEnhancer() {
    }
    
    public final void initLoudnessEnhancerValue() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getVirtualizerValue() {
        return null;
    }
    
    public final void updateVirtualizerValue(int value) {
    }
    
    public final void setVirtualizer() {
    }
    
    public final void initVirtualizerValue() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isHapticGeneratorEnabled() {
        return null;
    }
    
    public final void updateIsHapticGeneratorEnabled() {
    }
    
    private final void setHapticGenerator(boolean isEnabled) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isEnvironmentalReverbEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getRoomLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getRoomHFLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getDecayTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getDecayHFRatio() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getReflectionsLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getReflectionsDelay() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getReverbLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getReverbDelay() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getDiffusion() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getDensity() {
        return null;
    }
    
    public final void updateIsEnvironmentalReverbEnabled(boolean isEnabled) {
    }
    
    public final void updateRoomLevel(int value) {
    }
    
    public final void updateRoomHFLevel(int value) {
    }
    
    public final void updateDecayTime(int value) {
    }
    
    public final void updateDecayHFRatio(int value) {
    }
    
    public final void updateReflectionsLevel(int value) {
    }
    
    public final void updateReflectionsDelay(int value) {
    }
    
    public final void updateReverbLevel(int value) {
    }
    
    public final void updateReverbDelay(int value) {
    }
    
    public final void updateDiffusion(int value) {
    }
    
    public final void updateDensity(int value) {
    }
    
    public final void initEnvironmentalReverbValues() {
    }
    
    public final void initRoomLevel() {
    }
    
    public final void initRoomHFLevel() {
    }
    
    public final void initDecayTime() {
    }
    
    public final void initDecayHFRatio() {
    }
    
    public final void initReflectionsLevel() {
    }
    
    public final void initReflectionsDelay() {
    }
    
    public final void initReverbLevel() {
    }
    
    public final void initReverbDelay() {
    }
    
    public final void initDiffusion() {
    }
    
    public final void initDensity() {
    }
    
    public final void setEnvironmentalReverb() {
    }
    
    public final void disableEnvironmentalReverb() {
    }
    
    private final void sendSessionAction(java.lang.String action) {
    }
    
    private final void sendSessionCommand(java.lang.String action, android.os.Bundle bundle) {
    }
    
    public final void release() {
    }
}