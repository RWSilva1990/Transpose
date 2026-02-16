#include <jni.h>
#include <android/log.h>

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <memory>
#include <vector>

#include "signalsmith/signalsmith-linear/fft.h"

#define LOG_TAG "VocalRemovalNative"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {

constexpr float kInt16ToFloat = 1.0f / 32768.0f;
constexpr float kFloatToInt16 = 32767.0f;
constexpr float kPi = 3.14159265358979323846f;

struct VocalRemovalStftEngine {
    int sampleRate = 44100;
    int nFft = 6144;
    int hopLength = 1024;
    int dimF = 2048;
    int fullBins = 3073;

    signalsmith::linear::RealFFT<float> fft;
    std::vector<float> analysisWindow;
    std::vector<float> synthesisWindow;

    std::vector<float> timeDomainFrame;
    std::vector<float> freqReal;
    std::vector<float> freqImag;

    // Reused buffers for iSTFT to avoid per-call allocations.
    std::vector<float> outputInterleaved;
    std::vector<float> channelOutput;
    std::vector<float> normalization;

    explicit VocalRemovalStftEngine(int sampleRateIn, int nFftIn, int hopLengthIn, int dimFIn)
        : sampleRate(sampleRateIn)
        , nFft(nFftIn)
        , hopLength(hopLengthIn)
        , dimF(dimFIn)
        , fullBins((nFftIn / 2) + 1)
        , fft(nFftIn)
        , analysisWindow(static_cast<size_t>(nFftIn), 0.0f)
        , synthesisWindow(static_cast<size_t>(nFftIn), 0.0f)
        , timeDomainFrame(static_cast<size_t>(nFftIn), 0.0f)
        , freqReal(static_cast<size_t>(fullBins), 0.0f)
        , freqImag(static_cast<size_t>(fullBins), 0.0f) {
        for (int i = 0; i < nFft; ++i) {
            const float phase = (2.0f * kPi * static_cast<float>(i))
                / static_cast<float>(nFft);
            const float hann = 0.5f - 0.5f * std::cos(phase);
            analysisWindow[static_cast<size_t>(i)] = hann;
            synthesisWindow[static_cast<size_t>(i)] = hann;
        }
    }
};

inline int16_t floatToPcm16(float value) {
    float scaled = value * kFloatToInt16;
    scaled = std::max(-32768.0f, std::min(32767.0f, scaled));
    return static_cast<int16_t>(scaled);
}

inline float pcm16ToFloat(int16_t value) {
    return static_cast<float>(value) * kInt16ToFloat;
}

inline VocalRemovalStftEngine *toEngine(jlong handle) {
    return reinterpret_cast<VocalRemovalStftEngine *>(handle);
}

}

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeInitStft(
    JNIEnv *,
    jobject,
    jint sampleRate,
    jint nFft,
    jint hopLength,
    jint dimF
) {
    if (sampleRate <= 0 || nFft <= 0 || hopLength <= 0 || dimF <= 0) {
        LOGE("nativeInitStft invalid arguments");
        return 0;
    }

    auto *engine = new VocalRemovalStftEngine(sampleRate, nFft, hopLength, dimF);
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT jint JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeComputeStft(
    JNIEnv *env,
    jobject,
    jlong handle,
    jobject pcmInput,
    jint numSamples,
    jfloatArray outputReal,
    jfloatArray outputImag,
    jint channelCount
) {
    auto *engine = toEngine(handle);
    if (engine == nullptr || pcmInput == nullptr || outputReal == nullptr || outputImag == nullptr) {
        return 0;
    }
    if (channelCount <= 0 || numSamples < engine->nFft) {
        return 0;
    }

    auto *pcmPtr = static_cast<int16_t *>(env->GetDirectBufferAddress(pcmInput));
    if (pcmPtr == nullptr) {
        LOGE("nativeComputeStft requires direct ByteBuffer");
        return 0;
    }

    const int frames = 1 + ((numSamples - engine->nFft) / engine->hopLength);
    if (frames <= 0) {
        return 0;
    }

    const jint expectedSize = channelCount * engine->dimF * frames;
    if (env->GetArrayLength(outputReal) < expectedSize || env->GetArrayLength(outputImag) < expectedSize) {
        LOGE("nativeComputeStft output arrays too small");
        return 0;
    }

    jboolean realIsCopy = JNI_FALSE;
    jboolean imagIsCopy = JNI_FALSE;
    auto *realOut = env->GetFloatArrayElements(outputReal, &realIsCopy);
    auto *imagOut = env->GetFloatArrayElements(outputImag, &imagIsCopy);

    for (int channel = 0; channel < channelCount; ++channel) {
        for (int t = 0; t < frames; ++t) {
            const int frameOffset = t * engine->hopLength;
            for (int i = 0; i < engine->nFft; ++i) {
                const int sampleIndex = frameOffset + i;
                const int interleavedIndex = sampleIndex * channelCount + channel;
                const float sample = pcm16ToFloat(pcmPtr[interleavedIndex]);
                engine->timeDomainFrame[static_cast<size_t>(i)] =
                    sample * engine->analysisWindow[static_cast<size_t>(i)];
            }

            engine->fft.fft(
                engine->timeDomainFrame.data(),
                engine->freqReal.data(),
                engine->freqImag.data()
            );

            const int base = (channel * engine->dimF * frames) + t;
            for (int f = 0; f < engine->dimF; ++f) {
                const int outIndex = base + (f * frames);
                realOut[outIndex] = engine->freqReal[static_cast<size_t>(f)];
                imagOut[outIndex] = engine->freqImag[static_cast<size_t>(f)];
            }
        }
    }

    env->ReleaseFloatArrayElements(outputReal, realOut, 0);
    env->ReleaseFloatArrayElements(outputImag, imagOut, 0);
    return frames;
}

JNIEXPORT jint JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeComputeIstft(
    JNIEnv *env,
    jobject,
    jlong handle,
    jfloatArray inputReal,
    jfloatArray inputImag,
    jobject pcmOutput,
    jint numFrames,
    jint channelCount
) {
    auto *engine = toEngine(handle);
    if (engine == nullptr || inputReal == nullptr || inputImag == nullptr || pcmOutput == nullptr) {
        return 0;
    }
    if (numFrames <= 0 || channelCount <= 0) {
        return 0;
    }

    const int outputSamples = (numFrames - 1) * engine->hopLength + engine->nFft;
    if (outputSamples <= 0) {
        return 0;
    }

    auto *pcmPtr = static_cast<int16_t *>(env->GetDirectBufferAddress(pcmOutput));
    if (pcmPtr == nullptr) {
        LOGE("nativeComputeIstft requires direct ByteBuffer");
        return 0;
    }

    const jint expectedSize = channelCount * engine->dimF * numFrames;
    if (env->GetArrayLength(inputReal) < expectedSize || env->GetArrayLength(inputImag) < expectedSize) {
        LOGE("nativeComputeIstft input arrays too small");
        return 0;
    }

    jboolean realIsCopy = JNI_FALSE;
    jboolean imagIsCopy = JNI_FALSE;
    auto *realIn = env->GetFloatArrayElements(inputReal, &realIsCopy);
    auto *imagIn = env->GetFloatArrayElements(inputImag, &imagIsCopy);

    const size_t totalSamples = static_cast<size_t>(outputSamples * channelCount);
    if (engine->outputInterleaved.size() != totalSamples) {
        engine->outputInterleaved.resize(totalSamples);
    }
    if (engine->channelOutput.size() != static_cast<size_t>(outputSamples)) {
        engine->channelOutput.resize(static_cast<size_t>(outputSamples));
    }
    if (engine->normalization.size() != static_cast<size_t>(outputSamples)) {
        engine->normalization.resize(static_cast<size_t>(outputSamples));
    }
    std::fill(engine->outputInterleaved.begin(), engine->outputInterleaved.end(), 0.0f);

    auto &outputInterleaved = engine->outputInterleaved;
    auto &channelOutput = engine->channelOutput;
    auto &normalization = engine->normalization;

    // signalsmith::linear::RealFFT uses an unnormalized inverse; match common DSP conventions by scaling by 1/N.
    const float invFft = 1.0f / static_cast<float>(engine->nFft);

    for (int channel = 0; channel < channelCount; ++channel) {
        std::fill(channelOutput.begin(), channelOutput.end(), 0.0f);
        std::fill(normalization.begin(), normalization.end(), 0.0f);

        for (int t = 0; t < numFrames; ++t) {
            std::fill(engine->freqReal.begin(), engine->freqReal.end(), 0.0f);
            std::fill(engine->freqImag.begin(), engine->freqImag.end(), 0.0f);

            const int base = (channel * engine->dimF * numFrames) + t;
            const int usableBins = std::min(engine->dimF, engine->fullBins);
            for (int f = 0; f < usableBins; ++f) {
                const int inIndex = base + (f * numFrames);
                engine->freqReal[static_cast<size_t>(f)] = realIn[inIndex];
                engine->freqImag[static_cast<size_t>(f)] = imagIn[inIndex];
            }

            engine->fft.ifft(
                engine->freqReal.data(),
                engine->freqImag.data(),
                engine->timeDomainFrame.data()
            );

            const int frameOffset = t * engine->hopLength;
            for (int i = 0; i < engine->nFft; ++i) {
                const int sampleIndex = frameOffset + i;
                if (sampleIndex >= outputSamples) {
                    break;
                }
                const float window = engine->synthesisWindow[static_cast<size_t>(i)];
                const float sample = (engine->timeDomainFrame[static_cast<size_t>(i)] * invFft) * window;
                channelOutput[static_cast<size_t>(sampleIndex)] += sample;
                normalization[static_cast<size_t>(sampleIndex)] += window * window;
            }
        }

        for (int s = 0; s < outputSamples; ++s) {
            const float norm = normalization[static_cast<size_t>(s)];
            float value = channelOutput[static_cast<size_t>(s)];
            if (norm > 1e-8f) {
                value /= norm;
            }
            const size_t outIndex = static_cast<size_t>(s * channelCount + channel);
            outputInterleaved[outIndex] = value;
        }
    }

    for (size_t i = 0; i < totalSamples; ++i) {
        pcmPtr[i] = floatToPcm16(outputInterleaved[i]);
    }

    env->ReleaseFloatArrayElements(inputReal, realIn, JNI_ABORT);
    env->ReleaseFloatArrayElements(inputImag, imagIn, JNI_ABORT);
    return outputSamples;
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeReleaseStft(
    JNIEnv *,
    jobject,
    jlong handle
) {
    auto *engine = toEngine(handle);
    delete engine;
}

}
