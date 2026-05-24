#include <jni.h>
#include <android/log.h>

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <limits>
#include <memory>
#include <string>
#include <vector>

#include "signalsmith/signalsmith-linear/fft.h"
#include "third_party/onnxruntime/headers/onnxruntime_c_api.h"
#include "third_party/onnxruntime/headers/nnapi_provider_factory.h"

#define LOG_TAG "VocalRT"
#ifdef NDEBUG
#define LOGI(...) ((void)0)
#else
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#endif
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

    std::vector<float> deinterleavedChannel;

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

inline float clampFloat(float value, float minValue, float maxValue) {
    return std::max(minValue, std::min(maxValue, value));
}

inline VocalRemovalStftEngine *toEngine(jlong handle) {
    return reinterpret_cast<VocalRemovalStftEngine *>(handle);
}

bool renderIstftToInterleaved(
    VocalRemovalStftEngine *engine,
    const float *realIn,
    const float *imagIn,
    int numFrames,
    int channelCount
) {
    if (engine == nullptr || realIn == nullptr || imagIn == nullptr || numFrames <= 0 || channelCount <= 0) {
        return false;
    }

    const int outputSamples = (numFrames - 1) * engine->hopLength + engine->nFft;
    if (outputSamples <= 0) {
        return false;
    }

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

    return true;
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
    jobject outputRealBuffer,
    jobject outputImagBuffer,
    jint channelCount
) {
    auto *engine = toEngine(handle);
    if (engine == nullptr || pcmInput == nullptr || outputRealBuffer == nullptr || outputImagBuffer == nullptr) {
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

    auto *realOut = static_cast<float *>(env->GetDirectBufferAddress(outputRealBuffer));
    auto *imagOut = static_cast<float *>(env->GetDirectBufferAddress(outputImagBuffer));
    if (realOut == nullptr || imagOut == nullptr) {
        LOGE("nativeComputeStft output requires direct ByteBuffer");
        return 0;
    }

    const int frames = 1 + ((numSamples - engine->nFft) / engine->hopLength);
    if (frames <= 0) {
        return 0;
    }

    const jint expectedSize = channelCount * engine->dimF * frames;
    const jlong expectedBytes = static_cast<jlong>(expectedSize) * static_cast<jlong>(sizeof(float));
    if (env->GetDirectBufferCapacity(outputRealBuffer) < expectedBytes ||
        env->GetDirectBufferCapacity(outputImagBuffer) < expectedBytes) {
        LOGE("nativeComputeStft output buffer too small");
        return 0;
    }

    const size_t totalSamplesPerChannel = static_cast<size_t>(numSamples);
    if (engine->deinterleavedChannel.size() < totalSamplesPerChannel) {
        engine->deinterleavedChannel.resize(totalSamplesPerChannel);
    }

    for (int channel = 0; channel < channelCount; ++channel) {
        for (int s = 0; s < numSamples; ++s) {
            engine->deinterleavedChannel[static_cast<size_t>(s)] =
                pcm16ToFloat(pcmPtr[s * channelCount + channel]);
        }

        for (int t = 0; t < frames; ++t) {
            const int frameOffset = t * engine->hopLength;
            for (int i = 0; i < engine->nFft; ++i) {
                engine->timeDomainFrame[static_cast<size_t>(i)] =
                    engine->deinterleavedChannel[static_cast<size_t>(frameOffset + i)]
                    * engine->analysisWindow[static_cast<size_t>(i)];
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

    return frames;
}

JNIEXPORT jboolean JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeApplyVocalConfidenceFilter(
    JNIEnv *env,
    jobject,
    jlong handle,
    jobject mixRealBuffer,
    jobject mixImagBuffer,
    jobject vocalRealBuffer,
    jobject vocalImagBuffer,
    jint numFrames,
    jint channelCount,
    jfloat vocalSubtractGain,
    jfloat confidenceLow,
    jfloat confidenceHigh,
    jfloat maxReduction,
    jfloat minFreqHz,
    jfloat maxFreqHz
) {
    auto *engine = toEngine(handle);
    if (engine == nullptr || mixRealBuffer == nullptr || mixImagBuffer == nullptr ||
        vocalRealBuffer == nullptr || vocalImagBuffer == nullptr) {
        return JNI_FALSE;
    }
    if (numFrames <= 0 || channelCount <= 0) {
        return JNI_FALSE;
    }

    auto *mixReal = static_cast<float *>(env->GetDirectBufferAddress(mixRealBuffer));
    auto *mixImag = static_cast<float *>(env->GetDirectBufferAddress(mixImagBuffer));
    auto *vocalReal = static_cast<float *>(env->GetDirectBufferAddress(vocalRealBuffer));
    auto *vocalImag = static_cast<float *>(env->GetDirectBufferAddress(vocalImagBuffer));
    if (mixReal == nullptr || mixImag == nullptr || vocalReal == nullptr || vocalImag == nullptr) {
        LOGE("nativeApplyVocalConfidenceFilter requires direct ByteBuffers");
        return JNI_FALSE;
    }

    const jint expectedSize = channelCount * engine->dimF * numFrames;
    const jlong expectedBytes = static_cast<jlong>(expectedSize) * static_cast<jlong>(sizeof(float));
    if (env->GetDirectBufferCapacity(mixRealBuffer) < expectedBytes ||
        env->GetDirectBufferCapacity(mixImagBuffer) < expectedBytes ||
        env->GetDirectBufferCapacity(vocalRealBuffer) < expectedBytes ||
        env->GetDirectBufferCapacity(vocalImagBuffer) < expectedBytes) {
        LOGE("nativeApplyVocalConfidenceFilter buffer too small");
        return JNI_FALSE;
    }

    const float denom = std::max(1e-6f, confidenceHigh - confidenceLow);
    const float sampleRate = static_cast<float>(engine->sampleRate);
    const float nFft = static_cast<float>(engine->nFft);
    const int channelStride = engine->dimF * numFrames;

    for (int channel = 0; channel < channelCount; ++channel) {
        const int channelOffset = channel * channelStride;
        for (int f = 0; f < engine->dimF; ++f) {
            const float freqHz = static_cast<float>(f) * sampleRate / nFft;
            const bool inVocalBand = freqHz >= minFreqHz && freqHz <= maxFreqHz;
            const int bandOffset = channelOffset + (f * numFrames);

            for (int t = 0; t < numFrames; ++t) {
                const int index = bandOffset + t;
                const float mixR = mixReal[index];
                const float mixI = mixImag[index];
                const float vocalR = vocalReal[index];
                const float vocalI = vocalImag[index];

                const float mixMag = std::sqrt((mixR * mixR) + (mixI * mixI));
                const float vocalMag = std::sqrt((vocalR * vocalR) + (vocalI * vocalI));
                const float confidence = mixMag > 1e-6f
                    ? clampFloat(vocalMag / mixMag, 0.0f, 1.5f)
                    : 0.0f;
                const float strength = inVocalBand
                    ? clampFloat((confidence - confidenceLow) / denom, 0.0f, 1.0f)
                    : 0.0f;
                const float attenuation = 1.0f - (maxReduction * strength);

                vocalReal[index] = (mixR - (vocalR * vocalSubtractGain)) * attenuation;
                vocalImag[index] = (mixI - (vocalI * vocalSubtractGain)) * attenuation;
            }
        }
    }

    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativePackMdxModelInput(
    JNIEnv *env,
    jobject,
    jlong handle,
    jobject stftRealBuffer,
    jobject stftImagBuffer,
    jobject modelInputBuffer,
    jint numFrames
) {
    auto *engine = toEngine(handle);
    if (engine == nullptr || stftRealBuffer == nullptr || stftImagBuffer == nullptr ||
        modelInputBuffer == nullptr) {
        return JNI_FALSE;
    }
    if (numFrames <= 0) {
        return JNI_FALSE;
    }

    auto *stftReal = static_cast<float *>(env->GetDirectBufferAddress(stftRealBuffer));
    auto *stftImag = static_cast<float *>(env->GetDirectBufferAddress(stftImagBuffer));
    auto *modelInput = static_cast<float *>(env->GetDirectBufferAddress(modelInputBuffer));
    if (stftReal == nullptr || stftImag == nullptr || modelInput == nullptr) {
        LOGE("nativePackMdxModelInput requires direct ByteBuffers");
        return JNI_FALSE;
    }

    const jint channelStride = engine->dimF * numFrames;
    const jlong stereoBytes = static_cast<jlong>(2 * channelStride) * static_cast<jlong>(sizeof(float));
    const jlong modelBytes = static_cast<jlong>(4 * channelStride) * static_cast<jlong>(sizeof(float));
    if (env->GetDirectBufferCapacity(stftRealBuffer) < stereoBytes ||
        env->GetDirectBufferCapacity(stftImagBuffer) < stereoBytes ||
        env->GetDirectBufferCapacity(modelInputBuffer) < modelBytes) {
        LOGE("nativePackMdxModelInput buffer too small");
        return JNI_FALSE;
    }

    for (int f = 0; f < engine->dimF; ++f) {
        const int bandOffset = f * numFrames;
        for (int t = 0; t < numFrames; ++t) {
            const int tf = bandOffset + t;
            const int rightBase = channelStride + tf;
            modelInput[tf] = stftReal[tf];
            modelInput[channelStride + tf] = stftImag[tf];
            modelInput[(2 * channelStride) + tf] = stftReal[rightBase];
            modelInput[(3 * channelStride) + tf] = stftImag[rightBase];
        }
    }

    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeUnpackMdxModelOutput(
    JNIEnv *env,
    jobject,
    jlong handle,
    jobject modelOutputBuffer,
    jobject istftRealBuffer,
    jobject istftImagBuffer,
    jint numFrames,
    jint channelCount
) {
    auto *engine = toEngine(handle);
    if (engine == nullptr || modelOutputBuffer == nullptr || istftRealBuffer == nullptr ||
        istftImagBuffer == nullptr) {
        return JNI_FALSE;
    }
    if (numFrames <= 0 || channelCount <= 0) {
        return JNI_FALSE;
    }

    auto *modelOutput = static_cast<float *>(env->GetDirectBufferAddress(modelOutputBuffer));
    auto *istftReal = static_cast<float *>(env->GetDirectBufferAddress(istftRealBuffer));
    auto *istftImag = static_cast<float *>(env->GetDirectBufferAddress(istftImagBuffer));
    if (modelOutput == nullptr || istftReal == nullptr || istftImag == nullptr) {
        LOGE("nativeUnpackMdxModelOutput requires direct ByteBuffers");
        return JNI_FALSE;
    }

    const jint channelStride = engine->dimF * numFrames;
    const jlong modelBytes = static_cast<jlong>(4 * channelStride) * static_cast<jlong>(sizeof(float));
    const jlong istftBytes = static_cast<jlong>(channelCount * channelStride) * static_cast<jlong>(sizeof(float));
    if (env->GetDirectBufferCapacity(modelOutputBuffer) < modelBytes ||
        env->GetDirectBufferCapacity(istftRealBuffer) < istftBytes ||
        env->GetDirectBufferCapacity(istftImagBuffer) < istftBytes) {
        LOGE("nativeUnpackMdxModelOutput buffer too small");
        return JNI_FALSE;
    }

    if (channelCount == 1) {
        for (int i = 0; i < channelStride; ++i) {
            const float leftReal = modelOutput[i];
            const float leftImag = modelOutput[channelStride + i];
            const float rightReal = modelOutput[(2 * channelStride) + i];
            const float rightImag = modelOutput[(3 * channelStride) + i];
            istftReal[i] = 0.5f * (leftReal + rightReal);
            istftImag[i] = 0.5f * (leftImag + rightImag);
        }
    } else {
        for (int i = 0; i < channelStride; ++i) {
            const int rightIndex = channelStride + i;
            istftReal[i] = modelOutput[i];
            istftImag[i] = modelOutput[channelStride + i];
            istftReal[rightIndex] = modelOutput[(2 * channelStride) + i];
            istftImag[rightIndex] = modelOutput[(3 * channelStride) + i];
        }
    }

    return JNI_TRUE;
}

JNIEXPORT jint JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeComputeIstft(
    JNIEnv *env,
    jobject,
    jlong handle,
    jobject inputRealBuffer,
    jobject inputImagBuffer,
    jobject pcmOutput,
    jint numFrames,
    jint channelCount
) {
    auto *engine = toEngine(handle);
    if (engine == nullptr || inputRealBuffer == nullptr || inputImagBuffer == nullptr || pcmOutput == nullptr) {
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

    auto *realIn = static_cast<float *>(env->GetDirectBufferAddress(inputRealBuffer));
    auto *imagIn = static_cast<float *>(env->GetDirectBufferAddress(inputImagBuffer));
    if (realIn == nullptr || imagIn == nullptr) {
        LOGE("nativeComputeIstft input requires direct ByteBuffer");
        return 0;
    }

    const jint expectedSize = channelCount * engine->dimF * numFrames;
    const jlong expectedBytes = static_cast<jlong>(expectedSize) * static_cast<jlong>(sizeof(float));
    if (env->GetDirectBufferCapacity(inputRealBuffer) < expectedBytes ||
        env->GetDirectBufferCapacity(inputImagBuffer) < expectedBytes) {
        LOGE("nativeComputeIstft input buffer too small");
        return 0;
    }

    if (!renderIstftToInterleaved(engine, realIn, imagIn, numFrames, channelCount)) {
        return 0;
    }

    const size_t totalSamples = static_cast<size_t>(outputSamples * channelCount);
    for (size_t i = 0; i < totalSamples; ++i) {
        pcmPtr[i] = floatToPcm16(engine->outputInterleaved[i]);
    }

    return outputSamples;
}

JNIEXPORT jint JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeComputeIstftInterval(
    JNIEnv *env,
    jobject,
    jlong handle,
    jobject inputRealBuffer,
    jobject inputImagBuffer,
    jbyteArray dryPcmInput,
    jbyteArray pcmOutput,
    jint numFrames,
    jint channelCount,
    jint extractOffsetSamples,
    jint intervalSamples,
    jint dryOffsetBytes,
    jfloat mixRatio,
    jfloat vocalSubtractGain,
    jboolean modelOutputsVocal,
    jboolean modelOutputConvertedToInstrumental,
    jboolean vocalOnlyMode
) {
    auto *engine = toEngine(handle);
    if (engine == nullptr || inputRealBuffer == nullptr || inputImagBuffer == nullptr ||
        dryPcmInput == nullptr || pcmOutput == nullptr) {
        return 0;
    }
    if (numFrames <= 0 || channelCount <= 0 || extractOffsetSamples < 0 ||
        intervalSamples <= 0 || dryOffsetBytes < 0) {
        return 0;
    }

    const int outputSamples = (numFrames - 1) * engine->hopLength + engine->nFft;
    if (outputSamples <= 0 || extractOffsetSamples + intervalSamples > outputSamples) {
        return 0;
    }

    auto *realIn = static_cast<float *>(env->GetDirectBufferAddress(inputRealBuffer));
    auto *imagIn = static_cast<float *>(env->GetDirectBufferAddress(inputImagBuffer));
    if (realIn == nullptr || imagIn == nullptr) {
        LOGE("nativeComputeIstftInterval input requires direct ByteBuffer");
        return 0;
    }

    const jint expectedSize = channelCount * engine->dimF * numFrames;
    const jlong expectedBytes = static_cast<jlong>(expectedSize) * static_cast<jlong>(sizeof(float));
    if (env->GetDirectBufferCapacity(inputRealBuffer) < expectedBytes ||
        env->GetDirectBufferCapacity(inputImagBuffer) < expectedBytes) {
        LOGE("nativeComputeIstftInterval input buffer too small");
        return 0;
    }

    const jsize requiredBytes = static_cast<jsize>(intervalSamples * channelCount * sizeof(int16_t));
    if (env->GetArrayLength(pcmOutput) < requiredBytes) {
        LOGE("nativeComputeIstftInterval output byte array too small");
        return 0;
    }
    if (env->GetArrayLength(dryPcmInput) < dryOffsetBytes + requiredBytes) {
        LOGE("nativeComputeIstftInterval dry input byte array too small");
        return 0;
    }

    if (!renderIstftToInterleaved(engine, realIn, imagIn, numFrames, channelCount)) {
        return 0;
    }

    jbyte *dryBytes = env->GetByteArrayElements(dryPcmInput, nullptr);
    if (dryBytes == nullptr) {
        return 0;
    }
    jbyte *outputBytes = env->GetByteArrayElements(pcmOutput, nullptr);
    if (outputBytes == nullptr) {
        env->ReleaseByteArrayElements(dryPcmInput, dryBytes, JNI_ABORT);
        return 0;
    }

    const float mix = clampFloat(mixRatio, 0.0f, 1.0f);
    size_t byteIndex = 0;
    for (int s = 0; s < intervalSamples; ++s) {
        const int sourceSample = extractOffsetSamples + s;
        for (int channel = 0; channel < channelCount; ++channel) {
            const size_t sourceIndex = static_cast<size_t>(sourceSample * channelCount + channel);
            const int dryByteIndex = dryOffsetBytes + static_cast<int>(byteIndex);
            const uint16_t origPacked =
                static_cast<uint16_t>(static_cast<uint8_t>(dryBytes[dryByteIndex])) |
                (static_cast<uint16_t>(static_cast<uint8_t>(dryBytes[dryByteIndex + 1])) << 8u);
            const int orig = static_cast<int>(static_cast<int16_t>(origPacked));
            const int modelOut = static_cast<int>(floatToPcm16(engine->outputInterleaved[sourceIndex]));

            const int instrumental =
                (modelOutputsVocal == JNI_TRUE && modelOutputConvertedToInstrumental != JNI_TRUE)
                    ? std::clamp(
                        static_cast<int>(static_cast<float>(orig) - (static_cast<float>(modelOut) * vocalSubtractGain)),
                        static_cast<int>(std::numeric_limits<int16_t>::min()),
                        static_cast<int>(std::numeric_limits<int16_t>::max()))
                    : modelOut;
            const int vocal =
                (modelOutputsVocal == JNI_TRUE && modelOutputConvertedToInstrumental != JNI_TRUE)
                    ? modelOut
                    : std::clamp(
                        orig - instrumental,
                        static_cast<int>(std::numeric_limits<int16_t>::min()),
                        static_cast<int>(std::numeric_limits<int16_t>::max()));
            const int target = (vocalOnlyMode == JNI_TRUE) ? vocal : instrumental;
            const int mixed = std::clamp(
                static_cast<int>((static_cast<float>(orig) * (1.0f - mix)) + (static_cast<float>(target) * mix)),
                static_cast<int>(std::numeric_limits<int16_t>::min()),
                static_cast<int>(std::numeric_limits<int16_t>::max()));
            const uint16_t pcm = static_cast<uint16_t>(static_cast<int16_t>(mixed));
            outputBytes[byteIndex++] = static_cast<jbyte>(pcm & 0xFFu);
            outputBytes[byteIndex++] = static_cast<jbyte>((pcm >> 8u) & 0xFFu);
        }
    }

    env->ReleaseByteArrayElements(dryPcmInput, dryBytes, JNI_ABORT);
    env->ReleaseByteArrayElements(pcmOutput, outputBytes, 0);
    return intervalSamples;
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

}  // extern "C" (STFT functions)

// ============================================================================
//  Native MDX-NET inference (replaces Java OrtSession for ~3x speedup on Note 20)
// ============================================================================

namespace {

struct MdxModelEngine {
    const OrtApi* api = nullptr;
    OrtEnv* env = nullptr;
    OrtSession* session = nullptr;
    OrtMemoryInfo* memoryInfo = nullptr;
    std::string inputName;
    std::string outputName;
    int dimF = 0;
    int channels = 4;

    ~MdxModelEngine() { release(); }

    void logStatus(const char* step, OrtStatus* status) {
        if (api && status) {
            LOGE("MODEL %s failed: %s", step, api->GetErrorMessage(status));
            api->ReleaseStatus(status);
        }
    }

    bool init(const char* modelPath, int threads, int dimFIn, int channelsIn) {
        // Production/default path: XNNPACK CPU NEON kernels. NNAPI experiments
        // showed no useful speedup for current MDX models on the test device.
        constexpr bool kUseNnapiHybrid = false;

        api = OrtGetApiBase()->GetApi(ORT_API_VERSION);
        if (api == nullptr) { LOGE("MODEL ORT API unavailable"); return false; }

        OrtStatus* status = api->CreateEnv(ORT_LOGGING_LEVEL_WARNING, "TransposeMDX", &env);
        if (status != nullptr) { logStatus("CreateEnv", status); return false; }

        OrtSessionOptions* options = nullptr;
        status = api->CreateSessionOptions(&options);
        if (status != nullptr) { logStatus("CreateSessionOptions", status); return false; }

        status = api->SetSessionGraphOptimizationLevel(options, ORT_ENABLE_ALL);
        if (status != nullptr) {
            logStatus("SetSessionGraphOptimizationLevel", status);
            api->ReleaseSessionOptions(options);
            return false;
        }
        status = api->SetIntraOpNumThreads(options, std::max(1, threads));
        if (status != nullptr) {
            logStatus("SetIntraOpNumThreads", status);
            api->ReleaseSessionOptions(options);
            return false;
        }
        status = api->SetInterOpNumThreads(options, 1);
        if (status != nullptr) {
            logStatus("SetInterOpNumThreads", status);
            api->ReleaseSessionOptions(options);
            return false;
        }

        // NNAPI execution provider is disabled for the default path.
        bool nnapiAttached = false;
        if (kUseNnapiHybrid) {
            const uint32_t nnapiFlags = NNAPI_FLAG_USE_FP16 | NNAPI_FLAG_CPU_DISABLED;
            OrtStatus* nnapiStatus =
                OrtSessionOptionsAppendExecutionProvider_Nnapi(options, nnapiFlags);
            if (nnapiStatus != nullptr) {
                LOGE("BACKEND_ATTACH provider=NNAPI status=failed flags=0x%x reason=%s",
                     nnapiFlags,
                     api->GetErrorMessage(nnapiStatus));
                api->ReleaseStatus(nnapiStatus);
                api->ReleaseSessionOptions(options);
                return false;
            } else {
                nnapiAttached = true;
                LOGI("BACKEND_ATTACH provider=NNAPI status=attached flags=0x%x fp16=true nnapiCpuDisabled=true ortCpuFallback=allowed", nnapiFlags);
            }
        } else {
            LOGI("BACKEND_ATTACH provider=NNAPI status=disabled");
        }

        // Try XNNPACK CPU NEON kernels.
        bool xnnpackAttached = false;
        if (!kUseNnapiHybrid) {
            const std::string threadsValue = std::to_string(std::max(1, threads));
            const char* xnnpackKeys[] = {"intra_op_num_threads"};
            const char* xnnpackValues[] = {threadsValue.c_str()};
            OrtStatus* xnnpackStatus = api->SessionOptionsAppendExecutionProvider(
                options, "XNNPACK", xnnpackKeys, xnnpackValues, 1);
            if (xnnpackStatus != nullptr) {
                LOGI("BACKEND_ATTACH provider=XNNPACK status=unavailable reason=%s",
                     api->GetErrorMessage(xnnpackStatus));
                api->ReleaseStatus(xnnpackStatus);
            } else {
                xnnpackAttached = true;
                LOGI("BACKEND_ATTACH provider=XNNPACK status=attached threads=%d", threads);
            }
        } else {
            LOGI("BACKEND_ATTACH provider=XNNPACK status=disabled");
        }

        status = api->CreateSession(env, modelPath, options, &session);
        api->ReleaseSessionOptions(options);
        if (status != nullptr) { logStatus("CreateSession", status); return false; }

        status = api->CreateCpuMemoryInfo(OrtArenaAllocator, OrtMemTypeDefault, &memoryInfo);
        if (status != nullptr) { logStatus("CreateCpuMemoryInfo", status); return false; }

        OrtAllocator* allocator = nullptr;
        status = api->GetAllocatorWithDefaultOptions(&allocator);
        if (status != nullptr) { logStatus("GetAllocatorWithDefaultOptions", status); return false; }
        char* inName = nullptr;
        if (api->SessionGetInputName(session, 0, allocator, &inName) == nullptr && inName != nullptr) {
            inputName = inName;
            allocator->Free(allocator, inName);
        } else {
            inputName = "input";
        }
        char* outName = nullptr;
        if (api->SessionGetOutputName(session, 0, allocator, &outName) == nullptr && outName != nullptr) {
            outputName = outName;
            allocator->Free(allocator, outName);
        } else {
            outputName = "output";
        }

        dimF = dimFIn;
        channels = channelsIn;

        LOGI("BACKEND_READY nnapi=%s xnnpack=%s input=%s output=%s dimF=%d channels=%d model=%s",
             nnapiAttached ? "attached" : "unavailable",
             xnnpackAttached ? "attached" : "unavailable",
             inputName.c_str(), outputName.c_str(), dimF, channels, modelPath);
        return true;
    }

    void release() {
        if (api != nullptr) {
            if (session != nullptr) { api->ReleaseSession(session); session = nullptr; }
            if (memoryInfo != nullptr) { api->ReleaseMemoryInfo(memoryInfo); memoryInfo = nullptr; }
            if (env != nullptr) { api->ReleaseEnv(env); env = nullptr; }
            api = nullptr;
        }
    }

    bool run(float* inputData, float* outputData, int frames) {
        if (session == nullptr || memoryInfo == nullptr || api == nullptr) return false;

        const std::int64_t shape[4] = {1, channels, dimF, frames};
        const size_t elementCount = static_cast<size_t>(channels) *
                                    static_cast<size_t>(dimF) *
                                    static_cast<size_t>(frames);

        OrtValue* inputValue = nullptr;
        OrtStatus* status = api->CreateTensorWithDataAsOrtValue(
            memoryInfo, inputData, elementCount * sizeof(float),
            shape, 4, ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT, &inputValue);
        if (status != nullptr) { logStatus("CreateTensorInput", status); return false; }

        const char* inNames[] = {inputName.c_str()};
        const char* outNames[] = {outputName.c_str()};
        OrtValue* outputValue = nullptr;

        status = api->Run(session, nullptr, inNames, &inputValue, 1, outNames, 1, &outputValue);
        api->ReleaseValue(inputValue);
        if (status != nullptr) {
            logStatus("Run", status);
            if (outputValue != nullptr) api->ReleaseValue(outputValue);
            return false;
        }

        float* result = nullptr;
        status = api->GetTensorMutableData(outputValue, reinterpret_cast<void**>(&result));
        if (status != nullptr || result == nullptr) {
            if (status != nullptr) logStatus("GetTensorData", status);
            if (outputValue != nullptr) api->ReleaseValue(outputValue);
            return false;
        }

        std::memcpy(outputData, result, elementCount * sizeof(float));
        api->ReleaseValue(outputValue);
        return true;
    }

    bool runWaveform(float* inputData, float* outputData, int samples) {
        if (session == nullptr || memoryInfo == nullptr || api == nullptr) return false;

        const std::int64_t shape[3] = {1, 2, samples};
        const size_t elementCount = static_cast<size_t>(2) * static_cast<size_t>(samples);

        OrtValue* inputValue = nullptr;
        OrtStatus* status = api->CreateTensorWithDataAsOrtValue(
            memoryInfo, inputData, elementCount * sizeof(float),
            shape, 3, ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT, &inputValue);
        if (status != nullptr) { logStatus("CreateTensorWaveformInput", status); return false; }

        const char* inNames[] = {inputName.c_str()};
        const char* outNames[] = {outputName.c_str()};
        OrtValue* outputValue = nullptr;

        status = api->Run(session, nullptr, inNames, &inputValue, 1, outNames, 1, &outputValue);
        api->ReleaseValue(inputValue);
        if (status != nullptr) {
            logStatus("RunWaveform", status);
            if (outputValue != nullptr) api->ReleaseValue(outputValue);
            return false;
        }

        float* result = nullptr;
        status = api->GetTensorMutableData(outputValue, reinterpret_cast<void**>(&result));
        if (status != nullptr || result == nullptr) {
            if (status != nullptr) logStatus("GetWaveformTensorData", status);
            if (outputValue != nullptr) api->ReleaseValue(outputValue);
            return false;
        }

        std::memcpy(outputData, result, elementCount * sizeof(float));
        api->ReleaseValue(outputValue);
        return true;
    }

    bool runPolarformer(float* inputData, float* outputData, int frames, int features) {
        if (session == nullptr || memoryInfo == nullptr || api == nullptr) return false;
        if (frames <= 0 || features <= 0) return false;

        const std::int64_t shape[3] = {1, frames, features};
        const size_t elementCount = static_cast<size_t>(frames) * static_cast<size_t>(features);

        OrtValue* inputValue = nullptr;
        OrtStatus* status = api->CreateTensorWithDataAsOrtValue(
            memoryInfo, inputData, elementCount * sizeof(float),
            shape, 3, ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT, &inputValue);
        if (status != nullptr) { logStatus("CreateTensorPolarformerInput", status); return false; }

        const char* inNames[] = {inputName.c_str()};
        const char* outNames[] = {outputName.c_str()};
        OrtValue* outputValue = nullptr;

        status = api->Run(session, nullptr, inNames, &inputValue, 1, outNames, 1, &outputValue);
        api->ReleaseValue(inputValue);
        if (status != nullptr) {
            logStatus("RunPolarformer", status);
            if (outputValue != nullptr) api->ReleaseValue(outputValue);
            return false;
        }

        float* result = nullptr;
        status = api->GetTensorMutableData(outputValue, reinterpret_cast<void**>(&result));
        if (status != nullptr || result == nullptr) {
            if (status != nullptr) logStatus("GetPolarformerTensorData", status);
            if (outputValue != nullptr) api->ReleaseValue(outputValue);
            return false;
        }

        std::memcpy(outputData, result, elementCount * sizeof(float));
        api->ReleaseValue(outputValue);
        return true;
    }
};

inline MdxModelEngine* toMdxEngine(jlong handle) {
    return reinterpret_cast<MdxModelEngine*>(handle);
}

}  // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeInitMdxModel(
    JNIEnv* env, jobject, jstring modelPath, jint threads, jint dimF, jint channels) {
    if (modelPath == nullptr) return 0;
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    if (path == nullptr) return 0;
    auto engine = std::make_unique<MdxModelEngine>();
    const bool ok = engine->init(path, static_cast<int>(threads),
                                 static_cast<int>(dimF), static_cast<int>(channels));
    env->ReleaseStringUTFChars(modelPath, path);
    if (!ok) return 0;
    return reinterpret_cast<jlong>(engine.release());
}

JNIEXPORT jboolean JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeRunMdxModel(
    JNIEnv* env, jobject, jlong handle,
    jobject inputBuffer, jobject outputBuffer, jint frames) {
    auto* eng = toMdxEngine(handle);
    if (eng == nullptr || inputBuffer == nullptr || outputBuffer == nullptr) return JNI_FALSE;
    auto* input = static_cast<float*>(env->GetDirectBufferAddress(inputBuffer));
    auto* output = static_cast<float*>(env->GetDirectBufferAddress(outputBuffer));
    if (input == nullptr || output == nullptr) {
        LOGE("nativeRunMdxModel requires direct ByteBuffers");
        return JNI_FALSE;
    }
    return eng->run(input, output, static_cast<int>(frames)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeRunWaveformModel(
    JNIEnv* env, jobject, jlong handle,
    jobject inputBuffer, jobject outputBuffer, jint samples) {
    auto* eng = toMdxEngine(handle);
    if (eng == nullptr || inputBuffer == nullptr || outputBuffer == nullptr) return JNI_FALSE;
    auto* input = static_cast<float*>(env->GetDirectBufferAddress(inputBuffer));
    auto* output = static_cast<float*>(env->GetDirectBufferAddress(outputBuffer));
    if (input == nullptr || output == nullptr) {
        LOGE("nativeRunWaveformModel requires direct ByteBuffers");
        return JNI_FALSE;
    }
    return eng->runWaveform(input, output, static_cast<int>(samples)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeRunPolarformerModel(
    JNIEnv* env, jobject, jlong handle,
    jobject inputBuffer, jobject outputBuffer, jint frames, jint features) {
    auto* eng = toMdxEngine(handle);
    if (eng == nullptr || inputBuffer == nullptr || outputBuffer == nullptr) return JNI_FALSE;
    auto* input = static_cast<float*>(env->GetDirectBufferAddress(inputBuffer));
    auto* output = static_cast<float*>(env->GetDirectBufferAddress(outputBuffer));
    if (input == nullptr || output == nullptr) {
        LOGE("nativeRunPolarformerModel requires direct ByteBuffers");
        return JNI_FALSE;
    }
    return eng->runPolarformer(input, output, static_cast<int>(frames), static_cast<int>(features))
        ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_VocalRemovalProcessor_nativeReleaseMdxModel(
    JNIEnv*, jobject, jlong handle) {
    delete toMdxEngine(handle);
}

}  // extern "C" (MDX functions)
