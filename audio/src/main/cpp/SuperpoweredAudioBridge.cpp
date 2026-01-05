#include <jni.h>
#include <string>
#include <android/log.h>
#include <mutex>
#include <atomic>
#include <cmath>
#include <vector>
#include <superpowered/OpenSource/SuperpoweredAndroidAudioIO.h>
#include <superpowered/Superpowered.h>
#include <superpowered/SuperpoweredTimeStretching.h>
#include <superpowered/SuperpoweredSimple.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <SLES/OpenSLES.h>

#define LOG_TAG "SuperpoweredNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Ring buffer for PCM data from ExoPlayer
class PcmRingBuffer {
public:
    PcmRingBuffer(size_t capacity) : capacity(capacity), size(0), readPos(0), writePos(0) {
        buffer = new float[capacity];
        memset(buffer, 0, capacity * sizeof(float));
    }

    ~PcmRingBuffer() {
        delete[] buffer;
    }

    size_t write(const float* data, size_t samples) {
        std::lock_guard<std::mutex> lock(mutex);
        size_t toWrite = std::min(samples, capacity - size);
        for (size_t i = 0; i < toWrite; i++) {
            buffer[writePos] = data[i];
            writePos = (writePos + 1) % capacity;
        }
        size += toWrite;
        return toWrite;
    }

    size_t read(float* data, size_t samples) {
        std::lock_guard<std::mutex> lock(mutex);
        size_t toRead = std::min(samples, size);
        for (size_t i = 0; i < toRead; i++) {
            data[i] = buffer[readPos];
            readPos = (readPos + 1) % capacity;
        }
        size -= toRead;
        for (size_t i = toRead; i < samples; i++) {
            data[i] = 0.0f;
        }
        return toRead;
    }

    void clear() {
        std::lock_guard<std::mutex> lock(mutex);
        size = 0;
        readPos = 0;
        writePos = 0;
    }

    size_t available() {
        std::lock_guard<std::mutex> lock(mutex);
        return size;
    }

private:
    float* buffer;
    size_t capacity;
    size_t size;
    size_t readPos;
    size_t writePos;
    std::mutex mutex;
};

// Global state
static SuperpoweredAndroidAudioIO *audioIO = nullptr;
static Superpowered::TimeStretching *timeStretching = nullptr;
static PcmRingBuffer *inputBuffer = nullptr;
static float *tempInputBuffer = nullptr;
static float *tempOutputBuffer = nullptr;

static std::atomic<bool> isPlaying(false);
static std::atomic<float> currentPitch(1.0f);
static std::atomic<float> currentTempo(1.0f);

static std::atomic<int64_t> totalOutputFrames(0);
static std::atomic<int64_t> basePositionUs(0);
static std::atomic<int64_t> totalConsumedFrames(0);
static std::mutex positionMutex;
static int64_t totalInputFrames = 0;

static std::atomic<int> pushRejectedCount(0);

static int inputSampleRate = 44100;
static int inputChannelCount = 2;
static int outputSampleRate = 44100;
static int outputBufferSize = 256;
static const size_t BUFFER_SIZE_SAMPLES = 44100 * 2 * 10;
static const int MAX_FRAMES_TO_FEED = 4096;
static const size_t TEMP_INPUT_BUFFER_SIZE = MAX_FRAMES_TO_FEED * 2;

static std::atomic<int> silenceCount(0);
static std::atomic<int> underrunCount(0);

static bool audioProcessing(
        void * __unused clientdata,
        short int *audio,
        int numberOfFrames,
        int __unused samplerate
) {
    static int callCount = 0;
    callCount++;

    if (timeStretching == nullptr || inputBuffer == nullptr) {
        memset(audio, 0, numberOfFrames * 2 * sizeof(short int));
        return false;
    }

    size_t available = inputBuffer->available();

    if (available > 0) {
        int framesToFeed = std::min((int)(available / 2), MAX_FRAMES_TO_FEED);
        if (framesToFeed > 0) {
            inputBuffer->read(tempInputBuffer, framesToFeed * 2);
            timeStretching->addInput(tempInputBuffer, framesToFeed);
            totalConsumedFrames.fetch_add(framesToFeed);
        }
    }

    if (!isPlaying.load()) {
        memset(audio, 0, numberOfFrames * 2 * sizeof(short int));
        return false;
    }

    int outputFrames = timeStretching->getOutputLengthFrames();
    if (outputFrames >= numberOfFrames) {
        timeStretching->getOutput(tempOutputBuffer, numberOfFrames);
        Superpowered::FloatToShortInt(tempOutputBuffer, audio, (unsigned int)numberOfFrames);
        totalOutputFrames.fetch_add(numberOfFrames);
        silenceCount.store(0);
    } else {
        memset(audio, 0, numberOfFrames * 2 * sizeof(short int));
        int sc = silenceCount.fetch_add(1);
        if (sc == 0 || sc % 100 == 0) {
            underrunCount.fetch_add(1);
            LOGD("UNDERRUN[%d]: outputFrames=%d, needed=%d, ringAvail=%zu, isPlaying=%d",
                 underrunCount.load(), outputFrames, numberOfFrames, available, isPlaying.load() ? 1 : 0);
        }
    }

    return true;
}
extern "C" {

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeInit(
        JNIEnv *env,
        jobject /* this */,
        jint inSampleRate,
        jint inChannelCount,
        jint outSampleRate,
        jint outBufferSize
) {
    LOGD("nativeInit: inSampleRate=%d, inChannels=%d, outSampleRate=%d, outBufferSize=%d",
         inSampleRate, inChannelCount, outSampleRate, outBufferSize);

    // Cleanup previous resources
    if (audioIO != nullptr) {
        delete audioIO;
        audioIO = nullptr;
    }
    if (timeStretching != nullptr) {
        delete timeStretching;
        timeStretching = nullptr;
    }
    if (inputBuffer != nullptr) {
        delete inputBuffer;
        inputBuffer = nullptr;
    }
    if (tempInputBuffer != nullptr) {
        delete[] tempInputBuffer;
        tempInputBuffer = nullptr;
    }
    if (tempOutputBuffer != nullptr) {
        delete[] tempOutputBuffer;
        tempOutputBuffer = nullptr;
    }

    inputSampleRate = inSampleRate;
    inputChannelCount = inChannelCount;
    outputSampleRate = outSampleRate;
    outputBufferSize = outBufferSize;

    // Initialize Superpowered
    Superpowered::Initialize("ExampleLicenseKey-WillExpire-OnNextUpdate");

    // Create TimeStretching
    timeStretching = new Superpowered::TimeStretching(inSampleRate);
    timeStretching->rate = 1.0f;
    timeStretching->pitchShiftCents = -200;
    LOGD("TimeStretching created: rate=%.2f, pitch=%d (default -2 semitones)",
         timeStretching->rate, timeStretching->pitchShiftCents);
    LOGD("TimeStretching initialized with sampleRate=%d", inSampleRate);

    // Create buffers
    inputBuffer = new PcmRingBuffer(BUFFER_SIZE_SAMPLES);
    tempInputBuffer = new float[TEMP_INPUT_BUFFER_SIZE];
    tempOutputBuffer = new float[outBufferSize * 2];

    // Create audio output
    audioIO = new SuperpoweredAndroidAudioIO(
            inSampleRate,
            outBufferSize,
            false,
            true,
            audioProcessing,
            nullptr,
            -1,
            SL_ANDROID_STREAM_MEDIA
    );
    LOGD("AudioIO created with sampleRate=%d, bufferSize=%d", inSampleRate, outBufferSize);

    // ★ 위치 정보 초기화
    isPlaying.store(false);
    currentPitch.store(1.0f);
    currentTempo.store(1.0f);
    totalOutputFrames.store(0);
    basePositionUs.store(0);
    totalConsumedFrames.store(0);  // ★ 추가
    {
        std::lock_guard<std::mutex> lock(positionMutex);
        totalInputFrames = 0;
    }

    LOGD("nativeInit: completed successfully");
}

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeWritePcm(
        JNIEnv *env,
        jobject /* this */,
        jobject buffer,
        jint sizeInBytes,
        jlong presentationTimeUs
) {

    if (inputBuffer == nullptr) {
        LOGE("nativeWritePcm: inputBuffer is null");
        return;
    }

    void* bufferPtr = env->GetDirectBufferAddress(buffer);
    if (bufferPtr == nullptr) {
        LOGE("nativeWritePcm: GetDirectBufferAddress returned NULL - buffer is not direct!");
        return;
    }

    short* shortBuffer = static_cast<short*>(bufferPtr);
    int numSamples = sizeInBytes / sizeof(short);
    int numFrames = numSamples / 2;


    float* floatBuffer = new float[numSamples];
    Superpowered::ShortIntToFloat(shortBuffer, floatBuffer, (unsigned int)numFrames);

    size_t written = inputBuffer->write(floatBuffer, numSamples);
    delete[] floatBuffer;

    {
        std::lock_guard<std::mutex> lock(positionMutex);
        if (totalInputFrames == 0) {
            basePositionUs.store(presentationTimeUs);
            LOGD("nativeWritePcm: First chunk! basePositionUs=%lld", (long long)presentationTimeUs);
        }
        totalInputFrames += numFrames;
    }
}

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativePlay(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativePlay");
    isPlaying.store(true);
}

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativePause(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativePause");
    isPlaying.store(false);
}

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeFlush(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativeFlush");
    if (inputBuffer != nullptr) {
        inputBuffer->clear();
    }
    if (timeStretching != nullptr) {
        timeStretching->reset();
    }

    totalOutputFrames.store(0);
    basePositionUs.store(0);
    totalConsumedFrames.store(0);  // ★ 추가
    {
        std::lock_guard<std::mutex> lock(positionMutex);
        totalInputFrames = 0;
    }
}

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeReset(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativeReset");
    isPlaying.store(false);
    if (inputBuffer != nullptr) {
        inputBuffer->clear();
    }
    if (timeStretching != nullptr) {
        timeStretching->reset();
    }

    // ★ 위치 정보 리셋
    totalOutputFrames.store(0);
    basePositionUs.store(0);
    {
        std::lock_guard<std::mutex> lock(positionMutex);
        totalInputFrames = 0;
    }

    currentPitch.store(1.0f);
    currentTempo.store(1.0f);
}

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeRelease(
        JNIEnv *env,
        jobject /* this */
) {
    LOGD("nativeRelease");
    isPlaying.store(false);

    if (audioIO != nullptr) {
        delete audioIO;
        audioIO = nullptr;
    }
    if (timeStretching != nullptr) {
        delete timeStretching;
        timeStretching = nullptr;
    }
    if (inputBuffer != nullptr) {
        delete inputBuffer;
        inputBuffer = nullptr;
    }
    if (tempInputBuffer != nullptr) {
        delete[] tempInputBuffer;
        tempInputBuffer = nullptr;
    }
    if (tempOutputBuffer != nullptr) {
        delete[] tempOutputBuffer;
        tempOutputBuffer = nullptr;
    }
}

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeSetPitch(
        JNIEnv *env,
        jobject /* this */,
        jfloat pitch
) {
    LOGD("nativeSetPitch: %f", pitch);
    currentPitch.store(pitch);
    if (timeStretching != nullptr) {
        float semitones = 12.0f * log2f(pitch);
        int cents = (int)(semitones * 100.0f);
        timeStretching->pitchShiftCents = cents;
        LOGD("nativeSetPitch: semitones=%f, cents=%d", semitones, cents);
    }
}

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeSetTempo(
        JNIEnv *env,
        jobject /* this */,
        jfloat tempo
) {
    LOGD("nativeSetTempo: %f", tempo);
    currentTempo.store(tempo);
    if (timeStretching != nullptr) {
        timeStretching->rate = tempo;
        LOGD("nativeSetTempo: rate=%f", tempo);
    }
}

static std::atomic<int64_t> seekPositionUs(0);

JNIEXPORT jlong JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeGetCurrentPositionUs(
        JNIEnv *env,
        jobject /* this */
) {
    int64_t played = totalOutputFrames.load();
    int64_t seekPos = seekPositionUs.load();
    
    int64_t playedTimeUs = (played * 1000000LL) / inputSampleRate;
    int64_t result = seekPos + playedTimeUs;

    static int logCount = 0;
    if (logCount++ % 500 == 0) {
        LOGD("getPosition: played=%lld frames, seekPos=%lld, result=%lld us (%.2f sec)",
             (long long)played, (long long)seekPos, (long long)result, result / 1000000.0);
    }

    return result;
}

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeSetSeekPosition(
        JNIEnv *env,
        jobject /* this */,
        jlong positionUs
) {
    LOGD("setSeekPosition: %lld us (%.2f sec)", (long long)positionUs, positionUs / 1000000.0);
    seekPositionUs.store(positionUs);
    totalOutputFrames.store(0);
}


JNIEXPORT jboolean JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeIsPlaying(
        JNIEnv *env,
        jobject /* this */
) {
    return isPlaying.load();
}

JNIEXPORT jint JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeGetBufferAvailableSpace(
        JNIEnv *env,
        jobject /* this */
) {
    if (inputBuffer == nullptr) return 0;
    size_t used = inputBuffer->available(); // used samples
    size_t space = BUFFER_SIZE_SAMPLES > used ? BUFFER_SIZE_SAMPLES - used : 0;
    return (jint)space;
}

JNIEXPORT jboolean JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeHasPendingData(
        JNIEnv *env,
        jobject /* this */
) {
    if (inputBuffer == nullptr) return JNI_FALSE;
    return (inputBuffer->available() > 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeGetBufferUsedSamples(
        JNIEnv *env,
        jobject /* this */
) {
    if (inputBuffer == nullptr) return 0;
    return (jint)inputBuffer->available();
}

JNIEXPORT jint JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeGetPushRejectedCount(
        JNIEnv *env,
        jobject /* this */
) {
    return pushRejectedCount.load();
}

JNIEXPORT void JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeResetStats(
        JNIEnv *env,
        jobject /* this */
) {
    pushRejectedCount.store(0);
    underrunCount.store(0);
    silenceCount.store(0);
}

JNIEXPORT jint JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeGetUnderrunCount(
        JNIEnv *env,
        jobject /* this */
) {
    return underrunCount.load();
}

JNIEXPORT jint JNICALL
Java_com_example_audio_SuperpoweredAudioEngine_nativeGetSilenceCount(
        JNIEnv *env,
        jobject /* this */
) {
    return silenceCount.load();
}

} // extern "C"

