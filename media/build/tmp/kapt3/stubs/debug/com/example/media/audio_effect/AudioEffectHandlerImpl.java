package com.example.media.audio_effect;

import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.DynamicsProcessing;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.media.audiofx.HapticGenerator;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.os.Build;
import androidx.annotation.OptIn;
import androidx.media3.common.AuxEffectInfo;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import com.example.domain.repository.AudioEffectHandler;
import com.example.util.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0019\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0019\u001a\u00020\u001aH\u0016J\b\u0010\u001b\u001a\u00020\u001aH\u0016J\b\u0010\u001c\u001a\u00020\u001aH\u0016J\b\u0010\u001d\u001a\u00020\u001aH\u0016J\b\u0010\u001e\u001a\u00020\u001aH\u0016J\b\u0010\u001f\u001a\u00020\u001aH\u0016J\b\u0010 \u001a\u00020\u001aH\u0016J\b\u0010!\u001a\u00020\u001aH\u0016J\b\u0010\"\u001a\u00020\u001aH\u0016J\b\u0010#\u001a\u00020\u001aH\u0016J\u0010\u0010$\u001a\u00020\u001a2\u0006\u0010%\u001a\u00020\u0006H\u0016J`\u0010&\u001a\u00020\u001a2\u0006\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020\u00062\u0006\u0010*\u001a\u00020\u00062\u0006\u0010+\u001a\u00020\u00062\u0006\u0010,\u001a\u00020\u00062\u0006\u0010-\u001a\u00020\u00062\u0006\u0010.\u001a\u00020\u00062\u0006\u0010/\u001a\u00020\u00062\u0006\u00100\u001a\u00020\u00062\u0006\u00101\u001a\u00020\u00062\u0006\u00102\u001a\u00020\u0006H\u0016J\u0018\u00103\u001a\u00020\u001a2\u0006\u00104\u001a\u00020\u00062\u0006\u00105\u001a\u00020\u0006H\u0016J\u0010\u00106\u001a\u00020\u001a2\u0006\u0010%\u001a\u00020\u0006H\u0016J\u0010\u00107\u001a\u00020\u001a2\u0006\u0010\'\u001a\u00020(H\u0016J\u0010\u00108\u001a\u00020\u001a2\u0006\u0010%\u001a\u00020\u0006H\u0016J\u0010\u00109\u001a\u00020\u001a2\u0006\u0010%\u001a\u00020\u0006H\u0016J\u0018\u0010:\u001a\u00020\u001a2\u0006\u0010;\u001a\u00020\u00062\u0006\u0010<\u001a\u00020\u0006H\u0016J\u0010\u0010=\u001a\u00020\u001a2\u0006\u0010%\u001a\u00020\u0006H\u0016J\u0010\u0010>\u001a\u00020\u001a2\u0006\u0010%\u001a\u00020\u0006H\u0016J\b\u0010?\u001a\u00020\u001aH\u0016J\b\u0010@\u001a\u00020\u001aH\u0016R\u0014\u0010\u0005\u001a\u00020\u00068BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\bR\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006A"}, d2 = {"Lcom/example/media/audio_effect/AudioEffectHandlerImpl;", "Lcom/example/domain/repository/AudioEffectHandler;", "exoPlayer", "Landroidx/media3/exoplayer/ExoPlayer;", "(Landroidx/media3/exoplayer/ExoPlayer;)V", "audioSessionId", "", "getAudioSessionId", "()I", "bassBoost", "Landroid/media/audiofx/BassBoost;", "dynamicsProcessing", "Landroid/media/audiofx/DynamicsProcessing;", "environmentalReverb", "Landroid/media/audiofx/EnvironmentalReverb;", "equalizer", "Landroid/media/audiofx/Equalizer;", "hapticGenerator", "Landroid/media/audiofx/HapticGenerator;", "loudnessEnhancer", "Landroid/media/audiofx/LoudnessEnhancer;", "presetReverb", "Landroid/media/audiofx/PresetReverb;", "virtualizer", "Landroid/media/audiofx/Virtualizer;", "disableBassBoost", "", "disableEnvironmentReverb", "disableEqualizer", "disableLoudnessEnhancer", "disableReverb", "disableVirtualizer", "initPitchValue", "initTempoValue", "pitchMinusOne", "pitchPlusOne", "setBassBoost", "value", "setEnvironmentalReverb", "isEnabled", "", "roomLevel", "roomHFLevel", "decayTime", "decayHFRatio", "reflectionsLevel", "reflectionsDelay", "reverbLevel", "reverbDelay", "diffusion", "density", "setEqualizerWithCustomValue", "changedBand", "newGainLevel", "setEqualizerWithPreset", "setHapticGenerator", "setLoudnessEnhancer", "setPitch", "setPresetReverb", "presetIndex", "sendLevel", "setTempo", "setVirtualizer", "tempoMinusOne", "tempoPlusOne", "media_debug"})
@androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
public final class AudioEffectHandlerImpl implements com.example.domain.repository.AudioEffectHandler {
    @org.jetbrains.annotations.NotNull()
    private final androidx.media3.exoplayer.ExoPlayer exoPlayer = null;
    @org.jetbrains.annotations.Nullable()
    private android.media.audiofx.Equalizer equalizer;
    @org.jetbrains.annotations.Nullable()
    private android.media.audiofx.LoudnessEnhancer loudnessEnhancer;
    @org.jetbrains.annotations.Nullable()
    private android.media.audiofx.BassBoost bassBoost;
    @org.jetbrains.annotations.Nullable()
    private android.media.audiofx.Virtualizer virtualizer;
    @org.jetbrains.annotations.Nullable()
    private android.media.audiofx.PresetReverb presetReverb;
    @org.jetbrains.annotations.Nullable()
    private android.media.audiofx.EnvironmentalReverb environmentalReverb;
    @org.jetbrains.annotations.Nullable()
    private android.media.audiofx.DynamicsProcessing dynamicsProcessing;
    @org.jetbrains.annotations.Nullable()
    private android.media.audiofx.HapticGenerator hapticGenerator;
    
    @javax.inject.Inject()
    public AudioEffectHandlerImpl(@org.jetbrains.annotations.NotNull()
    androidx.media3.exoplayer.ExoPlayer exoPlayer) {
        super();
    }
    
    private final int getAudioSessionId() {
        return 0;
    }
    
    @java.lang.Override()
    public void setPitch(int value) {
    }
    
    @java.lang.Override()
    public void setTempo(int value) {
    }
    
    @java.lang.Override()
    public void pitchPlusOne() {
    }
    
    @java.lang.Override()
    public void initPitchValue() {
    }
    
    @java.lang.Override()
    public void pitchMinusOne() {
    }
    
    @java.lang.Override()
    public void tempoPlusOne() {
    }
    
    @java.lang.Override()
    public void initTempoValue() {
    }
    
    @java.lang.Override()
    public void tempoMinusOne() {
    }
    
    @java.lang.Override()
    public void setBassBoost(int value) {
    }
    
    @java.lang.Override()
    public void disableBassBoost() {
    }
    
    @java.lang.Override()
    public void setLoudnessEnhancer(int value) {
    }
    
    @java.lang.Override()
    public void disableLoudnessEnhancer() {
    }
    
    @java.lang.Override()
    public void setEqualizerWithPreset(int value) {
    }
    
    @java.lang.Override()
    public void setEqualizerWithCustomValue(int changedBand, int newGainLevel) {
    }
    
    @java.lang.Override()
    public void disableEqualizer() {
    }
    
    @java.lang.Override()
    public void setVirtualizer(int value) {
    }
    
    @java.lang.Override()
    public void disableVirtualizer() {
    }
    
    @java.lang.Override()
    public void setPresetReverb(int presetIndex, int sendLevel) {
    }
    
    @java.lang.Override()
    public void disableReverb() {
    }
    
    @java.lang.Override()
    public void setEnvironmentalReverb(boolean isEnabled, int roomLevel, int roomHFLevel, int decayTime, int decayHFRatio, int reflectionsLevel, int reflectionsDelay, int reverbLevel, int reverbDelay, int diffusion, int density) {
    }
    
    @java.lang.Override()
    public void disableEnvironmentReverb() {
    }
    
    @java.lang.Override()
    public void setHapticGenerator(boolean isEnabled) {
    }
}