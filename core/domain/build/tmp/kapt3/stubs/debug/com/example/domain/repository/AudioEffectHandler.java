package com.example.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u000b\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0019\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0003H&J\b\u0010\u0005\u001a\u00020\u0003H&J\b\u0010\u0006\u001a\u00020\u0003H&J\b\u0010\u0007\u001a\u00020\u0003H&J\b\u0010\b\u001a\u00020\u0003H&J\b\u0010\t\u001a\u00020\u0003H&J\b\u0010\n\u001a\u00020\u0003H&J\b\u0010\u000b\u001a\u00020\u0003H&J\b\u0010\f\u001a\u00020\u0003H&J\u0010\u0010\r\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH&J`\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u000f2\u0006\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u000f2\u0006\u0010\u001a\u001a\u00020\u000f2\u0006\u0010\u001b\u001a\u00020\u000f2\u0006\u0010\u001c\u001a\u00020\u000fH&J\u0018\u0010\u001d\u001a\u00020\u00032\u0006\u0010\u001e\u001a\u00020\u000f2\u0006\u0010\u001f\u001a\u00020\u000fH&J\u0010\u0010 \u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH&J\u0010\u0010!\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u0012H&J\u0010\u0010\"\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH&J\u0010\u0010#\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH&J\u0018\u0010$\u001a\u00020\u00032\u0006\u0010%\u001a\u00020\u000f2\u0006\u0010&\u001a\u00020\u000fH&J\u0010\u0010\'\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH&J\u0010\u0010(\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH&J\b\u0010)\u001a\u00020\u0003H&J\b\u0010*\u001a\u00020\u0003H&\u00a8\u0006+"}, d2 = {"Lcom/example/domain/repository/AudioEffectHandler;", "", "disableBassBoost", "", "disableEnvironmentReverb", "disableEqualizer", "disableLoudnessEnhancer", "disableReverb", "disableVirtualizer", "initPitchValue", "initTempoValue", "pitchMinusOne", "pitchPlusOne", "setBassBoost", "value", "", "setEnvironmentalReverb", "isEnabled", "", "roomLevel", "roomHFLevel", "decayTime", "decayHFRatio", "reflectionsLevel", "reflectionsDelay", "reverbLevel", "reverbDelay", "diffusion", "density", "setEqualizerWithCustomValue", "changedBand", "newGainLevel", "setEqualizerWithPreset", "setHapticGenerator", "setLoudnessEnhancer", "setPitch", "setPresetReverb", "presetIndex", "sendLevel", "setTempo", "setVirtualizer", "tempoMinusOne", "tempoPlusOne", "domain_debug"})
public abstract interface AudioEffectHandler {
    
    public abstract void setPitch(int value);
    
    public abstract void setTempo(int value);
    
    public abstract void initPitchValue();
    
    public abstract void initTempoValue();
    
    public abstract void pitchPlusOne();
    
    public abstract void pitchMinusOne();
    
    public abstract void tempoPlusOne();
    
    public abstract void tempoMinusOne();
    
    public abstract void setBassBoost(int value);
    
    public abstract void disableBassBoost();
    
    public abstract void setLoudnessEnhancer(int value);
    
    public abstract void disableLoudnessEnhancer();
    
    public abstract void setEqualizerWithPreset(int value);
    
    public abstract void setEqualizerWithCustomValue(int changedBand, int newGainLevel);
    
    public abstract void disableEqualizer();
    
    public abstract void setVirtualizer(int value);
    
    public abstract void disableVirtualizer();
    
    public abstract void setPresetReverb(int presetIndex, int sendLevel);
    
    public abstract void disableReverb();
    
    public abstract void setEnvironmentalReverb(boolean isEnabled, int roomLevel, int roomHFLevel, int decayTime, int decayHFRatio, int reflectionsLevel, int reflectionsDelay, int reverbLevel, int reverbDelay, int diffusion, int density);
    
    public abstract void disableEnvironmentReverb();
    
    public abstract void setHapticGenerator(boolean isEnabled);
}