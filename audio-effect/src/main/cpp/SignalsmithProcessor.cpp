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
#include "signalsmith-basics/limiter.h"
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
        , limiterEnabled_(false)
        , reverbEnabled_(false)
        , eqEnabled_(false)
        , pitchDetectionEnabled_(false)
        , chorusMix_(0.5f)
        , chorusDepthMs_(10.0f)
        , chorusDetune_(10.0f)
        , chorusStereo_(0.5f)
        , limiterInputGainDb_(0.0f)
        , limiterLimitDb_(-3.0f)
        , limiterAttackMs_(10.0f)
        , limiterReleaseMs_(100.0f)
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
        , detectedPitch_(0.0f)
{
        effectToggleSmoothingCoeff_ = computeSmoothingCoeff(EFFECT_TOGGLE_SMOOTHING_MS);
        initPresenceBoostFilter();

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

        limiterEffect_ = std::make_unique<signalsmith::basics::LimiterFloat>(100.0f);
        limiterEffect_->configure(sampleRate_, PROCESS_BLOCK_FRAMES, channelCount_);
        limiterEffect_->reset();

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

        if (shouldBypass()) {
            std::memcpy(
                output,
                input,
                static_cast<size_t>(framesToProcess) * static_cast<size_t>(channelCount_) * sizeof(short)
            );
            return framesToProcess;
        }

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

    void setChorusEnabled(bool enabled) {
        chorusEnabled_.store(enabled, std::memory_order_relaxed);
        chorusWetTarget_.store(enabled ? 1.0f : 0.0f, std::memory_order_relaxed);
    }
    void setChorusParams(float mix, float depthMs, float detune, float stereo) {
        chorusMix_.store(mix, std::memory_order_relaxed);
        chorusDepthMs_.store(depthMs, std::memory_order_relaxed);
        chorusDetune_.store(detune, std::memory_order_relaxed);
        chorusStereo_.store(stereo, std::memory_order_relaxed);
    }

    void setLimiterEnabled(bool enabled) {
        limiterEnabled_.store(enabled, std::memory_order_relaxed);
        limiterWetTarget_.store(enabled ? 1.0f : 0.0f, std::memory_order_relaxed);
    }
    void setLimiterParams(float inputGainDb, float limitDb, float attackMs, float releaseMs) {
        limiterInputGainDb_.store(inputGainDb, std::memory_order_relaxed);
        limiterLimitDb_.store(limitDb, std::memory_order_relaxed);
        limiterAttackMs_.store(attackMs, std::memory_order_relaxed);
        limiterReleaseMs_.store(releaseMs, std::memory_order_relaxed);
    }

    void setReverbEnabled(bool enabled) {
        reverbEnabled_.store(enabled, std::memory_order_relaxed);
        reverbWetTarget_.store(enabled ? 1.0f : 0.0f, std::memory_order_relaxed);
    }
    void setReverbParams(float dry, float wet, float roomMs, float decaySec) {
        reverbDry_.store(dry, std::memory_order_relaxed);
        reverbWet_.store(wet, std::memory_order_relaxed);
        reverbRoomMs_.store(roomMs, std::memory_order_relaxed);
        reverbDecaySec_.store(decaySec, std::memory_order_relaxed);
    }

    void setEqEnabled(bool enabled) {
        eqEnabled_.store(enabled, std::memory_order_relaxed);
        eqWetTarget_.store(enabled ? 1.0f : 0.0f, std::memory_order_relaxed);
        eqDirty_.store(true, std::memory_order_relaxed);
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
        eqDirty_.store(true, std::memory_order_relaxed);
    }

    void setPitchDetectionEnabled(bool enabled) { pitchDetectionEnabled_.store(enabled, std::memory_order_relaxed); }
    float getDetectedPitch() const { return detectedPitch_.load(std::memory_order_relaxed); }

private:
    using BiquadFilter = signalsmith::filters::BiquadStatic<float>;

    void initPresenceBoostFilter() {
    }

    void refreshEqFiltersIfNeeded() {
        bool coeffsNeedUpdate =
            eqDirty_.exchange(false, std::memory_order_relaxed) ||
            !eqFiltersConfigured_.load(std::memory_order_relaxed);

        const float targetFreqs[5] = {
            eqBand1Freq_.load(std::memory_order_relaxed),
            eqBand2Freq_.load(std::memory_order_relaxed),
            eqBand3Freq_.load(std::memory_order_relaxed),
            eqBand4Freq_.load(std::memory_order_relaxed),
            eqBand5Freq_.load(std::memory_order_relaxed),
        };
        const float targetGains[5] = {
            eqBand1Gain_.load(std::memory_order_relaxed),
            eqBand2Gain_.load(std::memory_order_relaxed),
            eqBand3Gain_.load(std::memory_order_relaxed),
            eqBand4Gain_.load(std::memory_order_relaxed),
            eqBand5Gain_.load(std::memory_order_relaxed),
        };

        for (int b = 0; b < 5; ++b) {
            const float nextFreq = eqSmoothedFreq_[b] + (targetFreqs[b] - eqSmoothedFreq_[b]) * EQ_PARAM_SMOOTHING;
            const float nextGain = eqSmoothedGain_[b] + (targetGains[b] - eqSmoothedGain_[b]) * EQ_PARAM_SMOOTHING;
            if (std::fabs(nextFreq - eqSmoothedFreq_[b]) > 1e-4f ||
                std::fabs(nextGain - eqSmoothedGain_[b]) > 1e-4f) {
                coeffsNeedUpdate = true;
            }
            eqSmoothedFreq_[b] = nextFreq;
            eqSmoothedGain_[b] = nextGain;
        }

        if (!coeffsNeedUpdate) return;

        const float invSampleRate = 1.0f / static_cast<float>(sampleRate_);
        eqFiltersL_[0].lowShelfDb(eqSmoothedFreq_[0] * invSampleRate, eqSmoothedGain_[0]);
        eqFiltersR_[0].lowShelfDb(eqSmoothedFreq_[0] * invSampleRate, eqSmoothedGain_[0]);
        for (int b = 1; b < 4; ++b) {
            eqFiltersL_[b].peakDb(eqSmoothedFreq_[b] * invSampleRate, eqSmoothedGain_[b], 1.0);
            eqFiltersR_[b].peakDb(eqSmoothedFreq_[b] * invSampleRate, eqSmoothedGain_[b], 1.0);
        }
        eqFiltersL_[4].highShelfDb(eqSmoothedFreq_[4] * invSampleRate, eqSmoothedGain_[4]);
        eqFiltersR_[4].highShelfDb(eqSmoothedFreq_[4] * invSampleRate, eqSmoothedGain_[4]);
        eqFiltersConfigured_.store(true, std::memory_order_relaxed);
    }

    bool shouldBypass() const {
        if (pitchSemitones_.load(std::memory_order_relaxed) != 0.0f) return false;

        if (isWetActive(chorusWetCurrent_, chorusWetTarget_.load(std::memory_order_relaxed))) return false;
        if (isWetActive(limiterWetCurrent_, limiterWetTarget_.load(std::memory_order_relaxed))) return false;
        if (isWetActive(reverbWetCurrent_, reverbWetTarget_.load(std::memory_order_relaxed))) return false;
        if (isWetActive(eqWetCurrent_, eqWetTarget_.load(std::memory_order_relaxed))) return false;

        return true;
    }

    void processBlock(const short* input, short* output, int frames) {
        shortToFloatDeinterleaved(input, frames);

        const float pitch = pitchSemitones_.load(std::memory_order_relaxed);
        const bool needPitch = pitch != 0.0f;

        if (needPitch) {
            stretch_.setTransposeSemitones(pitch);

            float* inputPtrs[2] = {inputLeft_.data(), inputRight_.data()};
            float* outputPtrs[2] = {outputLeft_.data(), outputRight_.data()};
            stretch_.process(inputPtrs, frames, outputPtrs, frames);

            std::memcpy(effectsBufferLeft_.data(), outputLeft_.data(), static_cast<size_t>(frames) * sizeof(float));
            std::memcpy(effectsBufferRight_.data(), outputRight_.data(), static_cast<size_t>(frames) * sizeof(float));
        } else {
            std::memcpy(effectsBufferLeft_.data(), inputLeft_.data(), static_cast<size_t>(frames) * sizeof(float));
            std::memcpy(effectsBufferRight_.data(), inputRight_.data(), static_cast<size_t>(frames) * sizeof(float));
        }

        applyEffects(frames);

        floatToShortInterleaved(effectsBufferLeft_.data(), effectsBufferRight_.data(), output, frames);
    }

    void applyEffects(int frames) {
        float* effectInPtrs[2] = {effectsBufferLeft_.data(), effectsBufferRight_.data()};
        float* effectOutPtrs[2] = {effectOutputL_.data(), effectOutputR_.data()};

        const float chorusTargetWet = chorusWetTarget_.load(std::memory_order_relaxed);
        if (chorusEffect_ &&
            (chorusEnabled_.load(std::memory_order_relaxed) || isWetActive(chorusWetCurrent_, chorusTargetWet))) {
            chorusEffect_->mix = chorusMix_.load(std::memory_order_relaxed);
            chorusEffect_->depthMs = chorusDepthMs_.load(std::memory_order_relaxed);
            chorusEffect_->detune = chorusDetune_.load(std::memory_order_relaxed);
            chorusEffect_->stereo = chorusStereo_.load(std::memory_order_relaxed);

            chorusEffect_->process(effectInPtrs, effectOutPtrs, frames);
            mixEffectOutputWithWet(
                effectOutputL_.data(),
                effectOutputR_.data(),
                frames,
                chorusWetCurrent_,
                chorusTargetWet
            );
        }

        const float limiterTargetWet = limiterWetTarget_.load(std::memory_order_relaxed);
        if (limiterEffect_ &&
            (limiterEnabled_.load(std::memory_order_relaxed) || isWetActive(limiterWetCurrent_, limiterTargetWet))) {
            const float gainDb = limiterInputGainDb_.load(std::memory_order_relaxed);
            limiterEffect_->inputGain = std::pow(10.0f, gainDb / 20.0f);
            limiterEffect_->outputLimit = std::pow(10.0f, limiterLimitDb_.load(std::memory_order_relaxed) / 20.0f);
            limiterEffect_->attackMs = limiterAttackMs_.load(std::memory_order_relaxed);
            limiterEffect_->releaseMs = limiterReleaseMs_.load(std::memory_order_relaxed);

            limiterEffect_->process(effectInPtrs, effectOutPtrs, frames);
            mixEffectOutputWithWet(
                effectOutputL_.data(),
                effectOutputR_.data(),
                frames,
                limiterWetCurrent_,
                limiterTargetWet
            );
        }

        const float reverbTargetWet = reverbWetTarget_.load(std::memory_order_relaxed);
        if (reverbEffect_ &&
            (reverbEnabled_.load(std::memory_order_relaxed) || isWetActive(reverbWetCurrent_, reverbTargetWet))) {
            reverbEffect_->dry = reverbDry_.load(std::memory_order_relaxed);
            reverbEffect_->wet = reverbWet_.load(std::memory_order_relaxed);
            reverbEffect_->roomMs = reverbRoomMs_.load(std::memory_order_relaxed);
            reverbEffect_->rt20 = reverbDecaySec_.load(std::memory_order_relaxed);

            reverbEffect_->process(effectInPtrs, effectOutPtrs, frames);
            mixEffectOutputWithWet(
                effectOutputL_.data(),
                effectOutputR_.data(),
                frames,
                reverbWetCurrent_,
                reverbTargetWet
            );
        }

        const float eqTargetWet = eqWetTarget_.load(std::memory_order_relaxed);
        if (eqEnabled_.load(std::memory_order_relaxed) || isWetActive(eqWetCurrent_, eqTargetWet)) {
            refreshEqFiltersIfNeeded();
            for (int i = 0; i < frames; i++) {
                float sampleL = effectsBufferLeft_[i];
                float sampleR = effectsBufferRight_[i];
                for (int b = 0; b < 5; b++) {
                    sampleL = eqFiltersL_[b](sampleL);
                    sampleR = eqFiltersR_[b](sampleR);
                }
                effectOutputL_[i] = sampleL;
                effectOutputR_[i] = sampleR;
            }
            mixEffectOutputWithWet(
                effectOutputL_.data(),
                effectOutputR_.data(),
                frames,
                eqWetCurrent_,
                eqTargetWet
            );
        }

    }

    float computeSmoothingCoeff(float smoothingMs) const {
        const float tauSec = std::max(1.0f, smoothingMs) * 0.001f;
        const float coeff = 1.0f - std::exp(-1.0f / (tauSec * static_cast<float>(sampleRate_)));
        return std::max(0.0f, std::min(1.0f, coeff));
    }

    bool isWetActive(float currentWet, float targetWet) const {
        return currentWet > 1e-4f || targetWet > 1e-4f;
    }

    float advanceWet(float& currentWet, float targetWet) {
        currentWet += (targetWet - currentWet) * effectToggleSmoothingCoeff_;
        if (std::fabs(targetWet - currentWet) < 1e-5f) {
            currentWet = targetWet;
        }
        return currentWet;
    }

    void mixEffectOutputWithWet(
        const float* wetL,
        const float* wetR,
        int frames,
        float& wetCurrent,
        float wetTarget
    ) {
        for (int i = 0; i < frames; ++i) {
            const float wet = advanceWet(wetCurrent, wetTarget);
            const float dry = 1.0f - wet;
            effectsBufferLeft_[i] = effectsBufferLeft_[i] * dry + wetL[i] * wet;
            effectsBufferRight_[i] = effectsBufferRight_[i] * dry + wetR[i] * wet;
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
    std::unique_ptr<signalsmith::basics::LimiterFloat> limiterEffect_;
    std::unique_ptr<signalsmith::basics::ReverbFloat> reverbEffect_;

    std::atomic<bool> chorusEnabled_;
    std::atomic<bool> limiterEnabled_;
    std::atomic<bool> reverbEnabled_;
    std::atomic<float> chorusWetTarget_{0.0f};
    std::atomic<float> limiterWetTarget_{0.0f};
    std::atomic<float> reverbWetTarget_{0.0f};
    float chorusWetCurrent_ = 0.0f;
    float limiterWetCurrent_ = 0.0f;
    float reverbWetCurrent_ = 0.0f;

    std::atomic<bool> eqEnabled_;
    std::atomic<float> eqWetTarget_{0.0f};
    float eqWetCurrent_ = 0.0f;
    float effectToggleSmoothingCoeff_ = 0.0f;
    static constexpr float EFFECT_TOGGLE_SMOOTHING_MS = 24.0f;

    std::atomic<bool> pitchDetectionEnabled_;
    std::atomic<float> detectedPitch_;

    std::atomic<float> chorusMix_;
    std::atomic<float> chorusDepthMs_;
    std::atomic<float> chorusDetune_;
    std::atomic<float> chorusStereo_;

    std::atomic<float> limiterInputGainDb_;
    std::atomic<float> limiterLimitDb_;
    std::atomic<float> limiterAttackMs_;
    std::atomic<float> limiterReleaseMs_;

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
    std::atomic<bool> eqDirty_{true};
    std::atomic<bool> eqFiltersConfigured_{false};
    std::array<float, 5> eqSmoothedFreq_ = {60.0f, 250.0f, 1000.0f, 4000.0f, 12000.0f};
    std::array<float, 5> eqSmoothedGain_ = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
    static constexpr float EQ_PARAM_SMOOTHING = 0.20f;
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
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetLimiterEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setLimiterEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetLimiterParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat inputGainDb,
        jfloat limitDb,
        jfloat attackMs,
        jfloat releaseMs) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setLimiterParams(inputGainDb, limitDb, attackMs, releaseMs);
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
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetPitchDetectionEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setPitchDetectionEnabled(enabled);
}

JNIEXPORT jfloat JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeGetDetectedPitch(
        JNIEnv*,
        jobject,
        jlong handle) {

    if (handle == 0) return 0.0f;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    return processor->getDetectedPitch();
}

}
