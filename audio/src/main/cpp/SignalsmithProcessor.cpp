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
#include "signalsmith-basics/crunch.h"
#include "signalsmith/basics/modules/dsp/filters.h"

#include "mit_hrtf_lib.h"
#include "FFTConvolver.h"

#include "Effects/phaser.h"
#include "Effects/flanger.h"
#include "Effects/tremolo.h"
#include "Effects/autowah.h"
#include "Effects/decimator.h"

#define LOG_TAG "SignalsmithProc"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {
constexpr int PROCESS_BLOCK_FRAMES = 512;
constexpr int HRTF_SAMPLE_RATE = 44100;
constexpr int HRTF_ELEVATION = 0;
constexpr int HRTF_SUBJECT = 1;
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
        , crunchEnabled_(false)
        , eqEnabled_(false)
        , compressorEnabled_(false)
        , pitchDetectionEnabled_(false)
        , hrtfEnabled_(false)
        , phaserEnabled_(false)
        , flangerEnabled_(false)
        , tremoloEnabled_(false)
        , autowahEnabled_(false)
        , decimatorEnabled_(false)
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
        , crunchDriveDb_(0.0f)
        , crunchFuzz_(0.0f)
        , crunchToneHz_(5000.0f)
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
        , compThresholdDb_(-20.0f)
        , compRatio_(4.0f)
        , compAttackMs_(10.0f)
        , compReleaseMs_(100.0f)
        , compMakeupGainDb_(0.0f)
        , detectedPitch_(0.0f)
        , hrtfIntensity_(1.0f)
        , hrtfAzimuth_(30)
        , hrtfInitialized_(false)
        , hrtfCurrentAzimuth_(0)
        , phaserLfoFreq_(0.5f)
        , phaserLfoDepth_(0.5f)
        , phaserFeedback_(0.7f)
        , phaserPoles_(4)
        , flangerLfoFreq_(0.2f)
        , flangerLfoDepth_(0.5f)
        , flangerFeedback_(0.5f)
        , flangerDelayMs_(3.0f)
        , tremoloFreq_(5.0f)
        , tremoloDepth_(0.5f)
        , tremoloWaveform_(0)
        , autowahWah_(0.5f)
        , autowahMix_(50.0f)
        , autowahLevel_(0.5f)
        , decimatorBitcrush_(0.5f)
        , decimatorDownsample_(0.5f) {

        stretch_.presetDefault(channelCount_, sampleRate_);

        inputLeft_.resize(PROCESS_BLOCK_FRAMES);
        inputRight_.resize(PROCESS_BLOCK_FRAMES);
        outputLeft_.resize(PROCESS_BLOCK_FRAMES);
        outputRight_.resize(PROCESS_BLOCK_FRAMES);

        effectsBufferLeft_.resize(PROCESS_BLOCK_FRAMES);
        effectsBufferRight_.resize(PROCESS_BLOCK_FRAMES);
        effectOutputL_.resize(PROCESS_BLOCK_FRAMES);
        effectOutputR_.resize(PROCESS_BLOCK_FRAMES);

        hrtfTempBufferL_.resize(PROCESS_BLOCK_FRAMES);
        hrtfTempBufferR_.resize(PROCESS_BLOCK_FRAMES);

        chorusEffect_ = std::make_unique<signalsmith::basics::ChorusFloat>(50.0f);
        chorusEffect_->configure(sampleRate_, PROCESS_BLOCK_FRAMES, channelCount_);
        chorusEffect_->reset();

        limiterEffect_ = std::make_unique<signalsmith::basics::LimiterFloat>(100.0f);
        limiterEffect_->configure(sampleRate_, PROCESS_BLOCK_FRAMES, channelCount_);
        limiterEffect_->reset();

        reverbEffect_ = std::make_unique<signalsmith::basics::ReverbFloat>(200.0f, 2.0f);
        reverbEffect_->configure(sampleRate_, PROCESS_BLOCK_FRAMES, channelCount_ == 2 ? 2 : 1);
        reverbEffect_->reset();

        crunchEffect_ = std::make_unique<signalsmith::basics::CrunchFloat>(true);
        crunchEffect_->configure(sampleRate_, PROCESS_BLOCK_FRAMES, channelCount_);
        crunchEffect_->reset();

        phaserL_.Init(sampleRate_);
        phaserR_.Init(sampleRate_);
        phaserL_.SetPoles(4);
        phaserR_.SetPoles(4);

        flangerL_.Init(sampleRate_);
        flangerR_.Init(sampleRate_);

        tremoloL_.Init(sampleRate_);
        tremoloR_.Init(sampleRate_);

        autowahL_.Init(sampleRate_);
        autowahR_.Init(sampleRate_);

        decimatorL_.Init();
        decimatorR_.Init();

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

    void setChorusEnabled(bool enabled) { chorusEnabled_.store(enabled, std::memory_order_relaxed); }
    void setChorusParams(float mix, float depthMs, float detune, float stereo) {
        chorusMix_.store(mix, std::memory_order_relaxed);
        chorusDepthMs_.store(depthMs, std::memory_order_relaxed);
        chorusDetune_.store(detune, std::memory_order_relaxed);
        chorusStereo_.store(stereo, std::memory_order_relaxed);
    }

    void setLimiterEnabled(bool enabled) { limiterEnabled_.store(enabled, std::memory_order_relaxed); }
    void setLimiterParams(float inputGainDb, float limitDb, float attackMs, float releaseMs) {
        limiterInputGainDb_.store(inputGainDb, std::memory_order_relaxed);
        limiterLimitDb_.store(limitDb, std::memory_order_relaxed);
        limiterAttackMs_.store(attackMs, std::memory_order_relaxed);
        limiterReleaseMs_.store(releaseMs, std::memory_order_relaxed);
    }

    void setReverbEnabled(bool enabled) { reverbEnabled_.store(enabled, std::memory_order_relaxed); }
    void setReverbParams(float dry, float wet, float roomMs, float decaySec) {
        reverbDry_.store(dry, std::memory_order_relaxed);
        reverbWet_.store(wet, std::memory_order_relaxed);
        reverbRoomMs_.store(roomMs, std::memory_order_relaxed);
        reverbDecaySec_.store(decaySec, std::memory_order_relaxed);
    }

    void setCrunchEnabled(bool enabled) { crunchEnabled_.store(enabled, std::memory_order_relaxed); }
    void setCrunchParams(float driveDb, float fuzz, float toneHz) {
        crunchDriveDb_.store(driveDb, std::memory_order_relaxed);
        crunchFuzz_.store(fuzz, std::memory_order_relaxed);
        crunchToneHz_.store(toneHz, std::memory_order_relaxed);
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

    void setCompressorEnabled(bool enabled) {
        compressorEnabled_.store(enabled, std::memory_order_relaxed);
        if (!enabled) {
            compEnvelope_ = 0.0f;
        }
    }

    void setCompressorParams(float thresholdDb, float ratio, float attackMs, float releaseMs, float makeupGainDb) {
        compThresholdDb_.store(thresholdDb, std::memory_order_relaxed);
        compRatio_.store(ratio, std::memory_order_relaxed);
        compAttackMs_.store(attackMs, std::memory_order_relaxed);
        compReleaseMs_.store(releaseMs, std::memory_order_relaxed);
        compMakeupGainDb_.store(makeupGainDb, std::memory_order_relaxed);
    }

    void setPitchDetectionEnabled(bool enabled) { pitchDetectionEnabled_.store(enabled, std::memory_order_relaxed); }
    float getDetectedPitch() const { return detectedPitch_.load(std::memory_order_relaxed); }

    void setHrtfEnabled(bool enabled) {
        hrtfEnabled_.store(enabled, std::memory_order_relaxed);
        if (!enabled) {
            hrtfInitialized_ = false;
        }
    }

    void setHrtfParams(float intensity, int azimuth) {
        hrtfIntensity_.store(intensity, std::memory_order_relaxed);
        hrtfAzimuth_.store(azimuth, std::memory_order_relaxed);
    }

    void setPhaserEnabled(bool enabled) { phaserEnabled_.store(enabled, std::memory_order_relaxed); }
    void setPhaserParams(float lfoFreq, float lfoDepth, float feedback, int poles) {
        phaserLfoFreq_.store(lfoFreq, std::memory_order_relaxed);
        phaserLfoDepth_.store(lfoDepth, std::memory_order_relaxed);
        phaserFeedback_.store(feedback, std::memory_order_relaxed);
        phaserPoles_.store(poles, std::memory_order_relaxed);
    }

    void setFlangerEnabled(bool enabled) { flangerEnabled_.store(enabled, std::memory_order_relaxed); }
    void setFlangerParams(float lfoFreq, float lfoDepth, float feedback, float delayMs) {
        flangerLfoFreq_.store(lfoFreq, std::memory_order_relaxed);
        flangerLfoDepth_.store(lfoDepth, std::memory_order_relaxed);
        flangerFeedback_.store(feedback, std::memory_order_relaxed);
        flangerDelayMs_.store(delayMs, std::memory_order_relaxed);
    }

    void setTremoloEnabled(bool enabled) { tremoloEnabled_.store(enabled, std::memory_order_relaxed); }
    void setTremoloParams(float freq, float depth, int waveform) {
        tremoloFreq_.store(freq, std::memory_order_relaxed);
        tremoloDepth_.store(depth, std::memory_order_relaxed);
        tremoloWaveform_.store(waveform, std::memory_order_relaxed);
    }

    void setAutowahEnabled(bool enabled) { autowahEnabled_.store(enabled, std::memory_order_relaxed); }
    void setAutowahParams(float wah, float mix, float level) {
        autowahWah_.store(wah, std::memory_order_relaxed);
        autowahMix_.store(mix, std::memory_order_relaxed);
        autowahLevel_.store(level, std::memory_order_relaxed);
    }

    void setDecimatorEnabled(bool enabled) { decimatorEnabled_.store(enabled, std::memory_order_relaxed); }
    void setDecimatorParams(float bitcrush, float downsample) {
        decimatorBitcrush_.store(bitcrush, std::memory_order_relaxed);
        decimatorDownsample_.store(downsample, std::memory_order_relaxed);
    }

private:
    using BiquadFilter = signalsmith::filters::BiquadStatic<float>;

    bool shouldBypass() const {
        if (pitchSemitones_.load(std::memory_order_relaxed) != 0.0f) return false;

        if (chorusEnabled_.load(std::memory_order_relaxed)) return false;
        if (limiterEnabled_.load(std::memory_order_relaxed)) return false;
        if (reverbEnabled_.load(std::memory_order_relaxed)) return false;
        if (crunchEnabled_.load(std::memory_order_relaxed)) return false;
        if (eqEnabled_.load(std::memory_order_relaxed)) return false;
        if (compressorEnabled_.load(std::memory_order_relaxed)) return false;
        if (hrtfEnabled_.load(std::memory_order_relaxed)) return false;
        if (phaserEnabled_.load(std::memory_order_relaxed)) return false;
        if (flangerEnabled_.load(std::memory_order_relaxed)) return false;
        if (tremoloEnabled_.load(std::memory_order_relaxed)) return false;
        if (autowahEnabled_.load(std::memory_order_relaxed)) return false;
        if (decimatorEnabled_.load(std::memory_order_relaxed)) return false;

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

        if (chorusEnabled_.load(std::memory_order_relaxed) && chorusEffect_) {
            chorusEffect_->mix = chorusMix_.load(std::memory_order_relaxed);
            chorusEffect_->depthMs = chorusDepthMs_.load(std::memory_order_relaxed);
            chorusEffect_->detune = chorusDetune_.load(std::memory_order_relaxed);
            chorusEffect_->stereo = chorusStereo_.load(std::memory_order_relaxed);

            chorusEffect_->process(effectInPtrs, effectOutPtrs, frames);
            std::memcpy(effectsBufferLeft_.data(), effectOutputL_.data(), static_cast<size_t>(frames) * sizeof(float));
            std::memcpy(effectsBufferRight_.data(), effectOutputR_.data(), static_cast<size_t>(frames) * sizeof(float));
        }

        if (limiterEnabled_.load(std::memory_order_relaxed) && limiterEffect_) {
            const float gainDb = limiterInputGainDb_.load(std::memory_order_relaxed);
            limiterEffect_->inputGain = std::pow(10.0f, gainDb / 20.0f);
            limiterEffect_->outputLimit = std::pow(10.0f, limiterLimitDb_.load(std::memory_order_relaxed) / 20.0f);
            limiterEffect_->attackMs = limiterAttackMs_.load(std::memory_order_relaxed);
            limiterEffect_->releaseMs = limiterReleaseMs_.load(std::memory_order_relaxed);

            limiterEffect_->process(effectInPtrs, effectOutPtrs, frames);
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

        if (crunchEnabled_.load(std::memory_order_relaxed) && crunchEffect_) {
            const float driveDb = crunchDriveDb_.load(std::memory_order_relaxed);
            crunchEffect_->drive = std::pow(10.0f, driveDb / 20.0f);
            crunchEffect_->fuzz = crunchFuzz_.load(std::memory_order_relaxed);
            crunchEffect_->toneHz = crunchToneHz_.load(std::memory_order_relaxed);

            crunchEffect_->process(effectInPtrs, effectOutPtrs, frames);
            std::memcpy(effectsBufferLeft_.data(), effectOutputL_.data(), static_cast<size_t>(frames) * sizeof(float));
            std::memcpy(effectsBufferRight_.data(), effectOutputR_.data(), static_cast<size_t>(frames) * sizeof(float));
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

        if (compressorEnabled_.load(std::memory_order_relaxed)) {
            const float threshold = std::pow(10.0f, compThresholdDb_.load(std::memory_order_relaxed) / 20.0f);
            const float ratio = compRatio_.load(std::memory_order_relaxed);
            const float attackCoef = std::exp(-1.0f / (compAttackMs_.load(std::memory_order_relaxed) * 0.001f * static_cast<float>(sampleRate_)));
            const float releaseCoef = std::exp(-1.0f / (compReleaseMs_.load(std::memory_order_relaxed) * 0.001f * static_cast<float>(sampleRate_)));
            const float makeupGain = std::pow(10.0f, compMakeupGainDb_.load(std::memory_order_relaxed) / 20.0f);

            for (int i = 0; i < frames; i++) {
                const float inputL = std::fabs(effectsBufferLeft_[i]);
                const float inputR = std::fabs(effectsBufferRight_[i]);
                const float inputPeak = std::max(inputL, inputR);

                const float targetEnv = inputPeak;
                const float coef = (targetEnv > compEnvelope_) ? attackCoef : releaseCoef;
                compEnvelope_ = coef * compEnvelope_ + (1.0f - coef) * targetEnv;

                float gainReduction = 1.0f;
                if (compEnvelope_ > threshold) {
                    const float overDb = 20.0f * std::log10(compEnvelope_ / threshold);
                    const float reducedDb = overDb * (1.0f - 1.0f / ratio);
                    gainReduction = std::pow(10.0f, -reducedDb / 20.0f);
                }

                effectsBufferLeft_[i] *= gainReduction * makeupGain;
                effectsBufferRight_[i] *= gainReduction * makeupGain;
            }
        }

        if (hrtfEnabled_.load(std::memory_order_relaxed)) {
            const int azimuth = hrtfAzimuth_.load(std::memory_order_relaxed);
            const float intensity = hrtfIntensity_.load(std::memory_order_relaxed);

            if (!hrtfInitialized_ || hrtfCurrentAzimuth_ != azimuth) {
                maybeInitHrtf(azimuth);
            }

            if (hrtfInitialized_) {
                hrtfConvolverL_.process(effectsBufferLeft_.data(), hrtfTempBufferL_.data(), frames);
                hrtfConvolverR_.process(effectsBufferRight_.data(), hrtfTempBufferR_.data(), frames);

                for (int i = 0; i < frames; i++) {
                    effectsBufferLeft_[i] = effectsBufferLeft_[i] * (1.0f - intensity) + hrtfTempBufferL_[i] * intensity;
                    effectsBufferRight_[i] = effectsBufferRight_[i] * (1.0f - intensity) + hrtfTempBufferR_[i] * intensity;
                }
            }
        }

        if (phaserEnabled_.load(std::memory_order_relaxed)) {
            const float lfoFreq = phaserLfoFreq_.load(std::memory_order_relaxed);
            const float lfoDepth = phaserLfoDepth_.load(std::memory_order_relaxed);
            const float feedback = phaserFeedback_.load(std::memory_order_relaxed);
            const int poles = phaserPoles_.load(std::memory_order_relaxed);

            phaserL_.SetLfoFreq(lfoFreq);
            phaserR_.SetLfoFreq(lfoFreq);
            phaserL_.SetLfoDepth(lfoDepth);
            phaserR_.SetLfoDepth(lfoDepth);
            phaserL_.SetFeedback(feedback);
            phaserR_.SetFeedback(feedback);
            phaserL_.SetPoles(poles);
            phaserR_.SetPoles(poles);

            for (int i = 0; i < frames; i++) {
                effectsBufferLeft_[i] = phaserL_.Process(effectsBufferLeft_[i]);
                effectsBufferRight_[i] = phaserR_.Process(effectsBufferRight_[i]);
            }
        }

        if (flangerEnabled_.load(std::memory_order_relaxed)) {
            const float lfoFreq = flangerLfoFreq_.load(std::memory_order_relaxed);
            const float lfoDepth = flangerLfoDepth_.load(std::memory_order_relaxed);
            const float feedback = flangerFeedback_.load(std::memory_order_relaxed);
            const float delayMs = flangerDelayMs_.load(std::memory_order_relaxed);

            flangerL_.SetLfoFreq(lfoFreq);
            flangerR_.SetLfoFreq(lfoFreq);
            flangerL_.SetLfoDepth(lfoDepth);
            flangerR_.SetLfoDepth(lfoDepth);
            flangerL_.SetFeedback(feedback);
            flangerR_.SetFeedback(feedback);
            flangerL_.SetDelayMs(delayMs);
            flangerR_.SetDelayMs(delayMs);

            for (int i = 0; i < frames; i++) {
                effectsBufferLeft_[i] = flangerL_.Process(effectsBufferLeft_[i]);
                effectsBufferRight_[i] = flangerR_.Process(effectsBufferRight_[i]);
            }
        }

        if (tremoloEnabled_.load(std::memory_order_relaxed)) {
            const float freq = tremoloFreq_.load(std::memory_order_relaxed);
            const float depth = tremoloDepth_.load(std::memory_order_relaxed);
            const int waveform = tremoloWaveform_.load(std::memory_order_relaxed);

            tremoloL_.SetFreq(freq);
            tremoloR_.SetFreq(freq);
            tremoloL_.SetDepth(depth);
            tremoloR_.SetDepth(depth);
            tremoloL_.SetWaveform(waveform);
            tremoloR_.SetWaveform(waveform);

            for (int i = 0; i < frames; i++) {
                effectsBufferLeft_[i] = tremoloL_.Process(effectsBufferLeft_[i]);
                effectsBufferRight_[i] = tremoloR_.Process(effectsBufferRight_[i]);
            }
        }

        if (autowahEnabled_.load(std::memory_order_relaxed)) {
            const float wah = autowahWah_.load(std::memory_order_relaxed);
            const float mix = autowahMix_.load(std::memory_order_relaxed);
            const float level = autowahLevel_.load(std::memory_order_relaxed);

            autowahL_.SetWah(wah);
            autowahR_.SetWah(wah);
            autowahL_.SetDryWet(mix);
            autowahR_.SetDryWet(mix);
            autowahL_.SetLevel(level);
            autowahR_.SetLevel(level);

            for (int i = 0; i < frames; i++) {
                effectsBufferLeft_[i] = autowahL_.Process(effectsBufferLeft_[i]);
                effectsBufferRight_[i] = autowahR_.Process(effectsBufferRight_[i]);
            }
        }

        if (decimatorEnabled_.load(std::memory_order_relaxed)) {
            const float bitcrush = decimatorBitcrush_.load(std::memory_order_relaxed);
            const float downsample = decimatorDownsample_.load(std::memory_order_relaxed);

            decimatorL_.SetBitcrushFactor(bitcrush);
            decimatorR_.SetBitcrushFactor(bitcrush);
            decimatorL_.SetDownsampleFactor(downsample);
            decimatorR_.SetDownsampleFactor(downsample);

            for (int i = 0; i < frames; i++) {
                effectsBufferLeft_[i] = decimatorL_.Process(effectsBufferLeft_[i]);
                effectsBufferRight_[i] = decimatorR_.Process(effectsBufferRight_[i]);
            }
        }
    }

    void maybeInitHrtf(int azimuth) {
        const unsigned int taps = mit_hrtf_availability(azimuth, HRTF_ELEVATION, HRTF_SAMPLE_RATE, HRTF_SUBJECT);
        if (taps == 0) {
            hrtfInitialized_ = false;
            return;
        }

        int actualAzimuth = azimuth;
        int actualElevation = HRTF_ELEVATION;
        std::vector<short> hrtfL(taps);
        std::vector<short> hrtfR(taps);
        mit_hrtf_get(&actualAzimuth, &actualElevation, HRTF_SAMPLE_RATE, HRTF_SUBJECT, hrtfL.data(), hrtfR.data());

        std::vector<float> irL(taps);
        std::vector<float> irR(taps);
        for (unsigned int i = 0; i < taps; i++) {
            irL[i] = hrtfL[i] / 32768.0f;
            irR[i] = hrtfR[i] / 32768.0f;
        }

        hrtfConvolverL_.init(PROCESS_BLOCK_FRAMES, irL.data(), taps);
        hrtfConvolverR_.init(PROCESS_BLOCK_FRAMES, irR.data(), taps);

        hrtfCurrentAzimuth_ = azimuth;
        hrtfInitialized_ = true;
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
    std::unique_ptr<signalsmith::basics::CrunchFloat> crunchEffect_;

    std::atomic<bool> chorusEnabled_;
    std::atomic<bool> limiterEnabled_;
    std::atomic<bool> reverbEnabled_;
    std::atomic<bool> crunchEnabled_;

    std::atomic<bool> eqEnabled_;
    std::atomic<bool> compressorEnabled_;

    std::atomic<bool> pitchDetectionEnabled_;
    std::atomic<float> detectedPitch_;

    std::atomic<bool> hrtfEnabled_;
    std::atomic<float> hrtfIntensity_;
    std::atomic<int> hrtfAzimuth_;
    fftconvolver::FFTConvolver hrtfConvolverL_;
    fftconvolver::FFTConvolver hrtfConvolverR_;
    bool hrtfInitialized_;
    int hrtfCurrentAzimuth_;
    std::vector<float> hrtfTempBufferL_;
    std::vector<float> hrtfTempBufferR_;

    daisysp::Phaser phaserL_;
    daisysp::Phaser phaserR_;
    daisysp::Flanger flangerL_;
    daisysp::Flanger flangerR_;
    daisysp::Tremolo tremoloL_;
    daisysp::Tremolo tremoloR_;
    daisysp::Autowah autowahL_;
    daisysp::Autowah autowahR_;
    daisysp::Decimator decimatorL_;
    daisysp::Decimator decimatorR_;

    std::atomic<bool> phaserEnabled_;
    std::atomic<bool> flangerEnabled_;
    std::atomic<bool> tremoloEnabled_;
    std::atomic<bool> autowahEnabled_;
    std::atomic<bool> decimatorEnabled_;

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

    std::atomic<float> crunchDriveDb_;
    std::atomic<float> crunchFuzz_;
    std::atomic<float> crunchToneHz_;

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

    std::atomic<float> compThresholdDb_;
    std::atomic<float> compRatio_;
    std::atomic<float> compAttackMs_;
    std::atomic<float> compReleaseMs_;
    std::atomic<float> compMakeupGainDb_;
    float compEnvelope_ = 0.0f;

    std::atomic<float> phaserLfoFreq_;
    std::atomic<float> phaserLfoDepth_;
    std::atomic<float> phaserFeedback_;
    std::atomic<int> phaserPoles_;

    std::atomic<float> flangerLfoFreq_;
    std::atomic<float> flangerLfoDepth_;
    std::atomic<float> flangerFeedback_;
    std::atomic<float> flangerDelayMs_;

    std::atomic<float> tremoloFreq_;
    std::atomic<float> tremoloDepth_;
    std::atomic<int> tremoloWaveform_;

    std::atomic<float> autowahWah_;
    std::atomic<float> autowahMix_;
    std::atomic<float> autowahLevel_;

    std::atomic<float> decimatorBitcrush_;
    std::atomic<float> decimatorDownsample_;
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
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetCrunchEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setCrunchEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetCrunchParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat driveDb,
        jfloat fuzz,
        jfloat toneHz) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setCrunchParams(driveDb, fuzz, toneHz);
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
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetCompressorEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setCompressorEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetCompressorParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat thresholdDb,
        jfloat ratio,
        jfloat attackMs,
        jfloat releaseMs,
        jfloat makeupGainDb) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setCompressorParams(thresholdDb, ratio, attackMs, releaseMs, makeupGainDb);
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

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetHrtfEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setHrtfEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetHrtfParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat intensity,
        jint azimuth) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setHrtfParams(intensity, azimuth);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetPhaserEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setPhaserEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetPhaserParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat lfoFreq,
        jfloat lfoDepth,
        jfloat feedback,
        jint poles) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setPhaserParams(lfoFreq, lfoDepth, feedback, poles);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetFlangerEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setFlangerEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetFlangerParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat lfoFreq,
        jfloat lfoDepth,
        jfloat feedback,
        jfloat delayMs) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setFlangerParams(lfoFreq, lfoDepth, feedback, delayMs);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetTremoloEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setTremoloEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetTremoloParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat freq,
        jfloat depth,
        jint waveform) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setTremoloParams(freq, depth, waveform);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetAutowahEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setAutowahEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetAutowahParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat wah,
        jfloat mix,
        jfloat level) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setAutowahParams(wah, mix, level);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetDecimatorEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setDecimatorEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetDecimatorParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat bitcrush,
        jfloat downsample) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setDecimatorParams(bitcrush, downsample);
}

}
