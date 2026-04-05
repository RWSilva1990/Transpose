#include <jni.h>
#include <android/log.h>

#include <algorithm>
#include <array>
#include <atomic>
#include <cmath>
#include <cstring>
#include <memory>
#include <vector>

#include "signalsmith/signalsmith-stretch.h"

#include "signalsmith-basics/chorus.h"
#include "signalsmith-basics/reverb.h"
#include "signalsmith/basics/modules/dsp/filters.h"

#define LOG_TAG "SignalsmithProc"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {
const int PROCESS_BLOCK_FRAMES = 512;
}

class SignalsmithProcessor {
public:
    SignalsmithProcessor(int sampleRate, int channelCount)
        : sampleRate_(sampleRate)
        , channelCount_(channelCount)
        , pitchSemitones_(0.0f)
        , tempoRate_(1.0f)
        , chorusEnabled_(false)
        , reverbEnabled_(false)
        , eqEnabled_(false)
        , chorusMix_(0.5f)
        , chorusDepthMs_(10.0f)
        , chorusDetune_(10.0f)
        , chorusStereo_(0.5f)
        , reverbDry_(1.0f)
        , reverbWet_(0.3f)
        , reverbRoomMs_(50.0f)
        , reverbDecaySec_(2.0f)
        , eqBand1Freq_(60.0f)
        , eqBand1Gain_(0.0f)
        , eqBand2Freq_(250.0f)
        , eqBand2Gain_(0.0f)
        , eqBand3Freq_(1000.0f)
        , eqBand3Gain_(0.0f)
        , eqBand4Freq_(4000.0f)
        , eqBand4Gain_(0.0f)
        , eqBand5Freq_(12000.0f)
        , eqBand5Gain_(0.0f)
{
        stretch_.presetDefault(channelCount_, sampleRate_);

        inputLeft_.resize(PROCESS_BLOCK_FRAMES);
        inputRight_.resize(PROCESS_BLOCK_FRAMES);
        outputLeft_.resize(PROCESS_BLOCK_FRAMES);
        outputRight_.resize(PROCESS_BLOCK_FRAMES);

        effectsBufferLeft_.resize(PROCESS_BLOCK_FRAMES);
        effectsBufferRight_.resize(PROCESS_BLOCK_FRAMES);
        effectOutputL_.resize(PROCESS_BLOCK_FRAMES);
        effectOutputR_.resize(PROCESS_BLOCK_FRAMES);

        chorusEffect_ = std::make_unique<signalsmith::basics::ChorusFloat>(50.0f);
        chorusEffect_->configure(sampleRate_, PROCESS_BLOCK_FRAMES, channelCount_);
        chorusEffect_->reset();

        reverbEffect_ = std::make_unique<signalsmith::basics::ReverbFloat>(200.0f, 2.0f);
        reverbEffect_->configure(sampleRate_, PROCESS_BLOCK_FRAMES, channelCount_ == 2 ? 2 : 1);
        reverbEffect_->reset();

        LOGD("Created: sampleRate=%d, channels=%d, inputLatency=%d, outputLatency=%d",
             sampleRate_, channelCount_, stretch_.inputLatency(), stretch_.outputLatency());
    }

    int process(const short* input, int inputBytes, short* output, int maxOutputFrames) {
        const int inputSamples = inputBytes / static_cast<int>(sizeof(short));
        const int inputFrames = inputSamples / channelCount_;
        if (inputFrames <= 0) return 0;

        const int framesToProcess = std::min(inputFrames, maxOutputFrames);
        if (framesToProcess <= 0) return 0;

        const int samplesPerFrame = channelCount_;
        int processed = 0;
        while (processed < framesToProcess) {
            const int framesRemaining = framesToProcess - processed;
            const int blockFrames = std::min(PROCESS_BLOCK_FRAMES, framesRemaining);

            processBlock(
                input + processed * samplesPerFrame,
                output + processed * samplesPerFrame,
                blockFrames
            );
            processed += blockFrames;
        }

        return framesToProcess;
    }

    int flushAndGetRemaining(short* output, int maxOutputFrames) {
        (void)output;
        (void)maxOutputFrames;
        stretch_.reset();
        return 0;
    }

    void setPitchSemitones(float semitones) {
        pitchSemitones_.store(semitones, std::memory_order_relaxed);
        LOGD("setPitchSemitones: %f", semitones);
    }

    void setTempoRate(float rate) {
        tempoRate_.store(rate, std::memory_order_relaxed);
        LOGD("setTempoRate: %f", rate);
    }

    void flush() {
        stretch_.reset();
        LOGD("flush");
    }

    void setChorusEnabled(bool enabled) { chorusEnabled_.store(enabled, std::memory_order_relaxed); }
    void setChorusParams(float mix, float depthMs, float detune, float stereo) {
        chorusMix_.store(mix, std::memory_order_relaxed);
        chorusDepthMs_.store(depthMs, std::memory_order_relaxed);
        chorusDetune_.store(detune, std::memory_order_relaxed);
        chorusStereo_.store(stereo, std::memory_order_relaxed);
    }

    void setReverbEnabled(bool enabled) { reverbEnabled_.store(enabled, std::memory_order_relaxed); }
    void setReverbParams(float dry, float wet, float roomMs, float decaySec) {
        reverbDry_.store(dry, std::memory_order_relaxed);
        reverbWet_.store(wet, std::memory_order_relaxed);
        reverbRoomMs_.store(roomMs, std::memory_order_relaxed);
        reverbDecaySec_.store(decaySec, std::memory_order_relaxed);
    }

    void setEqEnabled(bool enabled) {
        eqEnabled_.store(enabled, std::memory_order_relaxed);
        if (!enabled) {
            for (auto& f : eqFiltersL_) f.reset();
            for (auto& f : eqFiltersR_) f.reset();
        }
    }

    void setEqBand(int band, float freq, float gainDb) {
        switch (band) {
            case 0: eqBand1Freq_.store(freq, std::memory_order_relaxed); eqBand1Gain_.store(gainDb, std::memory_order_relaxed); break;
            case 1: eqBand2Freq_.store(freq, std::memory_order_relaxed); eqBand2Gain_.store(gainDb, std::memory_order_relaxed); break;
            case 2: eqBand3Freq_.store(freq, std::memory_order_relaxed); eqBand3Gain_.store(gainDb, std::memory_order_relaxed); break;
            case 3: eqBand4Freq_.store(freq, std::memory_order_relaxed); eqBand4Gain_.store(gainDb, std::memory_order_relaxed); break;
            case 4: eqBand5Freq_.store(freq, std::memory_order_relaxed); eqBand5Gain_.store(gainDb, std::memory_order_relaxed); break;
            default: break;
        }
    }

    void setToneFilterEnabled(bool enabled) {
        toneFilterEnabled_.store(enabled, std::memory_order_relaxed);
        if (!enabled) {
            for (auto& f : toneFiltersL_) f.reset();
            for (auto& f : toneFiltersR_) f.reset();
        }
    }

    void setToneFilterParams(float lowCutHz, float highCutHz, float lowShelfDb, float highShelfDb) {
        float safeHighCut = std::max(highCutHz, lowCutHz + 20.0f);
        toneFilterLowCutHz_.store(lowCutHz, std::memory_order_relaxed);
        toneFilterHighCutHz_.store(safeHighCut, std::memory_order_relaxed);
        toneFilterLowShelfDb_.store(lowShelfDb, std::memory_order_relaxed);
        toneFilterHighShelfDb_.store(highShelfDb, std::memory_order_relaxed);
    }

private:
    using BiquadFilter = signalsmith::filters::BiquadStatic<float>;

    bool shouldBypass() const {
        if (pitchSemitones_.load(std::memory_order_relaxed) != 0.0f) return false;

        if (chorusEnabled_.load(std::memory_order_relaxed)) return false;
        if (reverbEnabled_.load(std::memory_order_relaxed)) return false;
        if (toneFilterEnabled_.load(std::memory_order_relaxed)) return false;
        if (eqEnabled_.load(std::memory_order_relaxed)) return false;

        return true;
    }

    void processBlock(const short* input, short* output, int frames) {
        shortToFloatDeinterleaved(input, frames);

        const float pitch = pitchSemitones_.load(std::memory_order_relaxed);
        stretch_.setTransposeSemitones(pitch);

        float* inputPtrs[2] = {inputLeft_.data(), inputRight_.data()};
        float* outputPtrs[2] = {outputLeft_.data(), outputRight_.data()};
        stretch_.process(inputPtrs, frames, outputPtrs, frames);

        std::memcpy(effectsBufferLeft_.data(), outputLeft_.data(), static_cast<size_t>(frames) * sizeof(float));
        std::memcpy(effectsBufferRight_.data(), outputRight_.data(), static_cast<size_t>(frames) * sizeof(float));

        applyEffects(frames);

        floatToShortInterleaved(effectsBufferLeft_.data(), effectsBufferRight_.data(), output, frames);
    }

    void applyEffects(int frames) {
        float* effectInPtrs[2] = {effectsBufferLeft_.data(), effectsBufferRight_.data()};
        float* effectOutPtrs[2] = {effectOutputL_.data(), effectOutputR_.data()};

        if (chorusEnabled_.load(std::memory_order_relaxed) && chorusEffect_) {
            chorusEffect_->mix = chorusMix_.load(std::memory_order_relaxed);
            chorusEffect_->depthMs = chorusDepthMs_.load(std::memory_order_relaxed);
            chorusEffect_->detune = chorusDetune_.load(std::memory_order_relaxed);
            chorusEffect_->stereo = chorusStereo_.load(std::memory_order_relaxed);

            chorusEffect_->process(effectInPtrs, effectOutPtrs, frames);
            std::memcpy(effectsBufferLeft_.data(), effectOutputL_.data(), static_cast<size_t>(frames) * sizeof(float));
            std::memcpy(effectsBufferRight_.data(), effectOutputR_.data(), static_cast<size_t>(frames) * sizeof(float));
        }

        if (reverbEnabled_.load(std::memory_order_relaxed) && reverbEffect_) {
            reverbEffect_->dry = reverbDry_.load(std::memory_order_relaxed);
            reverbEffect_->wet = reverbWet_.load(std::memory_order_relaxed);
            reverbEffect_->roomMs = reverbRoomMs_.load(std::memory_order_relaxed);
            reverbEffect_->rt20 = reverbDecaySec_.load(std::memory_order_relaxed);

            reverbEffect_->process(effectInPtrs, effectOutPtrs, frames);
            std::memcpy(effectsBufferLeft_.data(), effectOutputL_.data(), static_cast<size_t>(frames) * sizeof(float));
            std::memcpy(effectsBufferRight_.data(), effectOutputR_.data(), static_cast<size_t>(frames) * sizeof(float));
        }

        if (toneFilterEnabled_.load(std::memory_order_relaxed)) {
            const float invSr = 1.0f / static_cast<float>(sampleRate_);
            const float lowCut = toneFilterLowCutHz_.load(std::memory_order_relaxed);
            const float highCut = toneFilterHighCutHz_.load(std::memory_order_relaxed);
            const float lowShelfDb = toneFilterLowShelfDb_.load(std::memory_order_relaxed);
            const float highShelfDb = toneFilterHighShelfDb_.load(std::memory_order_relaxed);

            const float lowShelfFreq = std::min(200.0f, highCut * 0.5f);
            const float highShelfFreq = std::max(1000.0f, lowCut * 2.0f);

            toneFiltersL_[0].highpass(lowCut * invSr, 1.9);
            toneFiltersR_[0].highpass(lowCut * invSr, 1.9);
            toneFiltersL_[1].lowpass(highCut * invSr, 1.9);
            toneFiltersR_[1].lowpass(highCut * invSr, 1.9);
            toneFiltersL_[2].lowShelfDb(lowShelfFreq * invSr, lowShelfDb);
            toneFiltersR_[2].lowShelfDb(lowShelfFreq * invSr, lowShelfDb);
            toneFiltersL_[3].highShelfDb(highShelfFreq * invSr, highShelfDb);
            toneFiltersR_[3].highShelfDb(highShelfFreq * invSr, highShelfDb);

            for (int i = 0; i < frames; i++) {
                float sL = effectsBufferLeft_[i];
                float sR = effectsBufferRight_[i];
                for (int f = 0; f < 4; f++) {
                    sL = toneFiltersL_[f](sL);
                    sR = toneFiltersR_[f](sR);
                }
                effectsBufferLeft_[i] = sL;
                effectsBufferRight_[i] = sR;
            }
        }

        if (eqEnabled_.load(std::memory_order_relaxed)) {
            const float invSampleRate = 1.0f / static_cast<float>(sampleRate_);
            const float freqs[5] = {
                eqBand1Freq_.load(std::memory_order_relaxed),
                eqBand2Freq_.load(std::memory_order_relaxed),
                eqBand3Freq_.load(std::memory_order_relaxed),
                eqBand4Freq_.load(std::memory_order_relaxed),
                eqBand5Freq_.load(std::memory_order_relaxed),
            };
            const float gains[5] = {
                eqBand1Gain_.load(std::memory_order_relaxed),
                eqBand2Gain_.load(std::memory_order_relaxed),
                eqBand3Gain_.load(std::memory_order_relaxed),
                eqBand4Gain_.load(std::memory_order_relaxed),
                eqBand5Gain_.load(std::memory_order_relaxed),
            };

            eqFiltersL_[0].lowShelfDb(freqs[0] * invSampleRate, gains[0]);
            eqFiltersR_[0].lowShelfDb(freqs[0] * invSampleRate, gains[0]);
            for (int b = 1; b < 4; b++) {
                eqFiltersL_[b].peakDb(freqs[b] * invSampleRate, gains[b], 1.0);
                eqFiltersR_[b].peakDb(freqs[b] * invSampleRate, gains[b], 1.0);
            }
            eqFiltersL_[4].highShelfDb(freqs[4] * invSampleRate, gains[4]);
            eqFiltersR_[4].highShelfDb(freqs[4] * invSampleRate, gains[4]);

            for (int i = 0; i < frames; i++) {
                float sampleL = effectsBufferLeft_[i];
                float sampleR = effectsBufferRight_[i];
                for (int b = 0; b < 5; b++) {
                    sampleL = eqFiltersL_[b](sampleL);
                    sampleR = eqFiltersR_[b](sampleR);
                }
                effectsBufferLeft_[i] = sampleL;
                effectsBufferRight_[i] = sampleR;
            }
        }
    }

    void shortToFloatDeinterleaved(const short* input, int frames) {
        const float scale = 1.0f / 32768.0f;
        if (channelCount_ == 2) {
            for (int i = 0; i < frames; i++) {
                inputLeft_[i] = input[i * 2] * scale;
                inputRight_[i] = input[i * 2 + 1] * scale;
            }
        } else {
            for (int i = 0; i < frames; i++) {
                inputLeft_[i] = input[i] * scale;
                inputRight_[i] = input[i] * scale;
            }
        }
    }

    void floatToShortInterleaved(const float* left, const float* right, short* output, int frames) {
        if (channelCount_ == 2) {
            for (int i = 0; i < frames; i++) {
                float l = left[i] * 32768.0f;
                float r = right[i] * 32768.0f;
                l = std::max(-32768.0f, std::min(32767.0f, l));
                r = std::max(-32768.0f, std::min(32767.0f, r));
                output[i * 2] = static_cast<short>(l);
                output[i * 2 + 1] = static_cast<short>(r);
            }
        } else {
            for (int i = 0; i < frames; i++) {
                float m = left[i] * 32768.0f;
                m = std::max(-32768.0f, std::min(32767.0f, m));
                output[i] = static_cast<short>(m);
            }
        }
    }

private:
    signalsmith::stretch::SignalsmithStretch<float> stretch_;
    int sampleRate_;
    int channelCount_;

    std::atomic<float> pitchSemitones_;
    std::atomic<float> tempoRate_;

    std::vector<float> inputLeft_;
    std::vector<float> inputRight_;
    std::vector<float> outputLeft_;
    std::vector<float> outputRight_;

    std::vector<float> effectsBufferLeft_;
    std::vector<float> effectsBufferRight_;
    std::vector<float> effectOutputL_;
    std::vector<float> effectOutputR_;

    std::unique_ptr<signalsmith::basics::ChorusFloat> chorusEffect_;
    std::unique_ptr<signalsmith::basics::ReverbFloat> reverbEffect_;

    std::atomic<bool> chorusEnabled_;
    std::atomic<bool> reverbEnabled_;

    std::atomic<bool> eqEnabled_;

    std::atomic<float> chorusMix_;
    std::atomic<float> chorusDepthMs_;
    std::atomic<float> chorusDetune_;
    std::atomic<float> chorusStereo_;

    std::atomic<float> reverbDry_;
    std::atomic<float> reverbWet_;
    std::atomic<float> reverbRoomMs_;
    std::atomic<float> reverbDecaySec_;

    std::atomic<float> eqBand1Freq_;
    std::atomic<float> eqBand1Gain_;
    std::atomic<float> eqBand2Freq_;
    std::atomic<float> eqBand2Gain_;
    std::atomic<float> eqBand3Freq_;
    std::atomic<float> eqBand3Gain_;
    std::atomic<float> eqBand4Freq_;
    std::atomic<float> eqBand4Gain_;
    std::atomic<float> eqBand5Freq_;
    std::atomic<float> eqBand5Gain_;

    std::array<BiquadFilter, 5> eqFiltersL_;
    std::array<BiquadFilter, 5> eqFiltersR_;

    std::atomic<bool> toneFilterEnabled_{false};
    std::atomic<float> toneFilterLowCutHz_{80.0f};
    std::atomic<float> toneFilterHighCutHz_{12000.0f};
    std::atomic<float> toneFilterLowShelfDb_{0.0f};
    std::atomic<float> toneFilterHighShelfDb_{0.0f};
    std::array<BiquadFilter, 4> toneFiltersL_;
    std::array<BiquadFilter, 4> toneFiltersR_;
};

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeInit(
        JNIEnv*,
        jobject,
        jint sampleRate,
        jint channelCount) {

    auto* processor = new SignalsmithProcessor(sampleRate, channelCount);
    return reinterpret_cast<jlong>(processor);
}

JNIEXPORT jint JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeProcess(
        JNIEnv* env,
        jobject,
        jlong handle,
        jobject inputBuffer,
        jint inputBytes,
        jobject outputBuffer,
        jint maxOutputFrames) {

    if (handle == 0) {
        LOGE("nativeProcess: null handle");
        return 0;
    }

    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);

    auto* input = static_cast<short*>(env->GetDirectBufferAddress(inputBuffer));
    auto* output = static_cast<short*>(env->GetDirectBufferAddress(outputBuffer));

    if (input == nullptr || output == nullptr) {
        LOGE("nativeProcess: null buffer address");
        return 0;
    }

    return processor->process(input, inputBytes, output, maxOutputFrames);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetPitchSemitones(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat semitones) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setPitchSemitones(semitones);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetTempoRate(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat rate) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setTempoRate(rate);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeFlush(
        JNIEnv*,
        jobject,
        jlong handle) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->flush();
}

JNIEXPORT jint JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeFlushAndGetRemaining(
        JNIEnv* env,
        jobject,
        jlong handle,
        jobject outputBuffer,
        jint maxOutputFrames) {

    if (handle == 0) return 0;

    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    auto* output = static_cast<short*>(env->GetDirectBufferAddress(outputBuffer));

    if (output == nullptr) return 0;

    return processor->flushAndGetRemaining(output, maxOutputFrames);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeRelease(
        JNIEnv*,
        jobject,
        jlong handle) {

    if (handle == 0) return;

    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    delete processor;
    LOGD("Released processor");
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetChorusEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setChorusEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetChorusParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat mix,
        jfloat depthMs,
        jfloat detune,
        jfloat stereo) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setChorusParams(mix, depthMs, detune, stereo);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetReverbEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setReverbEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetReverbParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat dry,
        jfloat wet,
        jfloat roomMs,
        jfloat decaySec) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setReverbParams(dry, wet, roomMs, decaySec);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetEqEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setEqEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetEqBand(
        JNIEnv*,
        jobject,
        jlong handle,
        jint band,
        jfloat freq,
        jfloat gainDb) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setEqBand(band, freq, gainDb);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetToneFilterEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setToneFilterEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetToneFilterParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat lowCutHz,
        jfloat highCutHz,
        jfloat lowShelfDb,
        jfloat highShelfDb) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setToneFilterParams(lowCutHz, highCutHz, lowShelfDb, highShelfDb);
}

}
