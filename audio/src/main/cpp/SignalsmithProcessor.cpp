#include <jni.h>
#include <android/log.h>
#include <cmath>
#include <vector>
#include <memory>

#include "signalsmith/signalsmith-stretch.h"

#define LOG_TAG "SignalsmithProc"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

class SignalsmithProcessor {
public:
    SignalsmithProcessor(int sampleRate, int channelCount)
        : sampleRate_(sampleRate)
        , channelCount_(channelCount)
        , pitchSemitones_(0.0f)
        , tempoRate_(1.0f) {
        
        stretch_.presetDefault(channelCount, sampleRate);
        
        inputLeft_.resize(8192);
        inputRight_.resize(8192);
        outputLeft_.resize(16384);
        outputRight_.resize(16384);
        
        LOGD("Created: sampleRate=%d, channels=%d, inputLatency=%d, outputLatency=%d",
             sampleRate, channelCount, stretch_.inputLatency(), stretch_.outputLatency());
    }

    int process(const short* input, int inputBytes, short* output, int maxOutputFrames) {
        int inputSamples = inputBytes / sizeof(short);
        int inputFrames = inputSamples / channelCount_;
        
        if (inputFrames <= 0) return 0;

        stretch_.setTransposeSemitones(pitchSemitones_);

        int outputFrames = static_cast<int>(inputFrames / tempoRate_) + 1;
        outputFrames = std::min(outputFrames, maxOutputFrames);

        ensureBufferSize(inputFrames, outputFrames);
        
        shortToFloatDeinterleaved(input, inputFrames);
        
        float* inputPtrs[2] = { inputLeft_.data(), inputRight_.data() };
        float* outputPtrs[2] = { outputLeft_.data(), outputRight_.data() };
        
        stretch_.process(inputPtrs, inputFrames, outputPtrs, outputFrames);
        
        floatToShortInterleaved(output, outputFrames);
        
        return outputFrames;
    }

    int flushAndGetRemaining(short* output, int maxOutputFrames) {
        stretch_.reset();
        return 0;
    }

    void setPitchSemitones(float semitones) {
        pitchSemitones_ = semitones;
        LOGD("setPitchSemitones: %f", semitones);
    }

    void setTempoRate(float rate) {
        tempoRate_ = rate;
        LOGD("setTempoRate: %f", rate);
    }

    void flush() {
        stretch_.reset();
        LOGD("flush");
    }

private:
    void ensureBufferSize(int inputFrames, int outputFrames) {
        if (inputLeft_.size() < static_cast<size_t>(inputFrames)) {
            inputLeft_.resize(inputFrames);
            inputRight_.resize(inputFrames);
        }
        if (outputLeft_.size() < static_cast<size_t>(outputFrames)) {
            outputLeft_.resize(outputFrames);
            outputRight_.resize(outputFrames);
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

    void floatToShortInterleaved(short* output, int frames) {
        if (channelCount_ == 2) {
            for (int i = 0; i < frames; i++) {
                float left = outputLeft_[i] * 32768.0f;
                float right = outputRight_[i] * 32768.0f;
                output[i * 2] = static_cast<short>(std::max(-32768.0f, std::min(32767.0f, left)));
                output[i * 2 + 1] = static_cast<short>(std::max(-32768.0f, std::min(32767.0f, right)));
            }
        } else {
            for (int i = 0; i < frames; i++) {
                float mono = outputLeft_[i] * 32768.0f;
                output[i] = static_cast<short>(std::max(-32768.0f, std::min(32767.0f, mono)));
            }
        }
    }

    signalsmith::stretch::SignalsmithStretch<float> stretch_;
    int sampleRate_;
    int channelCount_;
    float pitchSemitones_;
    float tempoRate_;
    
    std::vector<float> inputLeft_;
    std::vector<float> inputRight_;
    std::vector<float> outputLeft_;
    std::vector<float> outputRight_;
};

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeInit(
        JNIEnv* env,
        jobject thiz,
        jint sampleRate,
        jint channelCount) {
    
    auto* processor = new SignalsmithProcessor(sampleRate, channelCount);
    return reinterpret_cast<jlong>(processor);
}

JNIEXPORT jint JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeProcess(
        JNIEnv* env,
        jobject thiz,
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
        JNIEnv* env,
        jobject thiz,
        jlong handle,
        jfloat semitones) {
    
    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setPitchSemitones(semitones);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetTempoRate(
        JNIEnv* env,
        jobject thiz,
        jlong handle,
        jfloat rate) {
    
    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->setTempoRate(rate);
}

JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeFlush(
        JNIEnv* env,
        jobject thiz,
        jlong handle) {
    
    if (handle == 0) return;
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    processor->flush();
}

JNIEXPORT jint JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeFlushAndGetRemaining(
        JNIEnv* env,
        jobject thiz,
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
        JNIEnv* env,
        jobject thiz,
        jlong handle) {
    
    if (handle == 0) return;
    
    auto* processor = reinterpret_cast<SignalsmithProcessor*>(handle);
    delete processor;
    LOGD("Released processor");
}

}
