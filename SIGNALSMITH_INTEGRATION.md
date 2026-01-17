# Signalsmith Stretch 실시간 오디오 DSP 엔진 통합

> 오픈소스 MIT 라이선스 DSP 라이브러리를 활용한 실시간 Pitch/Tempo 이펙트 구현
>
> **브랜치**: `signalsmith-stretch` (현재)
> **상태**: 활성 개발 중

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [시스템 아키텍처](#2-시스템-아키텍처)
3. [핵심 컴포넌트 구현](#3-핵심-컴포넌트-구현)
4. [데이터 플로우 상세](#4-데이터-플로우-상세)
5. [기술적 챌린지 및 해결](#5-기술적-챌린지-및-해결)
6. [파일 구조 및 역할](#6-파일-구조-및-역할)
7. [Superpowered와의 차이점](#7-superpowered와의-차이점)
8. [성과 및 결론](#8-성과-및-결론)

---

## 1. 프로젝트 개요

### 1.1 Superpowered에서 전환한 이유

| 항목 | Superpowered | Signalsmith Stretch |
|:-----|:-------------|:--------------------|
| **라이선스** | 상용 (로열티 필요) | MIT (완전 무료) |
| **오디오 출력** | OpenSL ES 내장 | 별도 구현 필요 |
| **알고리즘** | WSOLA | STFT + Phase Vocoder |
| **배포 형태** | 바이너리 라이브러리 | Header-only |
| **커스터마이징** | 불가 | 소스 수정 가능 |

### 1.2 목표

| 요구사항 | 목표값 | 달성 |
|:---------|:-------|:-----|
| 실시간 Pitch Shifting | -24 ~ +24 반음 | O |
| 실시간 Time Stretching | 0.5x ~ 2.0x | O |
| VIDEO_WITH_DSP 모드 | 비디오 + DSP 동시 | O |
| MIT 라이선스 유지 | 오픈소스 호환 | O |

### 1.3 Signalsmith Stretch 알고리즘

STFT(Short-Time Fourier Transform) 기반 Phase Vocoder:

```
입력 PCM → [Windowing] → [FFT] → [Phase 보정] → [IFFT] → [Overlap-Add] → 출력 PCM
              Hann        주파수     Pitch/Tempo    시간      프레임
             Window      도메인      변환          도메인     합성
```

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
│        ├── AudioEffectsManager (UI 상태 관리)                                │
│        │                                                                     │
│        └── PlaybackModeController                                            │
│                 │                                                            │
│                 ├── switchToVideoMode()                                      │
│                 ├── switchToAudioMode()                                      │
│                 └── switchToVideoWithDspMode()  ← 새로 추가                  │
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
│                 ├── VIDEO 모드        → DefaultAudioSink                     │
│                 ├── AUDIO 모드        → SignalsmithBridge                    │
│                 └── VIDEO_WITH_DSP 모드 → SignalsmithBridge                  │
│                                               │                              │
│                                               └── JNI 호출                   │
│                                                                              │
│   SignalsmithBridgeImpl                                                      │
│        ├── Position 계산 (Wall Clock 기반)                                   │
│        ├── Seek 상태 관리 (isSeeking 플래그)                                 │
│        └── Stream Offset 보정                                                │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              NATIVE LAYER (C++)                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   SignalsmithAudioBridge.cpp                                                 │
│        │                                                                     │
│        ├── SpscRingBuffer (Lock-free SPSC 버퍼)                              │
│        │        │                                                            │
│        │        ├── write() ← JNI 스레드 (Producer)                          │
│        │        └── read()  → Kotlin 렌더 스레드 (Consumer)                  │
│        │                                                                     │
│        └── SignalsmithStretch<float>                                         │
│                 ├── presetDefault(channels, sampleRate)                      │
│                 ├── setTransposeSemitones(pitch)                             │
│                 └── process(input, inputFrames, output, outputFrames)        │
│                                                                              │
│   SignalsmithRenderThread.kt (Kotlin)                                        │
│        │                                                                     │
│        └── AudioTrack (별도 관리)                                            │
│                 └── 처리된 PCM 출력                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 3가지 재생 모드

| 모드 | 비디오 | DSP | 오디오 출력 경로 | 사용 사례 |
|:-----|:-------|:----|:----------------|:----------|
| **VIDEO** | O | X | DefaultAudioSink → AudioTrack | 일반 비디오 시청 |
| **AUDIO** | X | O | SignalsmithBridge → AudioTrack | 피치/템포 조절 (음악 연습) |
| **VIDEO_WITH_DSP** | O | O | SignalsmithBridge → AudioTrack | 비디오 보면서 피치 조절 |

---

## 3. 핵심 컴포넌트 구현

### 3.1 HybridAudioSink

**역할**: ExoPlayer의 PCM 데이터를 모드에 따라 분기

**Superpowered와의 차이점**:
- VIDEO_WITH_DSP 모드 추가
- `superpoweredBridge` 변수명은 유지 (실제로는 SignalsmithBridge)

```kotlin
class HybridAudioSink(
    context: Context,
    private val superpoweredBridge: SuperpoweredBridge  // 실제로는 SignalsmithBridge
) : AudioSink {

    var mode: PlaybackMode = PlaybackMode.VIDEO_WITH_DSP
    
    // DSP 사용 여부 판단
    private fun usesDsp(): Boolean = 
        mode == PlaybackMode.AUDIO || mode == PlaybackMode.VIDEO_WITH_DSP

    override fun handleBuffer(buffer: ByteBuffer, presentationTimeUs: Long, ...): Boolean {
        return when (mode) {
            PlaybackMode.VIDEO -> defaultAudioSink.handleBuffer(buffer, ...)
            
            PlaybackMode.AUDIO, PlaybackMode.VIDEO_WITH_DSP -> {
                // Backpressure: 버퍼 가득 차면 대기
                while (!superpoweredBridge.pushPcm(buffer, size, presentationTimeUs)) {
                    Thread.sleep(2)
                }
                true
            }
        }
    }

    override fun play() {
        if (usesDsp()) superpoweredBridge.play()
        if (mode != PlaybackMode.AUDIO) defaultAudioSink.play()
    }

    override fun pause() {
        when (mode) {
            PlaybackMode.VIDEO -> defaultAudioSink.pause()
            PlaybackMode.AUDIO, PlaybackMode.VIDEO_WITH_DSP -> superpoweredBridge.pause()
        }
    }
}
```

### 3.2 SignalsmithBridgeImpl

**역할**: JNI 브릿지 + Position 계산 + Seek 상태 관리

```kotlin
class SignalsmithBridgeImpl @Inject constructor(
    private val engine: SignalsmithAudioEngine,
    private val renderThread: SignalsmithRenderThread
) : SuperpoweredBridge {

    // Position 계산용 상태
    private var firstPtsUs: Long = 0
    private var firstPtsSystemTimeNs: Long = 0
    private var isSeeking: Boolean = false
    private var isFirstChunkAfterFlush: Boolean = true

    override fun pushPcm(buffer: ByteBuffer, sizeInBytes: Int, presentationTimeUs: Long): Boolean {
        // 버퍼 공간 확인
        val space = engine.getBufferAvailableSpace()
        if (space < sizeInBytes / 2) return false

        // Seek 후 첫 청크에서 Position 기준점 설정
        if (isFirstChunkAfterFlush) {
            firstPtsUs = presentationTimeUs
            firstPtsSystemTimeNs = System.nanoTime()
            isSeeking = false
            isFirstChunkAfterFlush = false
            engine.setSeekPosition(presentationTimeUs)
        }

        engine.writePcm(buffer, sizeInBytes, presentationTimeUs)
        return true
    }

    override fun getCurrentPositionUs(): Long {
        // Seek 중이면 POSITION_NOT_SET 반환
        if (isSeeking) return Long.MIN_VALUE

        // Wall Clock 기반 Position 계산
        val elapsedNs = System.nanoTime() - firstPtsSystemTimeNs
        val elapsedUs = elapsedNs / 1000
        return firstPtsUs + elapsedUs
    }

    override fun flush() {
        isSeeking = true
        isFirstChunkAfterFlush = true
        engine.flush()
    }

    override fun setPitch(ratio: Float) {
        engine.setPitch(ratio)
    }

    override fun setRate(rate: Float) {
        engine.setTempo(rate)
    }
}
```

### 3.3 SpscRingBuffer (C++)

**역할**: Lock-free Single Producer Single Consumer 버퍼

**Superpowered(Mutex)와의 차이**: `std::atomic`만 사용하여 오버헤드 감소

```cpp
class SpscRingBuffer {
public:
    SpscRingBuffer(size_t capacity) : capacity_(capacity), head_(0), tail_(0) {
        buffer_ = new float[capacity];
    }

    // Producer (JNI 스레드)
    size_t write(const float* data, size_t samples) {
        size_t head = head_.load(std::memory_order_relaxed);
        size_t tail = tail_.load(std::memory_order_acquire);
        
        size_t available = capacity_ - (head - tail);
        size_t toWrite = std::min(samples, available);
        
        for (size_t i = 0; i < toWrite; i++) {
            buffer_[(head + i) % capacity_] = data[i];
        }
        
        head_.store(head + toWrite, std::memory_order_release);
        return toWrite;
    }

    // Consumer (렌더 스레드)
    size_t read(float* data, size_t samples) {
        size_t head = head_.load(std::memory_order_acquire);
        size_t tail = tail_.load(std::memory_order_relaxed);
        
        size_t available = head - tail;
        size_t toRead = std::min(samples, available);
        
        for (size_t i = 0; i < toRead; i++) {
            data[i] = buffer_[(tail + i) % capacity_];
        }
        
        // Underrun 시 Zero-pad
        for (size_t i = toRead; i < samples; i++) {
            data[i] = 0.0f;
        }
        
        tail_.store(tail + toRead, std::memory_order_release);
        return toRead;
    }

private:
    float* buffer_;
    size_t capacity_;
    std::atomic<size_t> head_;  // Producer가 증가
    std::atomic<size_t> tail_;  // Consumer가 증가
};
```

### 3.4 processAudio (C++)

**역할**: Ring Buffer에서 읽고 Signalsmith로 처리

```cpp
static int processAudio(float* outputInterleaved, int outputFrames) {
    if (!isInitialized.load() || stretch == nullptr) {
        memset(outputInterleaved, 0, outputFrames * 2 * sizeof(float));
        return 0;
    }

    // 1. Pitch 설정
    float pitchSemitones = currentPitchSemitones.load();
    stretch->setTransposeSemitones(pitchSemitones);

    // 2. Tempo에 따른 입력 프레임 수 계산
    float tempo = currentTempo.load();
    double inputFramesNeeded = outputFrames * tempo + tempoAccumulator;
    int inputFramesToRead = (int)inputFramesNeeded;
    tempoAccumulator = inputFramesNeeded - inputFramesToRead;  // 소수점 누적

    // 3. Ring Buffer에서 읽기
    std::vector<float> interleavedInput(inputFramesToRead * 2);
    size_t samplesRead = inputBuffer->read(interleavedInput.data(), inputFramesToRead * 2);

    // 4. Deinterleave (LRLRLR → L[], R[])
    deinterleave(interleavedInput.data(), inputLeft.data(), inputRight.data(), inputFramesToRead);

    // 5. Signalsmith 처리
    stretch->process(inputPtrs, inputFramesToRead, outputPtrs, outputFrames);

    // 6. Interleave (L[], R[] → LRLRLR)
    if (isPlaying.load()) {
        interleave(outputLeft.data(), outputRight.data(), outputInterleaved, outputFrames);
    } else {
        memset(outputInterleaved, 0, outputFrames * 2 * sizeof(float));
    }

    return outputFrames;
}
```

### 3.5 SignalsmithRenderThread (Kotlin)

**역할**: AudioTrack을 별도 스레드에서 관리 (Superpowered의 OpenSL ES 대체)

```kotlin
class SignalsmithRenderThread @Inject constructor(
    private val engine: SignalsmithAudioEngine
) {
    private var audioTrack: AudioTrack? = null
    private var renderThread: Thread? = null
    private var isRunning = false

    fun start(sampleRate: Int, channelCount: Int) {
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            if (channelCount == 2) AudioFormat.CHANNEL_OUT_STEREO else AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioFormat(AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build())
            .setBufferSizeInBytes(bufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        isRunning = true
        renderThread = Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            audioTrack?.play()

            val outputBuffer = ShortArray(RENDER_FRAMES * 2)
            
            while (isRunning) {
                // Native에서 처리된 PCM 가져오기
                val framesProcessed = engine.process(outputBuffer, RENDER_FRAMES)
                
                if (framesProcessed > 0) {
                    audioTrack?.write(outputBuffer, 0, framesProcessed * 2)
                }
            }
        }
        renderThread?.start()
    }
}
```

---

## 4. 데이터 플로우 상세

### 4.1 VIDEO_WITH_DSP 모드 흐름

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         VIDEO_WITH_DSP 모드 흐름                            │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ExoPlayer                                                                 │
│        │                                                                    │
│        ├── Video Track ────────────────────────→ Surface (화면 출력)        │
│        │                                                                    │
│        └── Audio Track ────→ HybridAudioSink                               │
│                                    │                                        │
│                                    └── SignalsmithBridge                    │
│                                             │                               │
│                                             └── JNI → Ring Buffer           │
│                                                          │                  │
│                                                          └── processAudio() │
│                                                                   │         │
│                                                                   └── AudioTrack │
│                                                                              │
│   결과: 비디오는 ExoPlayer가 렌더링, 오디오는 DSP 처리 후 별도 출력          │
│                                                                              │
└────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Tempo 처리 (입출력 프레임 수 차이)

```
Tempo = 0.5x (느리게)
┌─────────────────────────────────────────────────────────────────────────────┐
│  요청: 512 출력 프레임                                                       │
│  필요: 512 × 0.5 = 256 입력 프레임                                          │
│                                                                              │
│  입력: [256 frames] ──→ Signalsmith ──→ [512 frames] 출력                   │
│          원본              Time Stretch      2배 늘어남                      │
└─────────────────────────────────────────────────────────────────────────────┘

Tempo = 2.0x (빠르게)
┌─────────────────────────────────────────────────────────────────────────────┐
│  요청: 512 출력 프레임                                                       │
│  필요: 512 × 2.0 = 1024 입력 프레임                                         │
│                                                                              │
│  입력: [1024 frames] ──→ Signalsmith ──→ [512 frames] 출력                  │
│           원본               Time Stretch     절반으로                       │
└─────────────────────────────────────────────────────────────────────────────┘

소수점 처리 (tempoAccumulator)
┌─────────────────────────────────────────────────────────────────────────────┐
│  Tempo = 1.5x                                                                │
│                                                                              │
│  Frame 1: 512 × 1.5 = 768.0 → 읽기: 768, 누적: 0.0                          │
│  Frame 2: 512 × 1.5 + 0.0 = 768.0 → 읽기: 768, 누적: 0.0                    │
│                                                                              │
│  Tempo = 1.3x                                                                │
│  Frame 1: 512 × 1.3 = 665.6 → 읽기: 665, 누적: 0.6                          │
│  Frame 2: 512 × 1.3 + 0.6 = 666.2 → 읽기: 666, 누적: 0.2                    │
│  Frame 3: 512 × 1.3 + 0.2 = 665.8 → 읽기: 665, 누적: 0.8                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. 기술적 챌린지 및 해결

### 5.1 Seek 시 음수 Position 표시

**문제**: Seek 후 `getCurrentPositionUs()`가 잘못된 값 반환

**원인**: `flush()` 후 새 PCM 도착 전에 Position 요청

**해결**: `isSeeking` 플래그 도입

```kotlin
override fun flush() {
    isSeeking = true  // Seek 시작
    isFirstChunkAfterFlush = true
    engine.flush()
}

override fun pushPcm(...): Boolean {
    if (isFirstChunkAfterFlush) {
        // 새 PCM 도착 시 기준점 설정
        firstPtsUs = presentationTimeUs
        firstPtsSystemTimeNs = System.nanoTime()
        isSeeking = false  // Seek 완료
    }
    // ...
}

override fun getCurrentPositionUs(): Long {
    if (isSeeking) return Long.MIN_VALUE  // POSITION_NOT_SET
    // 정상 계산...
}
```

### 5.2 Pitch/Tempo 변경이 적용 안 됨

**문제**: UI에서 조절해도 소리 변화 없음

**원인**: VIDEO 모드용 경로(ExoPlayer.playbackParameters)만 사용

**해결**: 모드별 분기 처리

```kotlin
// AudioEffectHandlerImpl.kt
override fun setPitch(value: Int) {
    val semitones = (value - 100) / 10f
    val ratio = 2f.pow(semitones / 12f)
    
    when (playbackModeController.currentMode) {
        PlaybackMode.VIDEO -> {
            exoPlayer.playbackParameters = PlaybackParameters(speed, ratio)
        }
        PlaybackMode.AUDIO, PlaybackMode.VIDEO_WITH_DSP -> {
            playbackModeController.setPitch(ratio)  // Signalsmith로 직접 전달
        }
    }
}
```

### 5.3 일시정지 후 Position 점프 (미해결)

**문제**: 30초 일시정지 후 재개 시 Position이 30초 점프

**원인**: Wall Clock 기반 계산의 한계

```
firstPtsSystemTimeNs = T0
일시정지 30초
현재 시간 = T0 + 30초
Position = firstPtsUs + 30초  ← 잘못됨!
```

**제안된 해결책** (미구현):

```kotlin
// 방안 1: 일시정지 시간 추적
private var totalPausedNs: Long = 0
private var pausedAtNs: Long = 0

fun pause() {
    pausedAtNs = System.nanoTime()
}

fun play() {
    if (pausedAtNs > 0) {
        totalPausedNs += System.nanoTime() - pausedAtNs
        pausedAtNs = 0
    }
}

fun getCurrentPositionUs(): Long {
    val elapsedNs = System.nanoTime() - firstPtsSystemTimeNs - totalPausedNs
    return firstPtsUs + (elapsedNs / 1000)
}
```

---

## 6. 파일 구조 및 역할

### 6.1 Kotlin/Java

```
media/src/main/java/com/example/media/
├── audio/
│   ├── PlaybackMode.kt              # enum: VIDEO, AUDIO, VIDEO_WITH_DSP
│   ├── PlaybackModeController.kt    # 모드 전환, pitch/tempo 전달
│   ├── HybridAudioSink.kt           # AudioSink 구현, PCM 라우팅
│   ├── SuperpoweredBridge.kt        # 인터페이스 (네이밍 유지)
│   └── SignalsmithBridgeImpl.kt     # JNI 브릿지, Position 계산
├── audio_effect/
│   └── AudioEffectHandlerImpl.kt    # 이펙트 핸들러, 모드별 분기
└── di/
    └── MediaModule.kt               # Hilt DI 설정

audio/src/main/java/com/example/audio/
├── SignalsmithAudioEngine.kt        # JNI 네이티브 메서드 선언
└── SignalsmithRenderThread.kt       # AudioTrack 렌더 스레드
```

### 6.2 C++ Native

```
audio/src/main/cpp/
├── SignalsmithAudioBridge.cpp       # 핵심 구현
│   ├── SpscRingBuffer              # Lock-free 원형 버퍼
│   ├── processAudio()              # DSP 처리 로직
│   ├── nativeInit()                # 엔진 초기화
│   ├── nativeWritePcm()            # PCM 버퍼에 쓰기
│   ├── nativeProcess()             # Kotlin에서 호출하여 처리된 PCM 받기
│   ├── nativeSetPitch()            # 피치 설정
│   └── nativeSetTempo()            # 템포 설정
│
└── signalsmith/
    └── signalsmith-stretch.h        # Header-only 라이브러리
```

### 6.3 각 파일의 역할 요약

| 파일 | 역할 | 핵심 기능 |
|:-----|:-----|:----------|
| `HybridAudioSink.kt` | PCM 데이터 분기점 | 3가지 모드 라우팅 |
| `SignalsmithBridgeImpl.kt` | JNI 브릿지 | Position 계산, Seek 상태 관리 |
| `SignalsmithAudioEngine.kt` | JNI 인터페이스 | native 메서드 선언 |
| `SignalsmithRenderThread.kt` | 오디오 출력 | AudioTrack 관리 |
| `SignalsmithAudioBridge.cpp` | Native DSP | SpscRingBuffer, Signalsmith 처리 |
| `PlaybackModeController.kt` | 모드 관리 | VIDEO ↔ AUDIO ↔ VIDEO_WITH_DSP |
| `AudioEffectHandlerImpl.kt` | 이펙트 라우팅 | 모드별 pitch/tempo 전달 |

---

## 7. Superpowered와의 차이점

| 측면 | Superpowered | Signalsmith |
|:-----|:-------------|:------------|
| **라이선스** | 상용 | MIT |
| **오디오 출력** | OpenSL ES 내장 | Kotlin AudioTrack |
| **Ring Buffer** | Mutex 기반 `PcmRingBuffer` | Lock-free `SpscRingBuffer` |
| **DSP 호출** | C++ 콜백에서 자동 | Kotlin에서 `nativeProcess()` 명시적 호출 |
| **모드** | VIDEO, AUDIO | VIDEO, AUDIO, VIDEO_WITH_DSP |
| **알고리즘** | WSOLA | STFT Phase Vocoder |

### 7.1 오디오 출력 방식 차이

```
Superpowered:
ExoPlayer → Ring Buffer → [OpenSL ES 콜백] → TimeStretching → 하드웨어
                              C++에서 자동 호출

Signalsmith:
ExoPlayer → Ring Buffer → [Kotlin 렌더 스레드] → nativeProcess() → AudioTrack
                              명시적 루프에서 호출
```

---

## 8. 성과 및 결론

### 8.1 달성 목표

| 목표 | 결과 |
|:-----|:-----|
| Superpowered 완전 대체 | O |
| MIT 라이선스 유지 | O |
| VIDEO_WITH_DSP 모드 | O |
| Seek 버그 수정 | O (음수 Position) |
| Pitch/Tempo 라우팅 | O |

### 8.2 미해결 이슈

| 이슈 | 상태 | 우선순위 |
|:-----|:-----|:---------|
| 일시정지 후 Position 점프 | 미해결 | 중 |
| VIDEO_WITH_DSP 립싱크 | 테스트 필요 | 중 |

### 8.3 향후 개선 방향

1. **AudioProcessor 주입 방식 검토**: `TODO_AUDIOPROCESSOR_MIGRATION.md` 참조
2. **Position 계산 개선**: Wall Clock → Native Frame 기반
3. **추가 이펙트**: EQ, Reverb 등 (MIT 라이브러리 활용)

---

*이 문서는 `signalsmith-stretch` 브랜치의 기술 구현을 정리한 포트폴리오 자료입니다.*
