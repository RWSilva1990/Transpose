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
        interleave(outputLeft.data(), outputRight.data(), outputInterleaved, outputFrames);
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
Java_com_example_audio_SignalsmithAudioEngine_nativeInit(
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

    // Reset state
    isPlaying.store(false);
    currentPitchSemitones.store(0.0f);
    currentTempo.store(1.0f);
    totalOutputFrames.store(0);
    seekPositionUs.store(0);
    totalInputFramesConsumed.store(0);
    tempoAccumulator = 0.0;
    underrunCount.store(0);

    isInitialized.store(true);
    LOGD("nativeInit: completed successfully");
}

JNIEXPORT void JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeWritePcm(
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
Java_com_example_audio_SignalsmithAudioEngine_nativeProcess(
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
Java_com_example_audio_SignalsmithAudioEngine_nativePlay(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativePlay");
    isPlaying.store(true);
}

JNIEXPORT void JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativePause(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativePause");
    isPlaying.store(false);
}

JNIEXPORT void JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeFlush(
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
Java_com_example_audio_SignalsmithAudioEngine_nativeReset(
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
Java_com_example_audio_SignalsmithAudioEngine_nativeRelease(
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
    
    inputLeft.clear();
    inputRight.clear();
    outputLeft.clear();
    outputRight.clear();
}

JNIEXPORT void JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeSetPitch(
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
Java_com_example_audio_SignalsmithAudioEngine_nativeSetTempo(
        JNIEnv *env,
        jobject /* this */,
        jfloat tempo
) {
    LOGD("nativeSetTempo: %f", tempo);
    currentTempo.store(tempo);
}

JNIEXPORT jlong JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeGetCurrentPositionUs(
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
Java_com_example_audio_SignalsmithAudioEngine_nativeSetSeekPosition(
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
Java_com_example_audio_SignalsmithAudioEngine_nativeIsPlaying(
        JNIEnv *env,
        jobject /* this */
) {
    return isPlaying.load();
}

JNIEXPORT jint JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeGetBufferAvailableSpace(
        JNIEnv *env,
        jobject /* this */
) {
    if (inputBuffer == nullptr) return 0;
    return (jint)inputBuffer->space();
}

JNIEXPORT jboolean JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeHasPendingData(
        JNIEnv *env,
        jobject /* this */
) {
    if (inputBuffer == nullptr) return JNI_FALSE;
    return (inputBuffer->available() > 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeGetBufferUsedSamples(
        JNIEnv *env,
        jobject /* this */
) {
    if (inputBuffer == nullptr) return 0;
    return (jint)inputBuffer->available();
}

JNIEXPORT jint JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeGetUnderrunCount(
        JNIEnv *env,
        jobject /* this */
) {
    return underrunCount.load();
}

JNIEXPORT void JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeResetStats(
        JNIEnv *env,
        jobject /* this */
) {
    underrunCount.store(0);
}

JNIEXPORT jint JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeGetInputLatency(
        JNIEnv *env,
        jobject /* this */
) {
    if (stretch == nullptr) return 0;
    return stretch->inputLatency();
}

JNIEXPORT jint JNICALL
Java_com_example_audio_SignalsmithAudioEngine_nativeGetOutputLatency(
        JNIEnv *env,
        jobject /* this */
) {
    if (stretch == nullptr) return 0;
    return stretch->outputLatency();
}

} // extern "C"
