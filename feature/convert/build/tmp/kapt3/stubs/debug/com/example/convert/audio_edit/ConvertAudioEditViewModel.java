package com.example.convert.audio_edit;

import androidx.lifecycle.ViewModel;
import com.example.media.manager.AudioEffectsManager;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u001c\n\u0002\u0010\u0002\n\u0002\b\u0017\n\u0002\u0010\u0007\n\u0002\b\f\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u00104\u001a\u000205J\u0006\u00106\u001a\u000205J\u0006\u00107\u001a\u000205J\u0006\u00108\u001a\u000205J\u0006\u00109\u001a\u000205J\u0006\u0010:\u001a\u000205J\u0006\u0010;\u001a\u000205J\u0006\u0010<\u001a\u000205J\u0006\u0010=\u001a\u000205J\u0006\u0010>\u001a\u000205J\u0006\u0010?\u001a\u000205J\u0006\u0010@\u001a\u000205J\u000e\u0010A\u001a\u0002052\u0006\u0010B\u001a\u00020\u0007J\u0006\u0010C\u001a\u000205J\u0006\u0010D\u001a\u000205J\u0006\u0010E\u001a\u000205J\u0006\u0010F\u001a\u000205J\u0006\u0010G\u001a\u000205J\u0006\u0010H\u001a\u000205J\u0006\u0010I\u001a\u000205J\u000e\u0010J\u001a\u0002052\u0006\u0010K\u001a\u00020\u0007J\u0016\u0010L\u001a\u0002052\u0006\u0010B\u001a\u00020\u00072\u0006\u0010K\u001a\u00020MJ\u000e\u0010N\u001a\u0002052\u0006\u0010O\u001a\u00020\u0007J\u0006\u0010P\u001a\u000205J\u0006\u0010Q\u001a\u000205J\u0006\u0010R\u001a\u000205J\u000e\u0010S\u001a\u0002052\u0006\u0010K\u001a\u00020\u0007J\u000e\u0010T\u001a\u0002052\u0006\u0010K\u001a\u00020\u0007J\u000e\u0010U\u001a\u0002052\u0006\u0010O\u001a\u00020\u0007J\u000e\u0010V\u001a\u0002052\u0006\u0010K\u001a\u00020\u0007J\u000e\u0010W\u001a\u0002052\u0006\u0010K\u001a\u00020\u0007J\u000e\u0010X\u001a\u0002052\u0006\u0010K\u001a\u00020\u0007R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\tR\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\tR\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\tR\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\tR\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\tR\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\tR\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\tR\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00180\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\tR\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00180\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\tR\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00180\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\tR\u0017\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\tR\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\tR\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\tR\u0017\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\tR\u0017\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\tR\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\tR\u0017\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\tR\u0017\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\tR\u0017\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\tR\u0017\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010\tR\u0017\u00100\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010\tR\u0017\u00102\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010\t\u00a8\u0006Y"}, d2 = {"Lcom/example/convert/audio_edit/ConvertAudioEditViewModel;", "Landroidx/lifecycle/ViewModel;", "audioEffectsManager", "Lcom/example/media/manager/AudioEffectsManager;", "(Lcom/example/media/manager/AudioEffectsManager;)V", "bassBoostValue", "Lkotlinx/coroutines/flow/StateFlow;", "", "getBassBoostValue", "()Lkotlinx/coroutines/flow/StateFlow;", "decayHFRatio", "getDecayHFRatio", "decayTime", "getDecayTime", "density", "getDensity", "diffusion", "getDiffusion", "equalizerCurrentPreset", "getEqualizerCurrentPreset", "equalizerSettings", "Lcom/example/media/audio_effect/data/equalizer/EqualizerSettings;", "getEqualizerSettings", "isEnvironmentalReverbEnabled", "", "isEqualizerEnabled", "isHapticGeneratorEnabled", "isReverbEnabled", "loudnessEnhancerValue", "getLoudnessEnhancerValue", "pitchValue", "getPitchValue", "reflectionsDelay", "getReflectionsDelay", "reflectionsLevel", "getReflectionsLevel", "reverbCurrentPreset", "getReverbCurrentPreset", "reverbDelay", "getReverbDelay", "reverbLevel", "getReverbLevel", "reverbValue", "getReverbValue", "roomHFLevel", "getRoomHFLevel", "roomLevel", "getRoomLevel", "tempoValue", "getTempoValue", "virtualizerValue", "getVirtualizerValue", "disableEqualizer", "", "disableReverb", "initBassBoostValue", "initEqualizerValue", "initLoudnessEnhancerValue", "initPitchValue", "initReverbValue", "initTempoValue", "initVirtualizerValue", "pitchMinusOne", "pitchPlusOne", "setBassBoost", "setEqualizerWithCustomValue", "bandIndex", "setLoudnessEnhancer", "setPitch", "setPresetReverb", "setTempo", "setVirtualizer", "tempoMinusOne", "tempoPlusOne", "updateBassBoostValue", "value", "updateEqualizerBand", "", "updateEqualizerWithPreset", "presetIndex", "updateHapticGeneratorValue", "updateIsEqualizerEnabled", "updateIsReverbEnabled", "updateLoudnessEnhancerValue", "updatePitchValue", "updateReverbPreset", "updateReverbValue", "updateTempoValue", "updateVirtualizerValue", "convert_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ConvertAudioEditViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.media.manager.AudioEffectsManager audioEffectsManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> pitchValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> tempoValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isEqualizerEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> equalizerCurrentPreset = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.media.audio_effect.data.equalizer.EqualizerSettings> equalizerSettings = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isReverbEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reverbCurrentPreset = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reverbValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> bassBoostValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> loudnessEnhancerValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> virtualizerValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isHapticGeneratorEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isEnvironmentalReverbEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> roomLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> roomHFLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> decayTime = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> decayHFRatio = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reflectionsLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reflectionsDelay = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reverbLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> reverbDelay = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> diffusion = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> density = null;
    
    @javax.inject.Inject()
    public ConvertAudioEditViewModel(@org.jetbrains.annotations.NotNull()
    com.example.media.manager.AudioEffectsManager audioEffectsManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getPitchValue() {
        return null;
    }
    
    public final void updatePitchValue(int value) {
    }
    
    public final void setPitch() {
    }
    
    public final void pitchPlusOne() {
    }
    
    public final void pitchMinusOne() {
    }
    
    public final void initPitchValue() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getTempoValue() {
        return null;
    }
    
    public final void updateTempoValue(int value) {
    }
    
    public final void setTempo() {
    }
    
    public final void tempoPlusOne() {
    }
    
    public final void tempoMinusOne() {
    }
    
    public final void initTempoValue() {
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
    
    public final void updateEqualizerWithPreset(int presetIndex) {
    }
    
    public final void setEqualizerWithCustomValue(int bandIndex) {
    }
    
    public final void updateEqualizerBand(int bandIndex, float value) {
    }
    
    public final void initEqualizerValue() {
    }
    
    public final void disableEqualizer() {
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
    
    public final void initReverbValue() {
    }
    
    public final void updateIsReverbEnabled() {
    }
    
    public final void updateReverbValue(int value) {
    }
    
    public final void setPresetReverb() {
    }
    
    public final void updateReverbPreset(int presetIndex) {
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
    
    public final void updateHapticGeneratorValue() {
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
}