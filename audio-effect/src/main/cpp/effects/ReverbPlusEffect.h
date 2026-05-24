#pragma once

#include <array>
#include <atomic>
#include <vector>

class ReverbPlusEffect {
public:
    void initialize(int sampleRate, int channelCount);
    void reset();

    void setEnabled(bool enabled);
    void setParams(float dry, float wet, float roomSize, float damping);

    bool isEnabled() const;
    void process(float* left, float* right, int frames);

private:
    struct CombFilter {
        std::vector<float> buffer;
        int index = 0;
        float filterStore = 0.0f;

        void resize(int size);
        void reset();
        float process(float input, float feedback, float damp1, float damp2);
    };

    struct AllpassFilter {
        std::vector<float> buffer;
        int index = 0;
        float feedback = 0.5f;

        void resize(int size);
        void reset();
        float process(float input);
    };

    using CombBank = std::array<CombFilter, 8>;
    using AllpassBank = std::array<AllpassFilter, 4>;

    void configureBuffers();
    static int scaledDelay(int baseDelay, float sampleRateScale);

    int sampleRate_ = 44100;
    int channelCount_ = 2;

    CombBank combL_;
    CombBank combR_;
    AllpassBank allpassL_;
    AllpassBank allpassR_;

    std::atomic<bool> enabled_{false};
    std::atomic<float> dry_{1.0f};
    std::atomic<float> wet_{0.3f};
    std::atomic<float> roomSize_{0.5f};
    std::atomic<float> damping_{0.5f};
};
