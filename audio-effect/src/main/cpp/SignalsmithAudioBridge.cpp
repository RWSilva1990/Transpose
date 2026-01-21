#include <jni.h>
#include <string>
#include <android/log.h>
#include <mutex>
#include <atomic>
#include <cmath>
#include <vector>
#include <thread>
#include <condition_variable>

// Signalsmith Stretch
#include "signalsmith/signalsmith-stretch.h"

// Signalsmith Basics Effects
#include "signalsmith-basics/chorus.h"
#include "signalsmith-basics/limiter.h"
#include "signalsmith-basics/reverb.h"
#include "signalsmith-basics/crunch.h"
#include "signalsmith/basics/modules/dsp/filters.h"
#include "signalsmith/basics/modules/dsp/envelopes.h"

#include "mit_hrtf_lib.h"
#include "FFTConvolver.h"

#define LOG_TAG "SignalsmithNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

// ============================================================================
// Lock-free SPSC Ring Buffer for PCM data
// ============================================================================
class SpscRingBuffer {
public:
    SpscRingBuffer(size_t capacity) : capacity_(capacity), head_(0), tail_(0) {
        buffer_ = new float[capacity];
        memset(buffer_, 0, capacity * sizeof(float));
    }

    ~SpscRingBuffer() {
        delete[] buffer_;
    }

    size_t write(const float* data, size_t samples) {
        size_t head = head_.load(std::memory_order_relaxed);
        size_t tail = tail_.load(std::memory_order_acquire);
        
        size_t available = capacity_ - (head - tail);
        size_t toWrite = std::min(samples, available);
        
        for (size_t i = 0; i < toWrite; i++) {
            buffer_[(head + i) % capacity_] = data[i];
        }
        
        head_.store(head + toWrite, std::memory_order_release);
        return toWrite;
    }

    size_t read(float* data, size_t samples) {
        size_t head = head_.load(std::memory_order_acquire);
        size_t tail = tail_.load(std::memory_order_relaxed);
        
        size_t available = head - tail;
        size_t toRead = std::min(samples, available);
        
        for (size_t i = 0; i < toRead; i++) {
            data[i] = buffer_[(tail + i) % capacity_];
        }
        
        // Zero-pad if not enough data
        for (size_t i = toRead; i < samples; i++) {
            data[i] = 0.0f;
        }
        
        tail_.store(tail + toRead, std::memory_order_release);
        return toRead;
    }

    size_t available() const {
        size_t head = head_.load(std::memory_order_acquire);
        size_t tail = tail_.load(std::memory_order_relaxed);
        return head - tail;
    }

    size_t space() const {
        size_t head = head_.load(std::memory_order_relaxed);
        size_t tail = tail_.load(std::memory_order_acquire);
        return capacity_ - (head - tail);
    }

    void clear() {
        head_.store(0, std::memory_order_relaxed);
        tail_.store(0, std::memory_order_relaxed);
    }

private:
    float* buffer_;
    size_t capacity_;
    std::atomic<size_t> head_;
    std::atomic<size_t> tail_;
};

// ============================================================================
// Global state
// ============================================================================
static signalsmith::stretch::SignalsmithStretch<float>* stretch = nullptr;
static SpscRingBuffer* inputBuffer = nullptr;

// Scratch buffers for processing
static std::vector<float> inputLeft, inputRight;
static std::vector<float> outputLeft, outputRight;
static float* inputPtrs[2] = {nullptr, nullptr};
static float* outputPtrs[2] = {nullptr, nullptr};

// Audio parameters
static std::atomic<bool> isPlaying(false);
static std::atomic<bool> isInitialized(false);
static std::atomic<float> currentPitchSemitones(0.0f);
static std::atomic<float> currentTempo(1.0f);

static signalsmith::basics::ChorusFloat* chorusEffect = nullptr;
static signalsmith::basics::LimiterFloat* limiterEffect = nullptr;
static signalsmith::basics::ReverbFloat* reverbEffect = nullptr;
static signalsmith::basics::CrunchFloat* crunchEffect = nullptr;
static std::vector<float*> effectInputPtrs, effectOutputPtrs;
static std::vector<float> effectInputL, effectInputR, effectOutputL, effectOutputR;

// Effects enabled flags
static std::atomic<bool> chorusEnabled(false);
static std::atomic<bool> limiterEnabled(false);
static std::atomic<bool> reverbEnabled(false);
static std::atomic<bool> crunchEnabled(false);

// Effects parameters
static std::atomic<float> chorusMix(0.5f);
static std::atomic<float> chorusDepthMs(10.0f);
static std::atomic<float> chorusDetune(10.0f);
static std::atomic<float> chorusStereo(0.5f);

static std::atomic<float> limiterInputGainDb(0.0f);
static std::atomic<float> limiterLimitDb(-3.0f);
static std::atomic<float> limiterAttackMs(10.0f);
static std::atomic<float> limiterReleaseMs(100.0f);

static std::atomic<float> reverbDry(1.0f);
static std::atomic<float> reverbWet(0.3f);
static std::atomic<float> reverbRoomMs(50.0f);
static std::atomic<float> reverbDecaySec(2.0f);

static std::atomic<float> crunchDriveDb(0.0f);
static std::atomic<float> crunchFuzz(0.0f);
static std::atomic<float> crunchToneHz(5000.0f);

static std::atomic<bool> eqEnabled(false);
static std::atomic<float> eqBand1Freq(60.0f);
static std::atomic<float> eqBand1Gain(0.0f);
static std::atomic<float> eqBand2Freq(250.0f);
static std::atomic<float> eqBand2Gain(0.0f);
static std::atomic<float> eqBand3Freq(1000.0f);
static std::atomic<float> eqBand3Gain(0.0f);
static std::atomic<float> eqBand4Freq(4000.0f);
static std::atomic<float> eqBand4Gain(0.0f);
static std::atomic<float> eqBand5Freq(12000.0f);
static std::atomic<float> eqBand5Gain(0.0f);

static std::atomic<bool> compressorEnabled(false);
static std::atomic<float> compThresholdDb(-20.0f);
static std::atomic<float> compRatio(4.0f);
static std::atomic<float> compAttackMs(10.0f);
static std::atomic<float> compReleaseMs(100.0f);
static std::atomic<float> compMakeupGainDb(0.0f);

static std::atomic<float> detectedPitch(0.0f);
static std::atomic<bool> pitchDetectionEnabled(false);

// MIT HRTF Virtualizer
static std::atomic<bool> hrtfEnabled(false);
static std::atomic<float> hrtfIntensity(1.0f);
static std::atomic<int> hrtfAzimuth(30);

static fftconvolver::FFTConvolver hrtfConvolverL;
static fftconvolver::FFTConvolver hrtfConvolverR;
static bool hrtfInitialized = false;
static int hrtfCurrentAzimuth = 0;
static std::vector<float> hrtfTempBufferL, hrtfTempBufferR;

using BiquadFilter = signalsmith::filters::BiquadStatic<float>;

static std::vector<BiquadFilter> eqFiltersL(5);
static std::vector<BiquadFilter> eqFiltersR(5);

static float compEnvelopeL = 0.0f;
static float compEnvelopeR = 0.0f;

static std::vector<float> effectsBufferLeft, effectsBufferRight;

// Position tracking
static std::atomic<int64_t> totalOutputFrames(0);
static std::atomic<int64_t> seekPositionUs(0);
static std::atomic<int64_t> totalInputFramesConsumed(0);

// Stats
static std::atomic<int> underrunCount(0);

// Configuration
static int sampleRate = 44100;
static int channelCount = 2;
static const size_t BUFFER_SIZE_SAMPLES = 44100 * 2 * 10; // 10 seconds stereo
static const int PROCESS_BLOCK_FRAMES = 512;

// Tempo accumulator for fractional frame handling
static double tempoAccumulator = 0.0;

// JNI callback references for AudioTrack
static JavaVM* javaVM = nullptr;
static jobject audioTrackCallback = nullptr;
static jmethodID writeAudioMethod = nullptr;

// ============================================================================
// Helper functions
// ============================================================================

static void shortToFloat(const short* src, float* dst, int samples) {
    const float scale = 1.0f / 32768.0f;
    for (int i = 0; i < samples; i++) {
        dst[i] = src[i] * scale;
    }
}

static void floatToShort(const float* src, short* dst, int samples) {
    for (int i = 0; i < samples; i++) {
        float v = src[i] * 32768.0f;
        if (v > 32767.0f) v = 32767.0f;
        if (v < -32768.0f) v = -32768.0f;
        dst[i] = (short)v;
    }
}

static void deinterleave(const float* interleaved, float* left, float* right, int frames) {
    for (int i = 0; i < frames; i++) {
        left[i] = interleaved[i * 2];
        right[i] = interleaved[i * 2 + 1];
    }
}

static void interleave(const float* left, const float* right, float* interleaved, int frames) {
    for (int i = 0; i < frames; i++) {
        interleaved[i * 2] = left[i];
        interleaved[i * 2 + 1] = right[i];
    }
}

// ============================================================================
// Core processing function - called from Kotlin render thread
// ============================================================================
static int processAudio(float* outputInterleaved, int outputFrames) {
    if (!isInitialized.load() || stretch == nullptr || inputBuffer == nullptr) {
        memset(outputInterleaved, 0, outputFrames * 2 * sizeof(float));
        return 0;
    }

    bool playing = isPlaying.load();
    
    float pitchSemitones = currentPitchSemitones.load();
    stretch->setTransposeSemitones(pitchSemitones);

    float tempo = currentTempo.load();
    double inputFramesNeeded = outputFrames * tempo + tempoAccumulator;
    int inputFramesToRead = (int)inputFramesNeeded;
    tempoAccumulator = inputFramesNeeded - inputFramesToRead;

    size_t maxFrames = std::max(inputFramesToRead, outputFrames);
    if (inputLeft.size() < maxFrames) {
        inputLeft.resize(maxFrames);
        inputRight.resize(maxFrames);
        outputLeft.resize(maxFrames);
        outputRight.resize(maxFrames);
        inputPtrs[0] = inputLeft.data();
        inputPtrs[1] = inputRight.data();
        outputPtrs[0] = outputLeft.data();
        outputPtrs[1] = outputRight.data();
    }

    std::vector<float> interleavedInput(inputFramesToRead * 2);
    size_t samplesRead = inputBuffer->read(interleavedInput.data(), inputFramesToRead * 2);
    int framesRead = samplesRead / 2;

    if (framesRead < inputFramesToRead) {
        underrunCount.fetch_add(1);
        memset(interleavedInput.data() + framesRead * 2, 0, 
               (inputFramesToRead - framesRead) * 2 * sizeof(float));
    }

    deinterleave(interleavedInput.data(), inputLeft.data(), inputRight.data(), inputFramesToRead);
    stretch->process(inputPtrs, inputFramesToRead, outputPtrs, outputFrames);
    
    if (playing) {
        if (effectsBufferLeft.size() < (size_t)outputFrames) {
            effectsBufferLeft.resize(outputFrames);
            effectsBufferRight.resize(outputFrames);
        }

        memcpy(effectsBufferLeft.data(), outputLeft.data(), outputFrames * sizeof(float));
        memcpy(effectsBufferRight.data(), outputRight.data(), outputFrames * sizeof(float));

        float* effectInPtrs[2] = {effectsBufferLeft.data(), effectsBufferRight.data()};
        float* effectOutPtrs[2] = {nullptr, nullptr};
        
        if (effectInputL.size() < (size_t)outputFrames) {
            effectInputL.resize(outputFrames);
            effectInputR.resize(outputFrames);
            effectOutputL.resize(outputFrames);
            effectOutputR.resize(outputFrames);
        }

        if (chorusEnabled.load() && chorusEffect) {
            chorusEffect->mix = chorusMix.load();
            chorusEffect->depthMs = chorusDepthMs.load();
            chorusEffect->detune = chorusDetune.load();
            chorusEffect->stereo = chorusStereo.load();
            
            effectOutPtrs[0] = effectOutputL.data();
            effectOutPtrs[1] = effectOutputR.data();
            chorusEffect->process(effectInPtrs, effectOutPtrs, outputFrames);
            memcpy(effectsBufferLeft.data(), effectOutputL.data(), outputFrames * sizeof(float));
            memcpy(effectsBufferRight.data(), effectOutputR.data(), outputFrames * sizeof(float));
        }

        if (limiterEnabled.load() && limiterEffect) {
            float gainDb = limiterInputGainDb.load();
            limiterEffect->inputGain = powf(10.0f, gainDb / 20.0f);
            limiterEffect->outputLimit = powf(10.0f, limiterLimitDb.load() / 20.0f);
            limiterEffect->attackMs = limiterAttackMs.load();
            limiterEffect->releaseMs = limiterReleaseMs.load();
            
            effectOutPtrs[0] = effectOutputL.data();
            effectOutPtrs[1] = effectOutputR.data();
            limiterEffect->process(effectInPtrs, effectOutPtrs, outputFrames);
            memcpy(effectsBufferLeft.data(), effectOutputL.data(), outputFrames * sizeof(float));
            memcpy(effectsBufferRight.data(), effectOutputR.data(), outputFrames * sizeof(float));
        }

        if (reverbEnabled.load() && reverbEffect) {
            reverbEffect->dry = reverbDry.load();
            reverbEffect->wet = reverbWet.load();
            reverbEffect->roomMs = reverbRoomMs.load();
            reverbEffect->rt20 = reverbDecaySec.load();
            
            effectOutPtrs[0] = effectOutputL.data();
            effectOutPtrs[1] = effectOutputR.data();
            reverbEffect->process(effectInPtrs, effectOutPtrs, outputFrames);
            memcpy(effectsBufferLeft.data(), effectOutputL.data(), outputFrames * sizeof(float));
            memcpy(effectsBufferRight.data(), effectOutputR.data(), outputFrames * sizeof(float));
        }

        if (crunchEnabled.load() && crunchEffect) {
            float driveDb = crunchDriveDb.load();
            crunchEffect->drive = powf(10.0f, driveDb / 20.0f);
            crunchEffect->fuzz = crunchFuzz.load();
            crunchEffect->toneHz = crunchToneHz.load();
            
            effectOutPtrs[0] = effectOutputL.data();
            effectOutPtrs[1] = effectOutputR.data();
            crunchEffect->process(effectInPtrs, effectOutPtrs, outputFrames);
            memcpy(effectsBufferLeft.data(), effectOutputL.data(), outputFrames * sizeof(float));
            memcpy(effectsBufferRight.data(), effectOutputR.data(), outputFrames * sizeof(float));
        }

        if (eqEnabled.load()) {
            float invSampleRate = 1.0f / sampleRate;
            float freqs[5] = {eqBand1Freq.load(), eqBand2Freq.load(), eqBand3Freq.load(), 
                              eqBand4Freq.load(), eqBand5Freq.load()};
            float gains[5] = {eqBand1Gain.load(), eqBand2Gain.load(), eqBand3Gain.load(), 
                              eqBand4Gain.load(), eqBand5Gain.load()};
            
            eqFiltersL[0].lowShelfDb(freqs[0] * invSampleRate, gains[0]);
            eqFiltersR[0].lowShelfDb(freqs[0] * invSampleRate, gains[0]);
            for (int b = 1; b < 4; b++) {
                eqFiltersL[b].peakDb(freqs[b] * invSampleRate, gains[b], 1.0);
                eqFiltersR[b].peakDb(freqs[b] * invSampleRate, gains[b], 1.0);
            }
            eqFiltersL[4].highShelfDb(freqs[4] * invSampleRate, gains[4]);
            eqFiltersR[4].highShelfDb(freqs[4] * invSampleRate, gains[4]);
            
            for (int i = 0; i < outputFrames; i++) {
                float sampleL = effectsBufferLeft[i];
                float sampleR = effectsBufferRight[i];
                for (int b = 0; b < 5; b++) {
                    sampleL = eqFiltersL[b](sampleL);
                    sampleR = eqFiltersR[b](sampleR);
                }
                effectsBufferLeft[i] = sampleL;
                effectsBufferRight[i] = sampleR;
            }
        }

        if (compressorEnabled.load()) {
            float threshold = powf(10.0f, compThresholdDb.load() / 20.0f);
            float ratio = compRatio.load();
            float attackCoef = expf(-1.0f / (compAttackMs.load() * 0.001f * sampleRate));
            float releaseCoef = expf(-1.0f / (compReleaseMs.load() * 0.001f * sampleRate));
            float makeupGain = powf(10.0f, compMakeupGainDb.load() / 20.0f);
            
            for (int i = 0; i < outputFrames; i++) {
                float inputL = fabsf(effectsBufferLeft[i]);
                float inputR = fabsf(effectsBufferRight[i]);
                float inputPeak = fmaxf(inputL, inputR);
                
                float targetEnv = inputPeak;
                float coef = (targetEnv > compEnvelopeL) ? attackCoef : releaseCoef;
                compEnvelopeL = coef * compEnvelopeL + (1.0f - coef) * targetEnv;
                
                float gainReduction = 1.0f;
                if (compEnvelopeL > threshold) {
                    float overDb = 20.0f * log10f(compEnvelopeL / threshold);
                    float reducedDb = overDb * (1.0f - 1.0f / ratio);
                    gainReduction = powf(10.0f, -reducedDb / 20.0f);
                }
                
                effectsBufferLeft[i] *= gainReduction * makeupGain;
                effectsBufferRight[i] *= gainReduction * makeupGain;
            }
        }

        if (hrtfEnabled.load()) {
            int azimuth = hrtfAzimuth.load();
            float intensity = hrtfIntensity.load();
            
            if (!hrtfInitialized || hrtfCurrentAzimuth != azimuth) {
                int actualAzimuth = azimuth;
                int actualElevation = 0;
                unsigned int taps = mit_hrtf_availability(azimuth, 0, 44100, 1);
                
                if (taps > 0) {
                    std::vector<short> hrtfL(taps), hrtfR(taps);
                    mit_hrtf_get(&actualAzimuth, &actualElevation, 44100, 1, hrtfL.data(), hrtfR.data());
                    
                    std::vector<float> irL(taps), irR(taps);
                    for (unsigned int i = 0; i < taps; i++) {
                        irL[i] = hrtfL[i] / 32768.0f;
                        irR[i] = hrtfR[i] / 32768.0f;
                    }
                    
                    hrtfConvolverL.init(512, irL.data(), taps);
                    hrtfConvolverR.init(512, irR.data(), taps);
                    hrtfCurrentAzimuth = azimuth;
                    hrtfInitialized = true;
                    
                    hrtfTempBufferL.resize(outputFrames);
                    hrtfTempBufferR.resize(outputFrames);
                }
            }
            
            if (hrtfInitialized) {
                hrtfConvolverL.process(effectsBufferLeft.data(), hrtfTempBufferL.data(), outputFrames);
                hrtfConvolverR.process(effectsBufferRight.data(), hrtfTempBufferR.data(), outputFrames);
                
                for (int i = 0; i < outputFrames; i++) {
                    effectsBufferLeft[i] = effectsBufferLeft[i] * (1.0f - intensity) + hrtfTempBufferL[i] * intensity;
                    effectsBufferRight[i] = effectsBufferRight[i] * (1.0f - intensity) + hrtfTempBufferR[i] * intensity;
                }
            }
        }

        interleave(effectsBufferLeft.data(), effectsBufferRight.data(), outputInterleaved, outputFrames);
        totalOutputFrames.fetch_add(outputFrames);
        totalInputFramesConsumed.fetch_add(framesRead);
    } else {
        memset(outputInterleaved, 0, outputFrames * 2 * sizeof(float));
    }

    return outputFrames;
}

// ============================================================================
// JNI Functions
// ============================================================================
extern "C" {

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    javaVM = vm;
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeInit(
        JNIEnv *env,
        jobject /* this */,
        jint inSampleRate,
        jint inChannelCount,
        jint bufferSize
) {
    LOGD("nativeInit: sampleRate=%d, channels=%d, bufferSize=%d",
         inSampleRate, inChannelCount, bufferSize);

    // Cleanup previous resources
    if (stretch != nullptr) {
        delete stretch;
        stretch = nullptr;
    }
    if (inputBuffer != nullptr) {
        delete inputBuffer;
        inputBuffer = nullptr;
    }

    sampleRate = inSampleRate;
    channelCount = inChannelCount;

    // Create Signalsmith Stretch
    stretch = new signalsmith::stretch::SignalsmithStretch<float>();
    stretch->presetDefault(channelCount, sampleRate);
    
    LOGD("Signalsmith Stretch initialized: inputLatency=%d, outputLatency=%d",
         stretch->inputLatency(), stretch->outputLatency());

    // Create input ring buffer
    inputBuffer = new SpscRingBuffer(BUFFER_SIZE_SAMPLES);

    // Initialize scratch buffers
    inputLeft.resize(PROCESS_BLOCK_FRAMES);
    inputRight.resize(PROCESS_BLOCK_FRAMES);
    outputLeft.resize(PROCESS_BLOCK_FRAMES);
    outputRight.resize(PROCESS_BLOCK_FRAMES);
    inputPtrs[0] = inputLeft.data();
    inputPtrs[1] = inputRight.data();
    outputPtrs[0] = outputLeft.data();
    outputPtrs[1] = outputRight.data();

    if (chorusEffect) delete chorusEffect;
    if (limiterEffect) delete limiterEffect;
    if (reverbEffect) delete reverbEffect;
    if (crunchEffect) delete crunchEffect;

    chorusEffect = new signalsmith::basics::ChorusFloat(50.0f);
    chorusEffect->configure(sampleRate, PROCESS_BLOCK_FRAMES, channelCount);
    chorusEffect->reset();
    chorusEffect->mix = 0.5;
    chorusEffect->depthMs = 15.0;
    chorusEffect->detune = 5.0;
    chorusEffect->stereo = 1.0;

    limiterEffect = new signalsmith::basics::LimiterFloat(100.0f);
    limiterEffect->configure(sampleRate, PROCESS_BLOCK_FRAMES, channelCount);
    limiterEffect->reset();
    limiterEffect->inputGain = 1.0;
    limiterEffect->outputLimit = 0.708;
    limiterEffect->attackMs = 20.0;
    limiterEffect->releaseMs = 100.0;

    reverbEffect = new signalsmith::basics::ReverbFloat(200.0f, 2.0f);
    reverbEffect->configure(sampleRate, PROCESS_BLOCK_FRAMES, 2);
    reverbEffect->reset();
    reverbEffect->dry = 1.0;
    reverbEffect->wet = 0.3;
    reverbEffect->roomMs = 50.0;
    reverbEffect->rt20 = 2.0;

    crunchEffect = new signalsmith::basics::CrunchFloat(true);
    crunchEffect->configure(sampleRate, PROCESS_BLOCK_FRAMES, channelCount);
    crunchEffect->reset();
    crunchEffect->drive = 1.0;
    crunchEffect->fuzz = 0.0;
    crunchEffect->toneHz = 5000.0;

    effectsBufferLeft.resize(PROCESS_BLOCK_FRAMES);
    effectsBufferRight.resize(PROCESS_BLOCK_FRAMES);

    isPlaying.store(false);
    currentPitchSemitones.store(0.0f);
    currentTempo.store(1.0f);
    totalOutputFrames.store(0);
    seekPositionUs.store(0);
    totalInputFramesConsumed.store(0);
    tempoAccumulator = 0.0;
    underrunCount.store(0);

    chorusEnabled.store(false);
    limiterEnabled.store(false);
    reverbEnabled.store(false);
    crunchEnabled.store(false);

    isInitialized.store(true);
    LOGD("nativeInit: completed successfully with effects");
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeWritePcm(
        JNIEnv *env,
        jobject /* this */,
        jobject buffer,
        jint sizeInBytes,
        jlong presentationTimeUs
) {
    if (!isInitialized.load() || inputBuffer == nullptr) {
        LOGE("nativeWritePcm: not initialized");
        return;
    }

    void* bufferPtr = env->GetDirectBufferAddress(buffer);
    if (bufferPtr == nullptr) {
        LOGE("nativeWritePcm: GetDirectBufferAddress returned NULL");
        return;
    }

    short* shortBuffer = static_cast<short*>(bufferPtr);
    int numSamples = sizeInBytes / sizeof(short);
    int numFrames = numSamples / channelCount;

    // Convert short to float
    std::vector<float> floatBuffer(numSamples);
    shortToFloat(shortBuffer, floatBuffer.data(), numSamples);

    // Write to ring buffer
    size_t written = inputBuffer->write(floatBuffer.data(), numSamples);
    
    if (written < (size_t)numSamples) {
        LOGW("nativeWritePcm: buffer full, dropped %zu samples", numSamples - written);
    }
}

JNIEXPORT jint JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeProcess(
        JNIEnv *env,
        jobject /* this */,
        jshortArray outputArray,
        jint outputFrames
) {
    if (!isInitialized.load()) {
        return 0;
    }

    // Get output array
    jshort* output = env->GetShortArrayElements(outputArray, nullptr);
    if (output == nullptr) {
        return 0;
    }

    // Process audio
    std::vector<float> floatOutput(outputFrames * channelCount);
    int framesProcessed = processAudio(floatOutput.data(), outputFrames);

    // Convert to short
    floatToShort(floatOutput.data(), output, outputFrames * channelCount);

    env->ReleaseShortArrayElements(outputArray, output, 0);
    return framesProcessed;
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativePlay(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativePlay");
    isPlaying.store(true);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativePause(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativePause");
    isPlaying.store(false);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeFlush(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativeFlush");
    if (inputBuffer != nullptr) {
        inputBuffer->clear();
    }
    if (stretch != nullptr) {
        stretch->reset();
    }
    totalOutputFrames.store(0);
    totalInputFramesConsumed.store(0);
    tempoAccumulator = 0.0;
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeReset(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativeReset");
    isPlaying.store(false);
    if (inputBuffer != nullptr) {
        inputBuffer->clear();
    }
    if (stretch != nullptr) {
        stretch->reset();
    }
    totalOutputFrames.store(0);
    seekPositionUs.store(0);
    totalInputFramesConsumed.store(0);
    currentPitchSemitones.store(0.0f);
    currentTempo.store(1.0f);
    tempoAccumulator = 0.0;
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeRelease(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativeRelease");
    isPlaying.store(false);
    isInitialized.store(false);

    if (stretch != nullptr) {
        delete stretch;
        stretch = nullptr;
    }
    if (inputBuffer != nullptr) {
        delete inputBuffer;
        inputBuffer = nullptr;
    }
    if (chorusEffect != nullptr) {
        delete chorusEffect;
        chorusEffect = nullptr;
    }
    if (limiterEffect != nullptr) {
        delete limiterEffect;
        limiterEffect = nullptr;
    }
    if (reverbEffect != nullptr) {
        delete reverbEffect;
        reverbEffect = nullptr;
    }
    if (crunchEffect != nullptr) {
        delete crunchEffect;
        crunchEffect = nullptr;
    }
    
    inputLeft.clear();
    inputRight.clear();
    outputLeft.clear();
    outputRight.clear();
    effectsBufferLeft.clear();
    effectsBufferRight.clear();
    effectInputL.clear();
    effectInputR.clear();
    effectOutputL.clear();
    effectOutputR.clear();
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetPitch(
        JNIEnv *env,
        jobject /* this */,
        jfloat pitchRatio
) {
    // Convert ratio to semitones: semitones = 12 * log2(ratio)
    float semitones = 12.0f * log2f(pitchRatio);
    LOGD("nativeSetPitch: ratio=%f, semitones=%f", pitchRatio, semitones);
    currentPitchSemitones.store(semitones);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetTempo(
        JNIEnv *env,
        jobject /* this */,
        jfloat tempo
) {
    LOGD("nativeSetTempo: %f", tempo);
    currentTempo.store(tempo);
}

JNIEXPORT jlong JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeGetCurrentPositionUs(
        JNIEnv *env,
        jobject /* this */
) {
    int64_t outputFrames = totalOutputFrames.load();
    int64_t seekPos = seekPositionUs.load();
    
    // Calculate played time based on output frames
    int64_t playedTimeUs = (outputFrames * 1000000LL) / sampleRate;
    int64_t result = seekPos + playedTimeUs;

    return result;
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetSeekPosition(
        JNIEnv *env,
        jobject /* this */,
        jlong positionUs
) {
    LOGD("nativeSetSeekPosition: %lld us", (long long)positionUs);
    seekPositionUs.store(positionUs);
    totalOutputFrames.store(0);
    totalInputFramesConsumed.store(0);
}

JNIEXPORT jboolean JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeIsPlaying(
        JNIEnv *env,
        jobject /* this */
) {
    return isPlaying.load();
}

JNIEXPORT jint JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeGetBufferAvailableSpace(
        JNIEnv *env,
        jobject /* this */
) {
    if (inputBuffer == nullptr) return 0;
    return (jint)inputBuffer->space();
}

JNIEXPORT jboolean JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeHasPendingData(
        JNIEnv *env,
        jobject /* this */
) {
    if (inputBuffer == nullptr) return JNI_FALSE;
    return (inputBuffer->available() > 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeGetBufferUsedSamples(
        JNIEnv *env,
        jobject /* this */
) {
    if (inputBuffer == nullptr) return 0;
    return (jint)inputBuffer->available();
}

JNIEXPORT jint JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeGetUnderrunCount(
        JNIEnv *env,
        jobject /* this */
) {
    return underrunCount.load();
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeResetStats(
        JNIEnv *env,
        jobject /* this */
) {
    underrunCount.store(0);
}

JNIEXPORT jint JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeGetInputLatency(
        JNIEnv *env,
        jobject /* this */
) {
    if (stretch == nullptr) return 0;
    return stretch->inputLatency();
}

JNIEXPORT jint JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeGetOutputLatency(
        JNIEnv *env,
        jobject /* this */
) {
    if (stretch == nullptr) return 0;
    return stretch->outputLatency();
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetChorusEnabled(
        JNIEnv *env,
        jobject /* this */,
        jboolean enabled
) {
    LOGD("nativeSetChorusEnabled: %d", enabled);
    chorusEnabled.store(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetChorusParams(
        JNIEnv *env,
        jobject /* this */,
        jfloat mix,
        jfloat depthMs,
        jfloat detune,
        jfloat stereo
) {
    LOGD("nativeSetChorusParams: mix=%f, depthMs=%f, detune=%f, stereo=%f", mix, depthMs, detune, stereo);
    chorusMix.store(mix);
    chorusDepthMs.store(depthMs);
    chorusDetune.store(detune);
    chorusStereo.store(stereo);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetLimiterEnabled(
        JNIEnv *env,
        jobject /* this */,
        jboolean enabled
) {
    LOGD("nativeSetLimiterEnabled: %d", enabled);
    limiterEnabled.store(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetLimiterParams(
        JNIEnv *env,
        jobject /* this */,
        jfloat inputGainDb,
        jfloat limitDb,
        jfloat attackMs,
        jfloat releaseMs
) {
    LOGD("nativeSetLimiterParams: inputGain=%f, limit=%f, attack=%f, release=%f", inputGainDb, limitDb, attackMs, releaseMs);
    limiterInputGainDb.store(inputGainDb);
    limiterLimitDb.store(limitDb);
    limiterAttackMs.store(attackMs);
    limiterReleaseMs.store(releaseMs);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetReverbEnabled(
        JNIEnv *env,
        jobject /* this */,
        jboolean enabled
) {
    LOGD("nativeSetReverbEnabled: %d", enabled);
    reverbEnabled.store(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetReverbParams(
        JNIEnv *env,
        jobject /* this */,
        jfloat dry,
        jfloat wet,
        jfloat roomMs,
        jfloat decaySec
) {
    LOGD("nativeSetReverbParams: dry=%f, wet=%f, room=%f, decay=%f", dry, wet, roomMs, decaySec);
    reverbDry.store(dry);
    reverbWet.store(wet);
    reverbRoomMs.store(roomMs);
    reverbDecaySec.store(decaySec);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetCrunchEnabled(
        JNIEnv *env,
        jobject /* this */,
        jboolean enabled
) {
    LOGD("nativeSetCrunchEnabled: %d", enabled);
    crunchEnabled.store(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetCrunchParams(
        JNIEnv *env,
        jobject /* this */,
        jfloat driveDb,
        jfloat fuzz,
        jfloat toneHz
) {
    LOGD("nativeSetCrunchParams: drive=%f, fuzz=%f, tone=%f", driveDb, fuzz, toneHz);
    crunchDriveDb.store(driveDb);
    crunchFuzz.store(fuzz);
    crunchToneHz.store(toneHz);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetEqEnabled(
        JNIEnv *env,
        jobject /* this */,
        jboolean enabled
) {
    LOGD("nativeSetEqEnabled: %d", enabled);
    eqEnabled.store(enabled);
    if (!enabled) {
        for (int i = 0; i < 5; i++) {
            eqFiltersL[i].reset();
            eqFiltersR[i].reset();
        }
    }
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetEqBand(
        JNIEnv *env,
        jobject /* this */,
        jint band,
        jfloat freq,
        jfloat gainDb
) {
    LOGD("nativeSetEqBand: band=%d, freq=%f, gain=%f", band, freq, gainDb);
    switch (band) {
        case 0: eqBand1Freq.store(freq); eqBand1Gain.store(gainDb); break;
        case 1: eqBand2Freq.store(freq); eqBand2Gain.store(gainDb); break;
        case 2: eqBand3Freq.store(freq); eqBand3Gain.store(gainDb); break;
        case 3: eqBand4Freq.store(freq); eqBand4Gain.store(gainDb); break;
        case 4: eqBand5Freq.store(freq); eqBand5Gain.store(gainDb); break;
    }
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetCompressorEnabled(
        JNIEnv *env,
        jobject /* this */,
        jboolean enabled
) {
    LOGD("nativeSetCompressorEnabled: %d", enabled);
    compressorEnabled.store(enabled);
    if (!enabled) {
        compEnvelopeL = 0.0f;
        compEnvelopeR = 0.0f;
    }
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetCompressorParams(
        JNIEnv *env,
        jobject /* this */,
        jfloat thresholdDb,
        jfloat ratio,
        jfloat attackMs,
        jfloat releaseMs,
        jfloat makeupGainDb
) {
    LOGD("nativeSetCompressorParams: threshold=%f, ratio=%f, attack=%f, release=%f, makeup=%f", 
         thresholdDb, ratio, attackMs, releaseMs, makeupGainDb);
    compThresholdDb.store(thresholdDb);
    compRatio.store(ratio);
    compAttackMs.store(attackMs);
    compReleaseMs.store(releaseMs);
    compMakeupGainDb.store(makeupGainDb);
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetPitchDetectionEnabled(
        JNIEnv *env,
        jobject /* this */,
        jboolean enabled
) {
    LOGD("nativeSetPitchDetectionEnabled: %d", enabled);
    pitchDetectionEnabled.store(enabled);
}

JNIEXPORT jfloat JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeGetDetectedPitch(
        JNIEnv *env,
        jobject /* this */
) {
    return detectedPitch.load();
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetHrtfEnabled(
        JNIEnv *env,
        jobject /* this */,
        jboolean enabled
) {
    LOGD("nativeSetHrtfEnabled: %d", enabled);
    hrtfEnabled.store(enabled);
    if (!enabled) {
        hrtfInitialized = false;
    }
}

JNIEXPORT void JNICALL
Java_com_example_audio_1effect_SignalsmithAudioEngine_nativeSetHrtfParams(
        JNIEnv *env,
        jobject /* this */,
        jfloat intensity,
        jint azimuth
) {
    LOGD("nativeSetHrtfParams: intensity=%f, azimuth=%d", intensity, azimuth);
    hrtfIntensity.store(intensity);
    hrtfAzimuth.store(azimuth);
}

} // extern "C"
