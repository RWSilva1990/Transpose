# Superpowered 실시간 오디오 DSP 엔진 통합

> YouTube 스트리밍 오디오에 실시간 Pitch/Tempo 이펙트를 적용하기 위한 Superpowered SDK 통합 프로젝트
>
> **브랜치**: `superpowered-sdk`
> **상태**: 완료 (라이선스 제약으로 Signalsmith로 대체됨)

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [시스템 아키텍처](#2-시스템-아키텍처)
3. [핵심 컴포넌트 구현](#3-핵심-컴포넌트-구현)
4. [데이터 플로우 상세](#4-데이터-플로우-상세)
5. [기술적 챌린지 및 해결](#5-기술적-챌린지-및-해결)
6. [파일 구조 및 역할](#6-파일-구조-및-역할)
7. [성과 및 결론](#7-성과-및-결론)

---

## 1. 프로젝트 개요

### 1.1 목표

| 요구사항 | 목표값 | 달성 |
|:---------|:-------|:-----|
| 실시간 Pitch Shifting | -24 ~ +24 반음 | O |
| 실시간 Time Stretching | 0.5x ~ 2.0x | O |
| 지연시간 | < 50ms | O (~20ms) |
| 스트리밍 호환 | 다운로드 없이 처리 | O |

### 1.2 왜 Superpowered인가?

| 대안 | 문제점 |
|:-----|:-------|
| ExoPlayer `PlaybackParameters` | SonicAudioProcessor 사용, 품질 낮음, Pitch/Speed 연동 |
| Android `AudioEffect` | Pitch Shifting 미지원 |
| FFmpeg | 실시간 처리 어려움, 라이브러리 크기 |
| **Superpowered** | WSOLA 알고리즘, OpenSL ES 내장, ARM NEON 최적화 |

### 1.3 한계 (Signalsmith로 전환한 이유)

- **상용 라이선스**: 앱 배포 시 로열티 발생
- 오픈소스 프로젝트 호환성 문제

---

## 2. 시스템 아키텍처

### 2.1 전체 구조

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              APPLICATION LAYER                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   MainViewModel                                                              │
│        │                                                                     │
│        ├── AudioEffectsManager (UI 상태: pitch 0~200, tempo 50~200)         │
│        │                                                                     │
│        └── PlaybackModeController (VIDEO ↔ AUDIO 모드 전환)                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               MEDIA LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ExoPlayer                                                                  │
│        │                                                                     │
│        └── HybridAudioSink (AudioSink 구현)                                  │
│                 │                                                            │
│                 ├── VIDEO 모드 → DefaultAudioSink                            │
│                 │                                                            │
│                 └── AUDIO 모드 → SuperpoweredBridge                          │
│                                         │                                    │
│                                         └── JNI 호출                         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              NATIVE LAYER (C++)                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   SuperpoweredAudioBridge.cpp                                                │
│        │                                                                     │
│        ├── PcmRingBuffer (Mutex 기반 Thread-safe 버퍼)                       │
│        │        │                                                            │
│        │        ├── write() ← ExoPlayer 스레드 (Producer)                    │
│        │        └── read()  → OpenSL ES 콜백 (Consumer)                      │
│        │                                                                     │
│        ├── TimeStretching (Superpowered DSP 엔진)                            │
│        │        │                                                            │
│        │        ├── pitchShiftCents (-2400 ~ +2400)                          │
│        │        └── rate (0.5 ~ 2.0)                                         │
│        │                                                                     │
│        └── AndroidAudioIO (OpenSL ES 오디오 출력)                            │
│                 │                                                            │
│                 └── audioProcessing() 콜백 → 하드웨어 출력                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Dual-Mode 설계

| 모드 | 오디오 경로 | 사용 사례 |
|:-----|:-----------|:----------|
| **VIDEO** | ExoPlayer → DefaultAudioSink → AudioTrack | 일반 비디오 시청 |
| **AUDIO** | ExoPlayer → HybridAudioSink → Superpowered → OpenSL ES | 피치/템포 조절 |

모드 전환 이유:
- VIDEO 모드: 비디오-오디오 동기화가 중요, DSP 불필요
- AUDIO 모드: DSP 필요, 비디오 없음, 독립적 오디오 출력

---

## 3. 핵심 컴포넌트 구현

### 3.1 HybridAudioSink

**역할**: ExoPlayer의 AudioSink 인터페이스를 구현하여 PCM 데이터 라우팅

**핵심 메서드**:

```kotlin
// handleBuffer: 디코딩된 PCM을 받아 모드에 따라 분기
override fun handleBuffer(buffer: ByteBuffer, presentationTimeUs: Long, ...): Boolean {
    return when (mode) {
        PlaybackMode.VIDEO -> defaultAudioSink.handleBuffer(buffer, ...)
        PlaybackMode.AUDIO -> {
            // Backpressure 처리: 버퍼 가득 차면 대기
            while (!superpoweredBridge.pushPcm(buffer, size, presentationTimeUs)) {
                Thread.sleep(2)
            }
            true
        }
    }
}

// getCurrentPositionUs: 재생 위치 반환
override fun getCurrentPositionUs(sourceEnded: Boolean): Long {
    return when (mode) {
        PlaybackMode.VIDEO -> defaultAudioSink.getCurrentPositionUs(sourceEnded)
        PlaybackMode.AUDIO -> superpoweredBridge.getCurrentPositionUs()
    }
}

// hasPendingData: 버퍼에 데이터 남아있는지
override fun hasPendingData(): Boolean {
    return when (mode) {
        PlaybackMode.VIDEO -> defaultAudioSink.hasPendingData()
        PlaybackMode.AUDIO -> true  // 항상 true (내부 버퍼링 있음)
    }
}
```

### 3.2 SuperpoweredBridge

**역할**: Kotlin ↔ C++ JNI 브릿지, Position 계산

**핵심 구현**:

```kotlin
class SuperpoweredBridgeImpl : SuperpoweredBridge {
    
    // Stream Offset 보정 (YouTube PTS는 0부터 시작하지 않음)
    private var streamOffsetUs: Long = -1
    private var firstPtsUs: Long = 0
    private var firstPtsSystemTimeNs: Long = 0
    private var isSeeking: Boolean = false
    
    override fun pushPcm(buffer: ByteBuffer, sizeInBytes: Int, presentationTimeUs: Long): Boolean {
        // 첫 청크에서 offset 감지
        if (streamOffsetUs < 0) {
            streamOffsetUs = presentationTimeUs
        }
        
        // Seek 후 첫 청크에서 Position 기준점 설정
        if (isFirstChunkAfterFlush) {
            firstPtsUs = presentationTimeUs - streamOffsetUs
            firstPtsSystemTimeNs = System.nanoTime()
            isSeeking = false
        }
        
        return engine.nativeWritePcm(buffer, sizeInBytes)
    }
    
    // Wall Clock 기반 Position 계산
    override fun getCurrentPositionUs(): Long {
        if (isSeeking) return Long.MIN_VALUE  // POSITION_NOT_SET
        
        val elapsedNs = System.nanoTime() - firstPtsSystemTimeNs
        return firstPtsUs + (elapsedNs / 1000)
    }
    
    override fun flush() {
        isSeeking = true
        streamOffsetUs = -1  // 다음 청크에서 새로 감지
        engine.nativeFlush()
    }
}
```

### 3.3 PcmRingBuffer (C++)

**역할**: Producer-Consumer 패턴의 Thread-safe 원형 버퍼

```cpp
class PcmRingBuffer {
public:
    PcmRingBuffer(size_t capacity) : capacity_(capacity), size_(0), head_(0), tail_(0) {
        buffer_.resize(capacity);
    }
    
    // Producer (ExoPlayer 스레드)
    size_t write(const float* data, size_t samples) {
        std::lock_guard<std::mutex> lock(mutex_);
        
        size_t available = capacity_ - size_;
        size_t toWrite = std::min(samples, available);
        
        for (size_t i = 0; i < toWrite; i++) {
            buffer_[(head_ + i) % capacity_] = data[i];
        }
        head_ = (head_ + toWrite) % capacity_;
        size_ += toWrite;
        
        return toWrite;
    }
    
    // Consumer (OpenSL ES 콜백 스레드)
    size_t read(float* data, size_t samples) {
        std::lock_guard<std::mutex> lock(mutex_);
        
        size_t toRead = std::min(samples, size_);
        
        for (size_t i = 0; i < toRead; i++) {
            data[i] = buffer_[(tail_ + i) % capacity_];
        }
        tail_ = (tail_ + toRead) % capacity_;
        size_ -= toRead;
        
        // Zero-pad if underrun
        for (size_t i = toRead; i < samples; i++) {
            data[i] = 0.0f;
        }
        
        return toRead;
    }
    
private:
    std::vector<float> buffer_;
    size_t capacity_, size_, head_, tail_;
    std::mutex mutex_;
};
```

### 3.4 Audio Callback (C++)

**역할**: OpenSL ES 하드웨어 콜백에서 DSP 처리 및 출력

```cpp
static bool audioProcessing(void* clientdata, short int* audioIO,
                            int numFrames, int samplerate) {
    
    // 1. Ring Buffer에서 입력 데이터 읽기
    size_t available = inputBuffer->available();
    int framesToFeed = std::min((int)(available / 2), MAX_FRAMES_TO_FEED);
    
    if (framesToFeed > 0) {
        inputBuffer->read(tempInputBuffer, framesToFeed * 2);
        
        // 2. TimeStretching 엔진에 입력
        timeStretching->addInput(tempInputBuffer, framesToFeed);
    }
    
    // 3. 처리된 출력 가져오기
    int outputFramesAvailable = timeStretching->getOutputLengthFrames();
    
    if (playing && outputFramesAvailable >= numFrames) {
        // 4. Float → Short 변환하여 하드웨어 출력
        timeStretching->getOutput(tempOutputBuffer, numFrames);
        Superpowered::FloatToShortInt(tempOutputBuffer, audioIO, numFrames * 2);
        return true;  // 소리 출력
    }
    
    return false;  // 무음
}
```

---

## 4. 데이터 플로우 상세

### 4.1 PCM 데이터 흐름

```
┌────────────────────────────────────────────────────────────────────────────┐
│ 1. YouTube → ExoPlayer                                                      │
│    • DASH/HLS 스트리밍으로 청크 다운로드                                     │
│    • 코덱(AAC/Opus) 디코딩 → PCM (16-bit signed, stereo)                    │
├────────────────────────────────────────────────────────────────────────────┤
│ 2. ExoPlayer → HybridAudioSink.handleBuffer()                               │
│    • ByteBuffer + presentationTimeUs 전달                                   │
│    • AUDIO 모드면 SuperpoweredBridge로 라우팅                               │
├────────────────────────────────────────────────────────────────────────────┤
│ 3. SuperpoweredBridge → JNI → nativeWritePcm()                              │
│    • Stream offset 보정                                                     │
│    • Short → Float 변환                                                     │
│    • Ring Buffer에 저장                                                     │
├────────────────────────────────────────────────────────────────────────────┤
│ 4. OpenSL ES 콜백 → audioProcessing()                                       │
│    • ~5.8ms마다 호출 (256 frames @ 44100Hz)                                 │
│    • Ring Buffer에서 읽기                                                   │
│    • TimeStretching.addInput() → getOutput()                                │
│    • Float → Short 변환 → 하드웨어 출력                                     │
└────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Pitch/Tempo 제어 흐름

```
UI Slider (0~200)
     │
     ▼
AudioEffectsManager.setPitchValue(value)
     │
     │  // 0~200 → -10~+10 반음 변환
     │  semitones = (value - 100) / 10.0
     ▼
AudioEffectHandlerImpl.setPitch(semitones)
     │
     │  // 모드 확인
     │  if (AUDIO 모드) → PlaybackModeController
     │  if (VIDEO 모드) → ExoPlayer.playbackParameters
     ▼
PlaybackModeController.setPitch(ratio)
     │
     │  // 반음 → ratio → cents 변환
     │  ratio = 2^(semitones/12)
     │  cents = semitones * 100
     ▼
SuperpoweredBridge.setPitchSemitones(semitones)
     │
     ▼
JNI: nativeSetPitch(cents)
     │
     ▼
timeStretching->pitchShiftCents = cents
```

---

## 5. 기술적 챌린지 및 해결

### 5.1 Buffer Backpressure

**문제**: ExoPlayer 디코딩이 실시간보다 빠름 → Ring Buffer 오버플로우 → 재생 중단

**해결**: `handleBuffer()`에서 대기 루프

```kotlin
while (!superpoweredBridge.pushPcm(...)) {
    Thread.sleep(2)  // 버퍼에 공간 생길 때까지 대기
}
return true  // 항상 성공 반환하여 ExoPlayer 상태 안정화
```

### 5.2 Position 음수 표시

**문제**: YouTube PTS가 ~999,840초부터 시작 → UI에 -278시간 표시

**해결**: Stream Offset 보정

```kotlin
if (streamOffsetUs < 0) {
    streamOffsetUs = presentationTimeUs  // 첫 청크의 PTS를 offset으로
}
normalizedPts = presentationTimeUs - streamOffsetUs
```

### 5.3 Seek 후 Position 오류

**문제**: Seek 시 `flush()` 호출 → 이전 offset 유지 → 잘못된 Position

**해결**: `flush()`에서 offset 리셋 + `isSeeking` 플래그

```kotlin
override fun flush() {
    isSeeking = true
    streamOffsetUs = -1  // 다음 청크에서 새로 감지
}

override fun getCurrentPositionUs(): Long {
    if (isSeeking) return Long.MIN_VALUE  // ExoPlayer가 자체 관리
}
```

### 5.4 Native 버퍼 오버플로우 크래시

**문제**: 할당된 버퍼보다 많은 데이터 쓰기 → 힙 손상 → 무관한 스레드에서 크래시

**해결**: 상수로 버퍼 크기 통일

```cpp
static const int MAX_FRAMES_TO_FEED = 4096;
static const size_t TEMP_BUFFER_SIZE = MAX_FRAMES_TO_FEED * 2;  // stereo
float* tempInputBuffer = new float[TEMP_BUFFER_SIZE];
```

---

## 6. 파일 구조 및 역할

### 6.1 Kotlin/Java

```
media/src/main/java/com/example/media/
├── audio/
│   ├── PlaybackMode.kt              # enum: VIDEO, AUDIO
│   ├── PlaybackModeController.kt    # 모드 전환, pitch/tempo 전달
│   ├── HybridAudioSink.kt           # AudioSink 구현, PCM 라우팅
│   ├── SuperpoweredBridge.kt        # 인터페이스 정의
│   └── SuperpoweredBridgeImpl.kt    # JNI 브릿지, Position 계산
├── audio_effect/
│   └── AudioEffectHandlerImpl.kt    # 이펙트 핸들러, 모드별 분기
└── di/
    └── MediaModule.kt               # Hilt DI 설정

audio/src/main/java/com/example/audio/
└── SuperpoweredAudioEngine.kt       # JNI 네이티브 메서드 선언
```

### 6.2 C++ Native

```
audio/src/main/cpp/
├── SuperpoweredAudioBridge.cpp      # 핵심 구현
│   ├── PcmRingBuffer               # Thread-safe 원형 버퍼
│   ├── nativeInit()                # 엔진 초기화
│   ├── nativeWritePcm()            # PCM 버퍼에 쓰기
│   ├── audioProcessing()           # OpenSL ES 콜백
│   ├── nativeSetPitch()            # 피치 설정
│   └── nativeSetRate()             # 템포 설정
│
└── superpowered/                    # Superpowered SDK 헤더
    ├── SuperpoweredTimeStretching.h
    ├── SuperpoweredSimple.h         # Float ↔ Short 변환
    └── OpenSource/
        └── SuperpoweredAndroidAudioIO.h
```

### 6.3 각 파일의 역할 요약

| 파일 | 역할 | 핵심 기능 |
|:-----|:-----|:----------|
| `HybridAudioSink.kt` | PCM 데이터 분기점 | `handleBuffer()`, `getCurrentPositionUs()` |
| `SuperpoweredBridgeImpl.kt` | JNI 브릿지 | offset 보정, Position 계산 |
| `SuperpoweredAudioBridge.cpp` | Native DSP | Ring Buffer, TimeStretching, OpenSL ES |
| `PlaybackModeController.kt` | 모드 관리 | VIDEO ↔ AUDIO 전환 |
| `AudioEffectHandlerImpl.kt` | 이펙트 라우팅 | 모드별 pitch/tempo 전달 경로 |

---

## 7. 성과 및 결론

### 7.1 달성 목표

| 목표 | 결과 |
|:-----|:-----|
| 실시간 Pitch Shifting | -24 ~ +24 반음, 고품질 WSOLA |
| 실시간 Time Stretching | 0.5x ~ 2.0x, 음질 유지 |
| 저지연 | ~20ms |
| 스트리밍 호환 | 다운로드 없이 실시간 처리 |
| 안정성 | 메모리 안전, 크래시 제거 |

### 7.2 핵심 기술 성과

1. **AudioSink 확장 패턴**: ExoPlayer 파이프라인을 비침습적으로 확장
2. **Producer-Consumer 패턴**: 비동기 데이터 교환으로 디코딩/재생 속도 차이 흡수
3. **JNI 브릿지 설계**: Kotlin ↔ C++ 간 효율적 데이터/명령 전달
4. **Position 동기화**: Stream offset 보정으로 정확한 재생 위치 표시

### 7.3 한계 및 후속 작업

- **라이선스 제약**: Superpowered 상용 라이선스로 인해 Signalsmith로 전환
- **일시정지 후 Position 점프**: Wall Clock 기반 계산의 한계 (미해결)
- **VIDEO_WITH_DSP**: Superpowered 브랜치에서는 미구현

---

*이 문서는 `superpowered-sdk` 브랜치의 기술 구현을 정리한 포트폴리오 자료입니다.*
