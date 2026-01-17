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

#include "mit_hrtf_lib.h"
#include "FFTConvolver.h"

// iir1 Library for DJ Filter
#include "Iir.h"

#define LOG_TAG "SignalsmithProc"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {
const int PROCESS_BLOCK_FRAMES = 512;
const int HRTF_SAMPLE_RATE = 44100;
const int HRTF_ELEVATION = 0;
const int HRTF_SUBJECT = 1;

// Gain compensation for HRTF processing - disabled to prevent distortion
// The binaural stereo approach preserves level well, no compensation needed
const float HRTF_GAIN_COMPENSATION = 1.0f;  // No boost - cleaner sound
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
        , compressorEnabled_(false)
        , pitchDetectionEnabled_(false)
        , hrtfEnabled_(false)
        , hrtfActiveBuffer_(0)
        , hrtfPendingBuffer_(-1)
        , hrtfCrossfadeProgress_(1.0f)
        , hrtfCrossfading_(false)
        , stereoWidenerEnabled_(false)
        , stereoWidenerWidth_(1.0f)
        , djFilterEnabled_(false)
        , djFilterPosition_(0.0f)  // -1.0 = full lowpass, 0.0 = bypass, 1.0 = full highpass
        , djFilterResonance_(0.7f) // Q factor
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
        , compThresholdDb_(-20.0f)
        , compRatio_(4.0f)
        , compAttackMs_(10.0f)
        , compReleaseMs_(100.0f)
        , compMakeupGainDb_(0.0f)
        , detectedPitch_(0.0f)
        , hrtfIntensity_(1.0f)
        , hrtfAzimuth_(0)  // 0 = Front, negative = Left, positive = Right
{
        // Initialize high-frequency compensation filter for HRTF
        // This compensates for the natural high-frequency roll-off in HRTF processing
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

        hrtfTempBufferL_.resize(PROCESS_BLOCK_FRAMES);
        hrtfTempBufferR_.resize(PROCESS_BLOCK_FRAMES);
        hrtfTempBufferL2_.resize(PROCESS_BLOCK_FRAMES);
        hrtfTempBufferR2_.resize(PROCESS_BLOCK_FRAMES);
        hrtfMonoBuffer_.resize(PROCESS_BLOCK_FRAMES);  // For correct HRTF processing

        // Pending convolver temp buffers for crossfade
        hrtfPendingTempBufferL_.resize(PROCESS_BLOCK_FRAMES);
        hrtfPendingTempBufferR_.resize(PROCESS_BLOCK_FRAMES);
        hrtfPendingTempBufferL2_.resize(PROCESS_BLOCK_FRAMES);
        hrtfPendingTempBufferR2_.resize(PROCESS_BLOCK_FRAMES);

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

        // Check sample rate mismatch with HRTF
        if (sampleRate_ != HRTF_SAMPLE_RATE) {
            LOGE("⚠️ SAMPLE RATE MISMATCH! Audio=%dHz, HRTF=%dHz - This may cause crackling!",
                 sampleRate_, HRTF_SAMPLE_RATE);
        } else {
            LOGD("✓ Sample rate OK: %dHz", sampleRate_);
        }
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
            // Reset old double-buffer system (legacy mono)
            hrtfInitialized_[0] = false;
            hrtfInitialized_[1] = false;
            hrtfCrossfading_ = false;
            hrtfCrossfadeProgress_ = 1.0f;
            // Reset binaural stereo double-buffer system
            hrtfBinauralInitialized_[0] = false;
            hrtfBinauralInitialized_[1] = false;
            hrtfBinauralActiveIndex_ = 0;
            hrtfBinauralPendingIndex_ = 1;
            hrtfBinauralCrossfading_ = false;
            hrtfBinauralCrossfadeProgress_ = 0.0f;
        }
    }

    void setHrtfParams(float intensity, int azimuth) {
        hrtfIntensity_.store(intensity, std::memory_order_relaxed);
        hrtfAzimuth_.store(azimuth, std::memory_order_relaxed);
    }

    void setStereoWidenerEnabled(bool enabled) { stereoWidenerEnabled_.store(enabled, std::memory_order_relaxed); }
    void setStereoWidenerParams(float width) {
        stereoWidenerWidth_.store(width, std::memory_order_relaxed);
    }

    void setDjFilterEnabled(bool enabled) {
        djFilterEnabled_.store(enabled, std::memory_order_relaxed);
        if (!enabled) {
            // Reset filters when disabled
            djFilterNeedsReset_ = true;
        }
    }
    void setDjFilterParams(float position, float resonance) {
        djFilterPosition_.store(position, std::memory_order_relaxed);
        djFilterResonance_.store(resonance, std::memory_order_relaxed);
    }

private:
    using BiquadFilter = signalsmith::filters::BiquadStatic<float>;

    // Presence boost filters disabled - they were causing harsh/tearing sound
    // The binaural stereo approach with virtual speakers preserves natural frequency response
    void initPresenceBoostFilter() {
        // No-op - presence boost disabled for cleaner, more natural sound
        // The MIT HRTF already has good high-frequency response
    }

    bool shouldBypass() const {
        if (pitchSemitones_.load(std::memory_order_relaxed) != 0.0f) return false;

        if (chorusEnabled_.load(std::memory_order_relaxed)) return false;
        if (limiterEnabled_.load(std::memory_order_relaxed)) return false;
        if (reverbEnabled_.load(std::memory_order_relaxed)) return false;
        if (eqEnabled_.load(std::memory_order_relaxed)) return false;
        if (compressorEnabled_.load(std::memory_order_relaxed)) return false;
        if (hrtfEnabled_.load(std::memory_order_relaxed)) return false;
        if (stereoWidenerEnabled_.load(std::memory_order_relaxed)) return false;
        if (djFilterEnabled_.load(std::memory_order_relaxed)) return false;

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
            const float rawIntensity = hrtfIntensity_.load(std::memory_order_relaxed);
            const float intensity = std::max(0.0f, std::min(1.0f, rawIntensity));

            // =========================================================================
            // 3D STEREO POSITIONING - Move entire stereo image in 3D space
            // =========================================================================
            // azimuth controls where the stereo image appears:
            // - azimuth = 0: front (normal stereo)
            // - azimuth = -90: left side
            // - azimuth = +90: right side
            // - azimuth = ±180: behind
            //
            // We keep L and R channels separate, but move their virtual speaker
            // positions based on the azimuth. This preserves stereo width while
            // moving the entire sound field in 3D space.
            // =========================================================================

            // Calculate virtual speaker positions offset by azimuth
            // Base stereo spread is ±30 degrees, then offset by azimuth
            const int stereoSpread = 30;
            int leftSpeakerAzimuth = azimuth - stereoSpread;
            int rightSpeakerAzimuth = azimuth + stereoSpread;

            // Wrap to -180 to 180 range
            if (leftSpeakerAzimuth < -180) leftSpeakerAzimuth += 360;
            if (leftSpeakerAzimuth > 180) leftSpeakerAzimuth -= 360;
            if (rightSpeakerAzimuth < -180) rightSpeakerAzimuth += 360;
            if (rightSpeakerAzimuth > 180) rightSpeakerAzimuth -= 360;

            // Initialize or update HRTF filters for both speaker positions
            maybeInitBinauralHrtf(leftSpeakerAzimuth, rightSpeakerAzimuth);

            const int active = hrtfBinauralActiveIndex_;
            const int pending = hrtfBinauralPendingIndex_;

            if (hrtfBinauralInitialized_[active]) {
                // Process LEFT channel through LEFT virtual speaker HRTF (active buffer)
                hrtfLeftSpeakerConvolverL_[active].process(effectsBufferLeft_.data(), hrtfTempBufferL_.data(), frames);
                hrtfLeftSpeakerConvolverR_[active].process(effectsBufferLeft_.data(), hrtfTempBufferR_.data(), frames);

                // Process RIGHT channel through RIGHT virtual speaker HRTF (active buffer)
                hrtfRightSpeakerConvolverL_[active].process(effectsBufferRight_.data(), hrtfTempBufferL2_.data(), frames);
                hrtfRightSpeakerConvolverR_[active].process(effectsBufferRight_.data(), hrtfTempBufferR2_.data(), frames);

                // If crossfading, also process through pending buffer
                if (hrtfBinauralCrossfading_ && hrtfBinauralInitialized_[pending]) {
                    // Process through pending buffer
                    hrtfLeftSpeakerConvolverL_[pending].process(effectsBufferLeft_.data(), hrtfPendingTempBufferL_.data(), frames);
                    hrtfLeftSpeakerConvolverR_[pending].process(effectsBufferLeft_.data(), hrtfPendingTempBufferR_.data(), frames);
                    hrtfRightSpeakerConvolverL_[pending].process(effectsBufferRight_.data(), hrtfPendingTempBufferL2_.data(), frames);
                    hrtfRightSpeakerConvolverR_[pending].process(effectsBufferRight_.data(), hrtfPendingTempBufferR2_.data(), frames);

                    // Blend between active and pending outputs
                    const float crossfadeStep = 1.0f / static_cast<float>(frames);
                    for (int i = 0; i < frames; i++) {
                        // Smooth per-sample crossfade
                        float fadeProgress = hrtfBinauralCrossfadeProgress_ + crossfadeStep * static_cast<float>(i);
                        fadeProgress = std::min(1.0f, fadeProgress);

                        // Active buffer output
                        float activeL = hrtfTempBufferL_[i] + hrtfTempBufferL2_[i];
                        float activeR = hrtfTempBufferR_[i] + hrtfTempBufferR2_[i];

                        // Pending buffer output
                        float pendingL = hrtfPendingTempBufferL_[i] + hrtfPendingTempBufferL2_[i];
                        float pendingR = hrtfPendingTempBufferR_[i] + hrtfPendingTempBufferR2_[i];

                        // Crossfade blend
                        float binauralL = activeL * (1.0f - fadeProgress) + pendingL * fadeProgress;
                        float binauralR = activeR * (1.0f - fadeProgress) + pendingR * fadeProgress;

                        effectsBufferLeft_[i] = effectsBufferLeft_[i] * (1.0f - intensity) + binauralL * intensity;
                        effectsBufferRight_[i] = effectsBufferRight_[i] * (1.0f - intensity) + binauralR * intensity;
                    }

                    // Update crossfade progress for next block
                    hrtfBinauralCrossfadeProgress_ += 1.0f;  // One block = full transition

                    // Check if crossfade is complete - just swap indices!
                    if (hrtfBinauralCrossfadeProgress_ >= 1.0f) {
                        // Swap indices (no data copy, just pointer swap)
                        hrtfBinauralActiveIndex_ = pending;
                        hrtfBinauralPendingIndex_ = active;

                        hrtfBinauralCrossfading_ = false;
                        hrtfBinauralCrossfadeProgress_ = 0.0f;
                    }
                } else {
                    // No crossfade, just use active buffer
                    for (int i = 0; i < frames; i++) {
                        float binauralL = hrtfTempBufferL_[i] + hrtfTempBufferL2_[i];
                        float binauralR = hrtfTempBufferR_[i] + hrtfTempBufferR2_[i];

                        effectsBufferLeft_[i] = effectsBufferLeft_[i] * (1.0f - intensity) + binauralL * intensity;
                        effectsBufferRight_[i] = effectsBufferRight_[i] * (1.0f - intensity) + binauralR * intensity;
                    }
                }
            }
        }

        // DJ Filter - Lowpass/Highpass sweep filter
        // Position: -1.0 = full lowpass, 0.0 = bypass, 1.0 = full highpass
        if (djFilterEnabled_.load(std::memory_order_relaxed)) {
            const float position = djFilterPosition_.load(std::memory_order_relaxed);
            const float resonance = djFilterResonance_.load(std::memory_order_relaxed);

            // Reset filters if needed
            if (djFilterNeedsReset_) {
                djLowpassL_.reset();
                djLowpassR_.reset();
                djHighpassL_.reset();
                djHighpassR_.reset();
                djFilterNeedsReset_ = false;
            }

            // Deadzone around 0 for bypass
            const float deadzone = 0.05f;
            if (std::fabs(position) > deadzone) {
                if (position < 0.0f) {
                    // Lowpass mode: position -1.0 = 200Hz, position -0.05 = ~20000Hz
                    const float normalizedPos = (-position - deadzone) / (1.0f - deadzone);
                    const float minFreq = 200.0f;
                    const float maxFreq = 18000.0f;
                    // Exponential mapping for more natural feel
                    const float cutoff = maxFreq * std::pow(minFreq / maxFreq, normalizedPos);

                    // Only reconfigure if cutoff changed significantly
                    if (std::fabs(cutoff - djFilterLastCutoff_) > 10.0f || djFilterLastMode_ != -1) {
                        djLowpassL_.setup(sampleRate_, cutoff);
                        djLowpassR_.setup(sampleRate_, cutoff);
                        djFilterLastCutoff_ = cutoff;
                        djFilterLastMode_ = -1;
                    }

                    for (int i = 0; i < frames; i++) {
                        effectsBufferLeft_[i] = djLowpassL_.filter(effectsBufferLeft_[i]);
                        effectsBufferRight_[i] = djLowpassR_.filter(effectsBufferRight_[i]);
                    }
                } else {
                    // Highpass mode: position 0.05 = ~20Hz, position 1.0 = 8000Hz
                    const float normalizedPos = (position - deadzone) / (1.0f - deadzone);
                    const float minFreq = 20.0f;
                    const float maxFreq = 8000.0f;
                    // Exponential mapping
                    const float cutoff = minFreq * std::pow(maxFreq / minFreq, normalizedPos);

                    if (std::fabs(cutoff - djFilterLastCutoff_) > 10.0f || djFilterLastMode_ != 1) {
                        djHighpassL_.setup(sampleRate_, cutoff);
                        djHighpassR_.setup(sampleRate_, cutoff);
                        djFilterLastCutoff_ = cutoff;
                        djFilterLastMode_ = 1;
                    }

                    for (int i = 0; i < frames; i++) {
                        effectsBufferLeft_[i] = djHighpassL_.filter(effectsBufferLeft_[i]);
                        effectsBufferRight_[i] = djHighpassR_.filter(effectsBufferRight_[i]);
                    }
                }
            }
        }

        // Stereo Widener - Mid-Side processing with parameter smoothing
        // Width < 1.0 = narrower (more mono), Width = 1.0 = original, Width > 1.0 = wider
        if (stereoWidenerEnabled_.load(std::memory_order_relaxed)) {
            const float targetWidth = stereoWidenerWidth_.load(std::memory_order_relaxed);
            const float smoothingCoef = 0.001f;  // Smooth parameter changes to prevent clicks

            for (int i = 0; i < frames; i++) {
                // Smooth the width parameter to prevent crackling
                stereoWidenerWidthSmoothed_ += (targetWidth - stereoWidenerWidthSmoothed_) * smoothingCoef;

                const float left = effectsBufferLeft_[i];
                const float right = effectsBufferRight_[i];

                // Convert to Mid-Side
                const float mid = (left + right) * 0.5f;
                const float side = (left - right) * 0.5f;

                // Apply width (scale side signal)
                const float wideSide = side * stereoWidenerWidthSmoothed_;

                // Convert back to Left-Right with soft limiting to prevent clipping
                float outL = mid + wideSide;
                float outR = mid - wideSide;

                // Soft clip to prevent harsh distortion
                outL = std::tanh(outL);
                outR = std::tanh(outR);

                effectsBufferLeft_[i] = outL;
                effectsBufferRight_[i] = outR;
            }
        }
    }

    // Initialize HRTF filter in the inactive buffer (non-blocking for audio thread)
    void prepareHrtfFilter(int azimuth, int bufferIndex) {
        const unsigned int taps = mit_hrtf_availability(azimuth, HRTF_ELEVATION, HRTF_SAMPLE_RATE, HRTF_SUBJECT);
        if (taps == 0) {
            hrtfInitialized_[bufferIndex] = false;
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

        hrtfConvolverL_[bufferIndex].init(PROCESS_BLOCK_FRAMES, irL.data(), taps);
        hrtfConvolverR_[bufferIndex].init(PROCESS_BLOCK_FRAMES, irR.data(), taps);

        hrtfCurrentAzimuth_[bufferIndex] = azimuth;
        hrtfInitialized_[bufferIndex] = true;
    }

    // Check if we need to prepare a new HRTF filter and start crossfade
    void maybeInitHrtf(int azimuth) {
        // If first time initialization
        if (!hrtfInitialized_[hrtfActiveBuffer_]) {
            prepareHrtfFilter(azimuth, hrtfActiveBuffer_);
            return;
        }

        // If azimuth changed and we're not already crossfading
        if (hrtfCurrentAzimuth_[hrtfActiveBuffer_] != azimuth && !hrtfCrossfading_) {
            // Prepare new filter in the inactive buffer
            int inactiveBuffer = 1 - hrtfActiveBuffer_;
            prepareHrtfFilter(azimuth, inactiveBuffer);

            if (hrtfInitialized_[inactiveBuffer]) {
                // Start crossfade
                hrtfPendingBuffer_ = inactiveBuffer;
                hrtfCrossfading_ = true;
                hrtfCrossfadeProgress_ = 0.0f;
            }
        }
    }

    // =========================================================================
    // Binaural Stereo HRTF Initialization with Double-Buffer Crossfade
    // =========================================================================
    void maybeInitBinauralHrtf(int leftAzimuth, int rightAzimuth) {
        const int active = hrtfBinauralActiveIndex_;

        // First time initialization
        if (!hrtfBinauralInitialized_[active]) {
            initBinauralHrtfToBuffer(leftAzimuth, rightAzimuth, active);
            return;
        }

        // If azimuth changed and not already crossfading, prepare pending buffer
        bool needsUpdate = (hrtfBinauralLeftAzimuth_[active] != leftAzimuth) ||
                          (hrtfBinauralRightAzimuth_[active] != rightAzimuth);

        if (needsUpdate && !hrtfBinauralCrossfading_) {
            const int pending = hrtfBinauralPendingIndex_;

            // Initialize pending buffer with new azimuth
            if (initBinauralHrtfToBuffer(leftAzimuth, rightAzimuth, pending)) {
                hrtfBinauralCrossfading_ = true;
                hrtfBinauralCrossfadeProgress_ = 0.0f;
            }
        }
    }

    // Initialize HRTF convolvers to a specific buffer index
    bool initBinauralHrtfToBuffer(int leftAzimuth, int rightAzimuth, int bufferIndex) {
        bool success = true;

        // Left speaker
        const unsigned int tapsL = mit_hrtf_availability(leftAzimuth, HRTF_ELEVATION, HRTF_SAMPLE_RATE, HRTF_SUBJECT);
        if (tapsL > 0) {
            int actualAz = leftAzimuth, actualEl = HRTF_ELEVATION;
            std::vector<short> hL(tapsL), hR(tapsL);
            mit_hrtf_get(&actualAz, &actualEl, HRTF_SAMPLE_RATE, HRTF_SUBJECT, hL.data(), hR.data());
            std::vector<float> irL(tapsL), irR(tapsL);
            for (unsigned int i = 0; i < tapsL; i++) {
                irL[i] = hL[i] / 32768.0f;
                irR[i] = hR[i] / 32768.0f;
            }
            hrtfLeftSpeakerConvolverL_[bufferIndex].init(PROCESS_BLOCK_FRAMES, irL.data(), tapsL);
            hrtfLeftSpeakerConvolverR_[bufferIndex].init(PROCESS_BLOCK_FRAMES, irR.data(), tapsL);
        } else {
            success = false;
        }

        // Right speaker
        const unsigned int tapsR = mit_hrtf_availability(rightAzimuth, HRTF_ELEVATION, HRTF_SAMPLE_RATE, HRTF_SUBJECT);
        if (tapsR > 0) {
            int actualAz = rightAzimuth, actualEl = HRTF_ELEVATION;
            std::vector<short> hL(tapsR), hR(tapsR);
            mit_hrtf_get(&actualAz, &actualEl, HRTF_SAMPLE_RATE, HRTF_SUBJECT, hL.data(), hR.data());
            std::vector<float> irL(tapsR), irR(tapsR);
            for (unsigned int i = 0; i < tapsR; i++) {
                irL[i] = hL[i] / 32768.0f;
                irR[i] = hR[i] / 32768.0f;
            }
            hrtfRightSpeakerConvolverL_[bufferIndex].init(PROCESS_BLOCK_FRAMES, irL.data(), tapsR);
            hrtfRightSpeakerConvolverR_[bufferIndex].init(PROCESS_BLOCK_FRAMES, irR.data(), tapsR);
        } else {
            success = false;
        }

        if (success) {
            hrtfBinauralLeftAzimuth_[bufferIndex] = leftAzimuth;
            hrtfBinauralRightAzimuth_[bufferIndex] = rightAzimuth;
            hrtfBinauralInitialized_[bufferIndex] = true;
        }

        return success;
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

    std::atomic<bool> eqEnabled_;
    std::atomic<bool> compressorEnabled_;

    std::atomic<bool> pitchDetectionEnabled_;
    std::atomic<float> detectedPitch_;

    std::atomic<bool> hrtfEnabled_;
    std::atomic<float> hrtfIntensity_;
    std::atomic<int> hrtfAzimuth_;

    // Double-buffered HRTF convolvers for glitch-free filter switching
    fftconvolver::FFTConvolver hrtfConvolverL_[2];
    fftconvolver::FFTConvolver hrtfConvolverR_[2];
    int hrtfActiveBuffer_ = 0;          // Currently active convolver (0 or 1)
    int hrtfPendingBuffer_ = -1;        // Buffer being prepared (-1 = none)
    bool hrtfInitialized_[2] = {false, false};
    int hrtfCurrentAzimuth_[2] = {0, 0};

    // Crossfade state
    float hrtfCrossfadeProgress_ = 1.0f;  // 0.0 = old filter, 1.0 = new filter
    static constexpr float HRTF_CROSSFADE_RATE = 0.02f;  // ~50 blocks for full transition
    bool hrtfCrossfading_ = false;

    std::vector<float> hrtfTempBufferL_;
    std::vector<float> hrtfTempBufferR_;
    std::vector<float> hrtfTempBufferL2_;  // For crossfade
    std::vector<float> hrtfTempBufferR2_;  // For crossfade
    std::vector<float> hrtfMonoBuffer_;    // Mono mix for correct HRTF processing

    // Pending convolver temp buffers (for crossfade blending)
    std::vector<float> hrtfPendingTempBufferL_;
    std::vector<float> hrtfPendingTempBufferR_;
    std::vector<float> hrtfPendingTempBufferL2_;
    std::vector<float> hrtfPendingTempBufferR2_;

    // High-frequency compensation filters for HRTF (presence boost)
    BiquadFilter hrtfPresenceFilterL_;
    BiquadFilter hrtfPresenceFilterR_;
    BiquadFilter hrtfPresenceFilterL2_;  // For crossfade
    BiquadFilter hrtfPresenceFilterR2_;  // For crossfade

    // Binaural Stereo (Virtual Speaker) HRTF convolvers - double buffered
    // [0] and [1] are swapped via index for glitch-free transitions
    fftconvolver::FFTConvolver hrtfLeftSpeakerConvolverL_[2];   // Left speaker -> Left ear
    fftconvolver::FFTConvolver hrtfLeftSpeakerConvolverR_[2];   // Left speaker -> Right ear
    fftconvolver::FFTConvolver hrtfRightSpeakerConvolverL_[2];  // Right speaker -> Left ear
    fftconvolver::FFTConvolver hrtfRightSpeakerConvolverR_[2];  // Right speaker -> Right ear

    // Binaural double-buffer state
    int hrtfBinauralActiveIndex_ = 0;      // Currently active buffer (0 or 1)
    int hrtfBinauralPendingIndex_ = 1;     // Buffer being prepared
    bool hrtfBinauralInitialized_[2] = {false, false};
    int hrtfBinauralLeftAzimuth_[2] = {0, 0};
    int hrtfBinauralRightAzimuth_[2] = {0, 0};

    // Binaural crossfade state
    bool hrtfBinauralCrossfading_ = false;
    float hrtfBinauralCrossfadeProgress_ = 0.0f;

    std::atomic<bool> stereoWidenerEnabled_;
    std::atomic<float> stereoWidenerWidth_;  // 0.0-2.0, 1.0 = original
    float stereoWidenerWidthSmoothed_ = 1.0f;  // Smoothed width for click-free parameter changes

    // DJ Filter - Butterworth lowpass/highpass
    std::atomic<bool> djFilterEnabled_;
    std::atomic<float> djFilterPosition_;   // -1.0 = full lowpass, 0.0 = bypass, 1.0 = full highpass
    std::atomic<float> djFilterResonance_;  // Resonance/Q factor
    bool djFilterNeedsReset_ = false;
    float djFilterLastCutoff_ = 0.0f;
    int djFilterLastMode_ = 0;  // -1 = lowpass, 0 = bypass, 1 = highpass

    // iir1 Butterworth filters for DJ filter (4th order for steeper slope)
    Iir::Butterworth::LowPass<4> djLowpassL_;
    Iir::Butterworth::LowPass<4> djLowpassR_;
    Iir::Butterworth::HighPass<4> djHighpassL_;
    Iir::Butterworth::HighPass<4> djHighpassR_;

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

    std::atomic<float> compThresholdDb_;
    std::atomic<float> compRatio_;
    std::atomic<float> compAttackMs_;
    std::atomic<float> compReleaseMs_;
    std::atomic<float> compMakeupGainDb_;
    float compEnvelope_ = 0.0f;
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
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetStereoWidenerEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setStereoWidenerEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetStereoWidenerParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat width) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setStereoWidenerParams(width);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetDjFilterEnabled(
        JNIEnv*,
        jobject,
        jlong handle,
        jboolean enabled) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setDjFilterEnabled(enabled);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetDjFilterParams(
        JNIEnv*,
        jobject,
        jlong handle,
        jfloat position,
        jfloat resonance) {

    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setDjFilterParams(position, resonance);
}

}
