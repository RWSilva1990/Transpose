package com.example.media;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0015\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/example/media/MediaSessionCallback;", "", "()V", "DISABLE_EQUALIZER", "", "DISABLE_REVERB", "GET_EQUALIZER_INFO", "INIT_PITCH_VALUE", "INIT_TEMPO_VALUE", "PITCH_MINUS", "PITCH_PLUS", "SET_BASS_BOOST", "SET_ENVIRONMENT_REVERB", "SET_EQUALIZER_CUSTOM", "SET_EQUALIZER_PRESET", "SET_HAPTIC_GENERATOR", "SET_LOUDNESS_ENHANCER", "SET_PITCH", "SET_REVERB", "SET_TEMPO", "SET_VIRTUALIZER", "TEMPO_MINUS", "TEMPO_PLUS", "UPDATE_METADATA", "prefix", "media_debug"})
public final class MediaSessionCallback {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String prefix = "transpose_";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String UPDATE_METADATA = "transpose_updateMetaData";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_PITCH = "transpose_setPitch";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_TEMPO = "transpose_setTempo";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GET_EQUALIZER_INFO = "transpose_setEqualizerInfo";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_EQUALIZER_PRESET = "transpose_setEqualizerPreset";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_EQUALIZER_CUSTOM = "transpose_setEqualizerCustom";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DISABLE_EQUALIZER = "transpose_disableEqualizer";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_BASS_BOOST = "transpose_setBassBoost";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_LOUDNESS_ENHANCER = "transpose_setLoudnessEnhancer";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_VIRTUALIZER = "transpose_setVirtualizer";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_REVERB = "transpose_setReverb";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DISABLE_REVERB = "transpose_disableReverb";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_ENVIRONMENT_REVERB = "transpose_setEnvironmentReverb";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String INIT_PITCH_VALUE = "transpose_initPitchValue";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PITCH_MINUS = "transpose_pitchMinus";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PITCH_PLUS = "transpose_pitchPlus";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String INIT_TEMPO_VALUE = "transpose_initTempoValue";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TEMPO_MINUS = "transpose_tempoMinus";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TEMPO_PLUS = "transpose_tempoPlus";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_HAPTIC_GENERATOR = "transpose_setHapticGenerator";
    @org.jetbrains.annotations.NotNull()
    public static final com.example.media.MediaSessionCallback INSTANCE = null;
    
    private MediaSessionCallback() {
        super();
    }
}