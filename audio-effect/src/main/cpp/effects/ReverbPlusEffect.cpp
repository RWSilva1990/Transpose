#include "effects/ReverbPlusEffect.h"

#include <algorithm>
#include <cmath>

namespace {
constexpr float FIXED_GAIN = 0.015f;
constexpr float SCALE_WET = 3.0f;
constexpr float SCALE_ROOM = 0.28f;
constexpr float OFFSET_ROOM = 0.7f;
constexpr float SCALE_DAMPING = 0.4f;

constexpr std::array<int, 8> COMB_TUNING_L = {
    1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617
};
constexpr std::array<int, 4> ALLPASS_TUNING_L = {
    556, 441, 341, 225
};
constexpr int STEREO_SPREAD = 23;

inline float flushDenormal(float value) {
    return std::fabs(value) < 1.0e-20f ? 0.0f : value;
}
}

void ReverbPlusEffect::CombFilter::resize(int size) {
    buffer.assign(std::max(1, size), 0.0f);
    index = 0;
    filterStore = 0.0f;
}

void ReverbPlusEffect::CombFilter::reset() {
    std::fill(buffer.begin(), buffer.end(), 0.0f);
    index = 0;
    filterStore = 0.0f;
}

float ReverbPlusEffect::CombFilter::process(float input, float feedback, float damp1, float damp2) {
    if (buffer.empty()) return 0.0f;

    const float output = buffer[static_cast<size_t>(index)];
    filterStore = flushDenormal(output * damp2 + filterStore * damp1);
    buffer[static_cast<size_t>(index)] = input + filterStore * feedback;

    index += 1;
    if (index >= static_cast<int>(buffer.size())) index = 0;

    return output;
}

void ReverbPlusEffect::AllpassFilter::resize(int size) {
    buffer.assign(std::max(1, size), 0.0f);
    index = 0;
}

void ReverbPlusEffect::AllpassFilter::reset() {
    std::fill(buffer.begin(), buffer.end(), 0.0f);
    index = 0;
}

float ReverbPlusEffect::AllpassFilter::process(float input) {
    if (buffer.empty()) return input;

    const float bufOut = buffer[static_cast<size_t>(index)];
    const float output = -input + bufOut;
    buffer[static_cast<size_t>(index)] = input + bufOut * feedback;

    index += 1;
    if (index >= static_cast<int>(buffer.size())) index = 0;

    return output;
}

void ReverbPlusEffect::initialize(int sampleRate, int channelCount) {
    sampleRate_ = std::max(1, sampleRate);
    channelCount_ = std::max(1, channelCount);
    configureBuffers();
}

void ReverbPlusEffect::reset() {
    for (auto& comb : combL_) comb.reset();
    for (auto& comb : combR_) comb.reset();
    for (auto& allpass : allpassL_) allpass.reset();
    for (auto& allpass : allpassR_) allpass.reset();
}

void ReverbPlusEffect::setEnabled(bool enabled) {
    enabled_.store(enabled, std::memory_order_relaxed);
}

void ReverbPlusEffect::setParams(float dry, float wet, float roomSize, float damping) {
    dry_.store(std::clamp(dry, 0.0f, 1.0f), std::memory_order_relaxed);
    wet_.store(std::clamp(wet, 0.0f, 1.0f), std::memory_order_relaxed);
    roomSize_.store(std::clamp(roomSize, 0.0f, 1.0f), std::memory_order_relaxed);
    damping_.store(std::clamp(damping, 0.0f, 1.0f), std::memory_order_relaxed);
}

bool ReverbPlusEffect::isEnabled() const {
    return enabled_.load(std::memory_order_relaxed);
}

void ReverbPlusEffect::process(float* left, float* right, int frames) {
    if (left == nullptr || right == nullptr || frames <= 0) return;

    const float dry = dry_.load(std::memory_order_relaxed);
    const float wet = wet_.load(std::memory_order_relaxed) * SCALE_WET;
    const float room = roomSize_.load(std::memory_order_relaxed) * SCALE_ROOM + OFFSET_ROOM;
    const float damp1 = damping_.load(std::memory_order_relaxed) * SCALE_DAMPING;
    const float damp2 = 1.0f - damp1;

    for (int i = 0; i < frames; ++i) {
        const float inputL = left[i];
        const float inputR = channelCount_ == 1 ? inputL : right[i];
        const float input = (inputL + inputR) * FIXED_GAIN;

        float outL = 0.0f;
        float outR = 0.0f;
        for (int c = 0; c < 8; ++c) {
            outL += combL_[static_cast<size_t>(c)].process(input, room, damp1, damp2);
            outR += combR_[static_cast<size_t>(c)].process(input, room, damp1, damp2);
        }

        for (int a = 0; a < 4; ++a) {
            outL = allpassL_[static_cast<size_t>(a)].process(outL);
            outR = allpassR_[static_cast<size_t>(a)].process(outR);
        }

        left[i] = inputL * dry + outL * wet;
        right[i] = inputR * dry + outR * wet;
    }
}

void ReverbPlusEffect::configureBuffers() {
    const float scale = static_cast<float>(sampleRate_) / 44100.0f;
    for (int i = 0; i < 8; ++i) {
        const int base = COMB_TUNING_L[static_cast<size_t>(i)];
        combL_[static_cast<size_t>(i)].resize(scaledDelay(base, scale));
        combR_[static_cast<size_t>(i)].resize(scaledDelay(base + STEREO_SPREAD, scale));
    }

    for (int i = 0; i < 4; ++i) {
        const int base = ALLPASS_TUNING_L[static_cast<size_t>(i)];
        allpassL_[static_cast<size_t>(i)].resize(scaledDelay(base, scale));
        allpassR_[static_cast<size_t>(i)].resize(scaledDelay(base + STEREO_SPREAD, scale));
    }
}

int ReverbPlusEffect::scaledDelay(int baseDelay, float sampleRateScale) {
    return std::max(1, static_cast<int>(std::lround(static_cast<float>(baseDelay) * sampleRateScale)));
}
