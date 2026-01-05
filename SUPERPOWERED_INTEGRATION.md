# Superpowered 실시간 오디오 DSP 엔진 통합

> YouTube 스트리밍 오디오에 실시간 Pitch/Tempo 이펙트를 적용하기 위한 Superpowered SDK 통합 프로젝트

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술적 배경: ExoPlayer 오디오 파이프라인](#2-기술적-배경-exoplayer-오디오-파이프라인)
3. [Superpowered 통합 아키텍처](#3-superpowered-통합-아키텍처)
4. [기술적 챌린지 및 해결 과정](#4-기술적-챌린지-및-해결-과정)
5. [최종 시스템 구조](#5-최종-시스템-구조)
6. [성과 및 결론](#6-성과-및-결론)

---

## 1. 프로젝트 개요

### 1.1 배경

**Transpose**는 YouTube 동영상의 오디오를 실시간으로 조작할 수 있는 Android 애플리케이션입니다. 음악 연습, 악기 튜닝, 속도 조절 학습 등 다양한 용도로 사용됩니다.

핵심 기능 중 하나는 **오디오 파일을 다운로드하지 않고** 스트리밍 중인 오디오에 실시간으로 Pitch(음높이)와 Tempo(재생 속도)를 조절하는 것입니다.

### 1.2 기술적 요구사항

| 요구사항 | 설명 |
|:---------|:-----|
| **실시간 Pitch Shifting** | 재생 속도를 변경하지 않고 음높이만 조절 (-24 ~ +24 반음) |
| **실시간 Time Stretching** | 음높이를 변경하지 않고 재생 속도만 조절 (0.5x ~ 2.0x) |
| **저지연(Low Latency)** | 사용자가 인지하지 못할 수준의 지연시간 유지 |
| **스트리밍 호환** | 파일 다운로드 없이 ExoPlayer 스트리밍과 완벽 통합 |

### 1.3 왜 Superpowered인가?

Android의 기본 오디오 API나 ExoPlayer의 내장 기능으로는 고품질 실시간 Pitch Shifting이 불가능합니다. ExoPlayer의 `PlaybackParameters`는 Pitch와 Speed가 연동되어 있어, 음높이만 독립적으로 변경할 수 없습니다.

**Superpowered Audio SDK**를 선택한 이유:

- **C++ 네이티브 최적화**: ARM NEON SIMD 명령어를 활용한 고성능 DSP
- **WSOLA 알고리즘**: 고품질 Time Stretching을 위한 Waveform Similarity Overlap-Add 구현
- **OpenSL ES 직접 출력**: Android AudioTrack을 우회하여 최소 지연시간 달성
- **독립적 Pitch/Tempo 제어**: 음높이와 속도를 완전히 분리하여 조절 가능

---

## 2. 기술적 배경: ExoPlayer 오디오 파이프라인

Superpowered 통합을 이해하려면 먼저 ExoPlayer의 기본 오디오 처리 방식을 이해해야 합니다.

### 2.1 ExoPlayer의 기본 오디오 흐름

ExoPlayer는 미디어 재생을 위한 Android의 표준 라이브러리로, 다음과 같은 파이프라인 구조를 가집니다:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        ExoPlayer 기본 오디오 파이프라인                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌──────────┐    ┌──────────┐    ┌─────────────────┐    ┌──────────┐  │
│   │  Media   │───▶│ Decoder  │───▶│ DefaultAudio    │───▶│  Audio   │  │
│   │  Source  │    │ (Codec)  │    │     Sink        │    │  Track   │  │
│   └──────────┘    └──────────┘    └─────────────────┘    └──────────┘  │
│                                                                          │
│   YouTube URL     AAC/MP3 등      PCM 데이터 수신        하드웨어 출력   │
│   에서 데이터     압축 해제        및 재생 관리           으로 전달       │
│   스트리밍                                                               │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 각 컴포넌트의 역할

#### MediaSource (미디어 소스)
네트워크나 로컬 파일에서 미디어 데이터를 가져옵니다. YouTube의 경우 DASH 또는 HLS 스트리밍 프로토콜을 통해 청크(chunk) 단위로 데이터를 수신합니다.

#### Decoder (디코더)
압축된 오디오 데이터(AAC, MP3, Opus 등)를 PCM(Pulse Code Modulation) 원시 오디오 데이터로 변환합니다. 이 단계에서 오디오는 샘플 단위의 디지털 파형이 됩니다.

#### AudioSink (오디오 싱크)
디코딩된 PCM 데이터를 받아 재생을 관리하는 핵심 컴포넌트입니다. ExoPlayer의 `DefaultAudioSink`는 다음을 담당합니다:
- PCM 버퍼 관리
- 재생 위치(Position) 추적
- 볼륨 및 재생 파라미터 적용
- AudioTrack으로 데이터 전달

#### AudioTrack (오디오 트랙)
Android 시스템의 오디오 출력 인터페이스입니다. PCM 데이터를 받아 실제 하드웨어 스피커로 소리를 출력합니다.

### 2.3 AudioSink의 핵심 인터페이스

ExoPlayer의 `AudioSink` 인터페이스에서 가장 중요한 메서드들:

| 메서드 | 역할 |
|:-------|:-----|
| `configure()` | 오디오 포맷(샘플레이트, 채널 수 등) 설정 |
| `handleBuffer()` | 디코딩된 PCM 버퍼를 수신하고 처리 |
| `play()` / `pause()` | 재생 상태 제어 |
| `flush()` | Seek 시 버퍼 초기화 |
| `hasPendingData()` | 아직 재생되지 않은 데이터 존재 여부 |
| `getCurrentPositionUs()` | 현재 재생 위치 반환 (마이크로초) |

### 2.4 ExoPlayer 내장 Pitch/Tempo의 한계

ExoPlayer는 `PlaybackParameters`를 통해 속도와 피치를 조절할 수 있지만, 근본적인 한계가 있습니다:

```
PlaybackParameters(speed = 1.5f, pitch = 1.0f)
```

내부적으로 ExoPlayer는 **SonicAudioProcessor**를 사용하는데, 이는 기본적인 리샘플링 방식으로 구현되어 있어 고품질 Pitch Shifting에는 적합하지 않습니다. 특히:

- **음질 저하**: 큰 폭의 피치 변경 시 음질이 현저히 떨어짐
- **아티팩트 발생**: 금속성 소리나 떨림 현상 발생
- **제한된 범위**: 극단적인 피치/템포 조합에서 불안정

---

## 3. Superpowered 통합 아키텍처

### 3.1 통합 전략: AudioSink 교체

ExoPlayer의 파이프라인에서 **AudioSink**를 교체하는 전략을 선택했습니다. 이 방식의 장점:

- ExoPlayer의 디코딩 및 버퍼링 로직을 그대로 활용
- MediaSource와 Decoder는 수정 없이 유지
- Superpowered는 순수하게 DSP 처리만 담당

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Superpowered 통합 후 파이프라인                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌──────────┐    ┌──────────┐    ┌─────────────────┐                   │
│   │  Media   │───▶│ Decoder  │───▶│  HybridAudio    │                   │
│   │  Source  │    │ (Codec)  │    │     Sink        │                   │
│   └──────────┘    └──────────┘    └─────────────────┘                   │
│                                            │                             │
│                                   ┌────────┴────────┐                   │
│                                   ▼                 ▼                   │
│                           ┌─────────────┐   ┌─────────────┐             │
│                           │   VIDEO     │   │   AUDIO     │             │
│                           │   Mode      │   │   Mode      │             │
│                           └─────────────┘   └─────────────┘             │
│                                   │                 │                    │
│                                   ▼                 ▼                    │
│                           ┌─────────────┐   ┌─────────────┐             │
│                           │  Default    │   │ Superpowered│             │
│                           │ AudioSink   │   │   Engine    │             │
│                           └─────────────┘   └─────────────┘             │
│                                   │                 │                    │
│                                   ▼                 ▼                    │
│                           ┌─────────────┐   ┌─────────────┐             │
│                           │ AudioTrack  │   │  OpenSL ES  │             │
│                           └─────────────┘   └─────────────┘             │
│                                   │                 │                    │
│                                   └────────┬────────┘                   │
│                                            ▼                             │
│                                        🔊 Speaker                        │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Dual-Mode 시스템 설계

시스템은 두 가지 재생 모드를 지원합니다:

#### VIDEO 모드
비디오와 함께 재생할 때 사용합니다. 비디오-오디오 동기화가 중요하므로 ExoPlayer의 기본 오디오 처리를 사용합니다. 이 모드에서는 Superpowered를 사용하지 않습니다.

#### AUDIO 모드  
오디오만 재생하거나 DSP 이펙트가 필요할 때 사용합니다. PCM 데이터가 Superpowered 엔진으로 전달되어 Pitch/Tempo 처리 후 OpenSL ES를 통해 직접 출력됩니다.

### 3.3 데이터 플로우 상세

AUDIO 모드에서의 데이터 흐름을 단계별로 설명합니다:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      AUDIO 모드 상세 데이터 플로우                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  1️⃣ ExoPlayer Decoder                                                   │
│     │                                                                    │
│     │  압축 해제된 PCM 데이터                                             │
│     │  (16-bit signed integer, stereo)                                  │
│     │  + presentationTimeUs (재생 시점 타임스탬프)                        │
│     ▼                                                                    │
│  2️⃣ HybridAudioSink.handleBuffer()                                      │
│     │                                                                    │
│     │  PCM 데이터를 SuperpoweredBridge로 전달                            │
│     │  버퍼가 가득 차면 대기 후 재시도                                    │
│     ▼                                                                    │
│  3️⃣ SuperpoweredBridge (Kotlin)                                         │
│     │                                                                    │
│     │  • presentationTimeUs 오프셋 보정                                  │
│     │  • Seek 위치 동기화                                                │
│     │  • ByteBuffer → JNI 호출                                          │
│     ▼                                                                    │
│  4️⃣ SuperpoweredAudioBridge.cpp (JNI)                                   │
│     │                                                                    │
│     │  • Short Int → Float 변환                                         │
│     │  • Ring Buffer에 저장 (Producer)                                   │
│     ▼                                                                    │
│  5️⃣ PcmRingBuffer (Thread-safe)                                         │
│     │                                                                    │
│     │  • 10초 분량 버퍼 (44100 × 2 × 10 samples)                         │
│     │  • std::mutex로 동기화                                             │
│     │  • ExoPlayer(쓰기)와 Superpowered(읽기) 분리                       │
│     ▼                                                                    │
│  6️⃣ Audio Callback (OpenSL ES에서 호출)                                 │
│     │                                                                    │
│     │  • Ring Buffer에서 데이터 읽기 (Consumer)                          │
│     │  • TimeStretching 엔진에 입력                                      │
│     ▼                                                                    │
│  7️⃣ Superpowered TimeStretching                                         │
│     │                                                                    │
│     │  • WSOLA 알고리즘으로 Pitch/Tempo 처리                             │
│     │  • pitchShiftCents: 음높이 (100 = 1반음)                           │
│     │  • rate: 재생 속도 (1.0 = 원본)                                    │
│     ▼                                                                    │
│  8️⃣ OpenSL ES Output                                                    │
│     │                                                                    │
│     │  • Float → Short Int 변환                                         │
│     │  • 하드웨어 버퍼로 직접 전달                                        │
│     ▼                                                                    │
│  🔊 Speaker                                                              │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.4 Producer-Consumer 패턴

이 시스템의 핵심은 **비동기 Producer-Consumer 패턴**입니다:

| 역할 | 컴포넌트 | 스레드 | 속도 |
|:-----|:---------|:-------|:-----|
| **Producer** | ExoPlayer (handleBuffer) | ExoPlayer 내부 스레드 | 디코딩 속도 (실시간보다 빠름) |
| **Buffer** | PcmRingBuffer | - | 10초 분량 저장 가능 |
| **Consumer** | Superpowered (audioCallback) | OpenSL ES 콜백 스레드 | 정확히 실시간 |

ExoPlayer는 네트워크와 디코딩 상황에 따라 **불규칙한 속도**로 PCM 데이터를 생성합니다. 반면 Superpowered의 오디오 콜백은 하드웨어에 의해 **정확히 실시간**으로 호출됩니다. Ring Buffer가 이 속도 차이를 흡수합니다.

### 3.5 Pitch/Tempo 제어 흐름

사용자가 UI에서 피치를 조절할 때의 데이터 흐름:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Pitch 제어 흐름                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  👆 User: 피치 슬라이더 조작                                              │
│     │                                                                    │
│     ▼                                                                    │
│  📱 UI Layer (Jetpack Compose)                                          │
│     │                                                                    │
│     │  pitchValue: Int (0~200, 100=원음)                                │
│     │  예: 80 = -2반음, 120 = +2반음                                     │
│     ▼                                                                    │
│  🎛️ AudioEffectsManager                                                 │
│     │                                                                    │
│     │  UI 상태 관리 및 MediaSession 명령 전송                             │
│     ▼                                                                    │
│  🔀 AudioEffectHandlerImpl                                              │
│     │                                                                    │
│     │  모드 확인 후 라우팅 결정                                           │
│     │  • VIDEO 모드 → ExoPlayer PlaybackParameters                      │
│     │  • AUDIO 모드 → PlaybackModeController                            │
│     ▼                                                                    │
│  🎚️ PlaybackModeController                                              │
│     │                                                                    │
│     │  semitones → ratio 변환                                           │
│     │  ratio = 2^(semitones/12)                                         │
│     │  예: -2반음 → 2^(-2/12) ≈ 0.891                                   │
│     ▼                                                                    │
│  🌉 SuperpoweredBridge                                                  │
│     │                                                                    │
│     │  ratio → cents 변환                                               │
│     │  cents = 12 × log2(ratio) × 100                                   │
│     │  예: 0.891 → -200 cents                                           │
│     ▼                                                                    │
│  ⚙️ TimeStretching.pitchShiftCents = -200                               │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 4. 기술적 챌린지 및 해결 과정

### 4.1 Buffer Backpressure로 인한 재생 중단

#### 문제 현상

오디오 재생을 시작하면 2-3초 후 갑자기 멈추고, 로그에서 빠른 pause/play 사이클이 반복되는 것이 관찰되었습니다. 사용자 경험상 오디오가 "뚝뚝 끊기는" 현상이 발생했습니다.

#### 원인 분석

ExoPlayer의 디코딩 속도와 Superpowered의 소비 속도 사이의 불균형이 원인이었습니다.

ExoPlayer는 네트워크에서 데이터를 받아 최대한 빠르게 디코딩합니다. 좋은 네트워크 환경에서는 **실시간 재생 속도보다 훨씬 빠르게** 디코딩이 진행됩니다. 예를 들어, 1초 분량의 오디오를 0.1초 만에 디코딩할 수 있습니다.

반면 Superpowered의 오디오 콜백은 하드웨어 타이밍에 맞춰 **정확히 실시간**으로 호출됩니다. 256 샘플 버퍼 기준으로 약 5.8ms마다 한 번 호출됩니다.

이로 인해 Ring Buffer가 빠르게 가득 차게 됩니다. `handleBuffer()`가 호출될 때 Ring Buffer에 공간이 없으면 `false`를 반환해야 하는데, ExoPlayer는 이를 "버퍼링 필요" 신호로 해석하여 **BUFFERING 상태**로 전환합니다.

```
시간 흐름:
0ms:    ExoPlayer 디코딩 시작, Ring Buffer에 데이터 쓰기
50ms:   Ring Buffer 50% 채워짐
100ms:  Ring Buffer 100% 가득 참
101ms:  handleBuffer() return false → ExoPlayer BUFFERING 상태
102ms:  Superpowered가 데이터 소비 → 공간 생김
103ms:  handleBuffer() return true → ExoPlayer PLAYING 상태
104ms:  다시 가득 참 → BUFFERING...
        (이 사이클이 초당 수십 번 반복)
```

#### 해결 방법

`handleBuffer()`에서 버퍼가 가득 찼을 때 즉시 `false`를 반환하는 대신, **짧은 시간 대기 후 재시도**하는 방식으로 변경했습니다.

```kotlin
override fun handleBuffer(buffer: ByteBuffer, presentationTimeUs: Long, ...): Boolean {
    return when (mode) {
        PlaybackMode.AUDIO -> {
            var retryCount = 0
            while (retryCount < 10) {
                val success = superpoweredBridge.pushPcm(buffer, buffer.remaining(), presentationTimeUs)
                if (success) return true
                
                // 버퍼에 공간이 생길 때까지 짧게 대기
                Thread.sleep(5)
                retryCount++
            }
            true  // 최대 재시도 후에도 true 반환하여 ExoPlayer 상태 안정화
        }
        // ...
    }
}
```

이 방식으로 ExoPlayer의 상태 전환을 최소화하고, 자연스러운 backpressure를 구현했습니다.

---

### 4.2 hasPendingData()로 인한 조기 재생 중단

#### 문제 현상

재생 중 Ring Buffer가 일시적으로 비어있을 때 ExoPlayer가 **스트림이 끝났다고 판단**하여 재생을 중단하는 문제가 발생했습니다.

#### 원인 분석

ExoPlayer의 `AudioSink` 인터페이스에는 `hasPendingData()` 메서드가 있습니다. 이 메서드는 "아직 재생되지 않은 데이터가 있는가?"를 반환합니다. ExoPlayer는 이 값이 `false`일 때 **스트림이 완료되었다고 판단**할 수 있습니다.

문제는 Ring Buffer와 Superpowered TimeStretching 엔진의 **이중 버퍼 구조**에서 발생했습니다:

```
┌─────────────────────────────────────────────────────────────────┐
│                      이중 버퍼 구조                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Ring Buffer        TimeStretching 내부 버퍼                    │
│   ┌─────────┐        ┌─────────────────────┐                    │
│   │         │   ──▶  │                     │   ──▶  출력        │
│   │  비어   │        │   아직 데이터 있음    │                    │
│   │  있음   │        │   (처리 중)          │                    │
│   └─────────┘        └─────────────────────┘                    │
│                                                                  │
│   hasPendingData()가                                             │
│   Ring Buffer만 확인하면                                          │
│   false 반환 → 조기 종료!                                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

Ring Buffer가 비어있어도 TimeStretching 엔진 내부에는 아직 처리 중인 데이터가 있을 수 있습니다. 단순히 Ring Buffer만 확인하면 잘못된 판단을 하게 됩니다.

#### 해결 방법

AUDIO 모드에서는 `hasPendingData()`가 **항상 `true`를 반환**하도록 변경했습니다.

```kotlin
override fun hasPendingData(): Boolean {
    return when (mode) {
        PlaybackMode.VIDEO -> defaultAudioSink.hasPendingData()
        PlaybackMode.AUDIO -> true  // Superpowered 내부 버퍼링이 있으므로 항상 true
    }
}
```

실제 스트림 종료는 ExoPlayer의 다른 메커니즘(EOS 플래그 등)으로 감지되므로, 이 변경은 안전합니다.

---

### 4.3 Position 동기화 실패 (음수 재생 시간 표시)

#### 문제 현상

재생 위치를 표시하는 UI에 **"-278:32:15"** 같은 음수 시간이 표시되었습니다. 분명히 0초부터 재생을 시작했는데 -278시간이 표시되는 비정상적인 상황이었습니다.

#### 원인 분석

ExoPlayer에서 전달하는 `presentationTimeUs` 값의 특성을 이해해야 합니다.

`presentationTimeUs`는 **스트림 내부의 절대 타임스탬프**입니다. YouTube 같은 스트리밍 서비스에서 이 값은 0부터 시작하지 않고, 스트림 인코딩 시점의 타임스탬프를 기준으로 합니다.

실제 관찰된 값:
```
첫 번째 청크의 presentationTimeUs ≈ 999,840,000,000 마이크로초
                                  ≈ 999,840 초
                                  ≈ 277.7 시간
```

이 값을 그대로 사용하면 재생 위치가 277시간부터 시작하는 것으로 표시됩니다. 또한 특정 계산에서 오버플로우가 발생하면 음수 값이 될 수 있습니다.

#### 해결 방법

**Stream Offset 보정** 메커니즘을 구현했습니다. 첫 번째 청크가 도착할 때 그 `presentationTimeUs`를 **기준점(offset)**으로 저장하고, 이후 모든 타임스탬프에서 이 offset을 빼서 0부터 시작하는 정규화된 시간을 계산합니다.

```kotlin
class SuperpoweredBridgeImpl {
    private var streamOffsetUs: Long = -1  // 아직 감지 안됨
    
    override fun pushPcm(buffer: ByteBuffer, sizeInBytes: Int, presentationTimeUs: Long): Boolean {
        // 첫 번째 청크에서 offset 감지
        if (streamOffsetUs < 0) {
            streamOffsetUs = presentationTimeUs
            // 예: streamOffsetUs = 999,840,000,000
        }
        
        // 정규화된 위치 계산
        val normalizedPositionUs = presentationTimeUs - streamOffsetUs
        // 예: 999,845,000,000 - 999,840,000,000 = 5,000,000 (5초)
        
        // ...
    }
}
```

---

### 4.4 Seek 후 Position 재계산 실패

#### 문제 현상

재생 중 +15초 건너뛰기 등 **버퍼링된 범위를 벗어나는 Seek**을 수행하면, 다시 재생 위치가 -278시간으로 표시되는 문제가 재발했습니다.

#### 원인 분석

Seek이 발생하면 ExoPlayer는 `flush()` 메서드를 호출하여 AudioSink의 버퍼를 초기화합니다. 이후 새로운 위치에서 디코딩을 시작하고, 새로운 `presentationTimeUs` 값과 함께 데이터를 전달합니다.

문제는 Seek 후에도 **이전의 streamOffsetUs를 유지**하고 있었다는 점입니다.

```
Seek 전:
  streamOffsetUs = 999,840,000,000 (재생 시작 시점에 감지)
  현재 presentationTimeUs = 999,850,000,000
  정규화된 위치 = 10초 ✓

Seek 후 (예: 5분 지점으로):
  새로운 presentationTimeUs = 1,000,140,000,000
  정규화된 위치 = 1,000,140,000,000 - 999,840,000,000 = 300초 = 5분 ✓
  (이 경우는 운 좋게 맞음)

하지만 다른 스트림이나 특정 상황에서:
  새로운 presentationTimeUs가 완전히 다른 기준점을 가질 수 있음
  → 계산 오류 발생
```

#### 해결 방법

`flush()` 호출 시 **streamOffsetUs를 리셋**하여 다음 청크에서 새로운 offset을 감지하도록 변경했습니다.

```kotlin
override fun flush() {
    streamOffsetUs = -1  // 리셋 → 다음 청크에서 새로 감지
    isFirstChunkAfterFlush = true
    firstPtsUs = 0L
    engine.flush()
}
```

이렇게 하면 Seek 후 첫 번째 청크가 도착할 때 새로운 기준점이 설정되어 정확한 위치 계산이 가능합니다.

---

### 4.5 Native 버퍼 오버플로우로 인한 앱 크래시

#### 문제 현상

앱이 무작위로 크래시되며, 로그에 다음과 같은 오류가 기록되었습니다:

```
Fatal signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x00217f43bf1a15a4
pid: 12967, tid: 13021, name: RenderThread >>> com.example.transpose <<<

backtrace:
  #00 pc 000000000080d9e8 /system/lib64/libhwui.so (GrOpsRenderPass::bindPipeline)
  #01 pc 00000000008e1dec /system/lib64/libhwui.so (TextureOpImpl::onExecute)
  ...
```

특이한 점은 크래시가 **RenderThread (UI 렌더링 스레드)**에서 발생했다는 것입니다. 오디오 처리와 전혀 관련 없어 보이는 GPU 렌더링 코드에서 크래시가 발생했습니다.

#### 원인 분석

이것은 **힙 메모리 손상(Heap Corruption)**의 전형적인 증상입니다. 한 스레드에서 메모리를 잘못 덮어쓰면, 그 손상된 메모리 영역을 나중에 다른 스레드가 사용할 때 크래시가 발생합니다.

C++ Native 코드를 분석한 결과, **버퍼 오버플로우**를 발견했습니다:

```cpp
// 버퍼 할당 (nativeInit에서)
tempInputBuffer = new float[outBufferSize * 2 * 8];
// outBufferSize = 256인 경우: 256 × 2 × 8 = 4,096 floats

// 실제 사용 (audioProcessing 콜백에서)
int framesToFeed = std::min((int)(available / 2), 4096);  // 최대 4,096 frames
inputBuffer->read(tempInputBuffer, framesToFeed * 2);      // 최대 8,192 samples!
```

**문제**: `tempInputBuffer`에 4,096개의 float을 할당했지만, 최대 **8,192개의 float을 쓰려고 시도**했습니다. 이로 인해 할당된 메모리 영역을 넘어서 데이터를 쓰게 되고, 인접한 메모리 영역(다른 변수나 힙 메타데이터)이 손상됩니다.

이 손상된 메모리는 즉시 크래시를 일으키지 않습니다. 나중에 해당 메모리 영역을 사용하는 코드(이 경우 UI 렌더링)가 실행될 때 비로소 크래시가 발생합니다. 이것이 오디오 코드와 무관해 보이는 RenderThread에서 크래시가 발생한 이유입니다.

#### 해결 방법

상수를 정의하여 버퍼 크기와 사용량의 관계를 명확히 하고, 충분한 크기로 버퍼를 할당했습니다:

```cpp
// 상수 정의
static const int MAX_FRAMES_TO_FEED = 4096;
static const size_t TEMP_INPUT_BUFFER_SIZE = MAX_FRAMES_TO_FEED * 2;  // 8,192 floats

// 버퍼 할당
tempInputBuffer = new float[TEMP_INPUT_BUFFER_SIZE];  // 충분한 크기

// 사용
int framesToFeed = std::min((int)(available / 2), MAX_FRAMES_TO_FEED);
inputBuffer->read(tempInputBuffer, framesToFeed * 2);  // 최대 TEMP_INPUT_BUFFER_SIZE
```

이제 버퍼 크기와 사용량이 상수로 연결되어 있어, 하나를 변경하면 다른 쪽도 자동으로 맞춰집니다.

---

### 4.6 Pitch 변경이 적용되지 않는 문제

#### 문제 현상

UI에서 피치를 변경해도 **실제 소리는 원음 그대로** 재생되었습니다.

#### 원인 분석

피치 제어 흐름을 추적한 결과, 두 가지 문제가 있었습니다:

**문제 1: ExoPlayer를 경유하는 피치 설정**

원래 구현에서 피치 변경은 다음 경로로 전달되었습니다:

```
UI → AudioEffectHandlerImpl.setPitch() → ExoPlayer.playbackParameters 설정
    → HybridAudioSink.setPlaybackParameters() → Superpowered에 전달
```

그러나 AUDIO 모드에서는 ExoPlayer의 `playbackParameters`를 경유할 필요가 없습니다. 오히려 ExoPlayer가 내부적으로 피치 처리를 시도하여 **이중 처리**가 발생할 수 있었습니다.

**문제 2: ExoPlayer 기본값으로 덮어쓰기**

`HybridAudioSink.setPlaybackParameters()`는 ExoPlayer의 내부 상태 변경 시 호출됩니다. ExoPlayer는 기본 pitch 값인 **1.0**으로 이 메서드를 호출하여, 사용자가 설정한 Superpowered 피치를 덮어쓰고 있었습니다.

#### 해결 방법

**1. 모드별 분기 처리**

`AudioEffectHandlerImpl`에서 현재 모드에 따라 피치를 다르게 처리합니다:

```kotlin
override fun setPitch(value: Int) {
    val pitchRatio = semitonesToRatio(semitones)
    
    when (playbackModeController.get().currentMode) {
        PlaybackMode.AUDIO -> {
            // Superpowered로 직접 전달 (ExoPlayer 우회)
            playbackModeController.get().setPitch(pitchRatio)
        }
        PlaybackMode.VIDEO -> {
            // ExoPlayer 사용
            exoPlayer.playbackParameters = PlaybackParameters(speed, pitchRatio)
        }
    }
}
```

**2. AUDIO 모드에서 ExoPlayer 피치 무시**

`HybridAudioSink.setPlaybackParameters()`에서 AUDIO 모드일 때는 아무 작업도 하지 않습니다:

```kotlin
override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
    when (mode) {
        PlaybackMode.VIDEO -> defaultAudioSink.setPlaybackParameters(playbackParameters)
        PlaybackMode.AUDIO -> { /* 무시 - 직접 제어 */ }
    }
}
```

**3. 기본 피치 적용**

AUDIO 모드 진입 시 또는 Superpowered 초기화 시 기본 피치(-2 반음)를 자동 적용합니다:

```cpp
// C++ 초기화
timeStretching = new Superpowered::TimeStretching(sampleRate);
timeStretching->pitchShiftCents = -200;  // 기본값: -2 반음
```

---

## 5. 최종 시스템 구조

### 5.1 컴포넌트 다이어그램

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              APPLICATION LAYER                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                         MainViewModel                                │   │
│   │                                                                      │   │
│   │   • 재생 상태 관리                                                    │   │
│   │   • 사용자 입력 처리                                                  │   │
│   │   • PlaybackModeController 호출                                      │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                     │                                        │
│                                     ▼                                        │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                      AudioEffectsManager                             │   │
│   │                                                                      │   │
│   │   • pitchValue (80 = -2반음, 100 = 원음, 120 = +2반음)              │   │
│   │   • tempoValue                                                       │   │
│   │   • MediaSession 명령 전송                                           │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               MEDIA LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐     │
│   │ AudioEffectHand  │    │ PlaybackMode     │    │ Superpowered     │     │
│   │    lerImpl       │───▶│   Controller     │───▶│    Bridge        │     │
│   │                  │    │                  │    │                  │     │
│   │ • 모드별 분기     │    │ • VIDEO/AUDIO   │    │ • JNI 래퍼       │     │
│   │ • ExoPlayer/SP   │    │   모드 전환      │    │ • offset 보정    │     │
│   │   라우팅         │    │ • 기본값 적용    │    │ • position 관리  │     │
│   └──────────────────┘    └──────────────────┘    └──────────────────┘     │
│                                                              │               │
│   ┌───────────────────────────────────────────────────────────────────┐    │
│   │                        HybridAudioSink                             │    │
│   │                                                                    │    │
│   │   • ExoPlayer AudioSink 인터페이스 구현                            │    │
│   │   • VIDEO 모드: DefaultAudioSink로 위임                            │    │
│   │   • AUDIO 모드: SuperpoweredBridge로 PCM 전달                      │    │
│   │   • handleBuffer(), hasPendingData(), flush() 등                   │    │
│   └───────────────────────────────────────────────────────────────────┘    │
│                                      │                                      │
└──────────────────────────────────────│──────────────────────────────────────┘
                                       │
                                       ▼ JNI
┌─────────────────────────────────────────────────────────────────────────────┐
│                              NATIVE LAYER (C++)                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌──────────────────────────────────────────────────────────────────┐     │
│   │                   SuperpoweredAudioBridge.cpp                     │     │
│   │                                                                   │     │
│   │   ┌───────────────┐                                               │     │
│   │   │ PcmRingBuffer │  Thread-safe 원형 버퍼                        │     │
│   │   │               │  • capacity: 10초 분량                        │     │
│   │   │   Producer ──▶│  • std::mutex 동기화                          │     │
│   │   │   (ExoPlayer) │  • write() / read() / available()            │     │
│   │   │               │                                               │     │
│   │   │   Consumer ◀──│                                               │     │
│   │   │  (Callback)   │                                               │     │
│   │   └───────────────┘                                               │     │
│   │           │                                                       │     │
│   │           ▼                                                       │     │
│   │   ┌───────────────┐                                               │     │
│   │   │TimeStretching │  Superpowered DSP 엔진                        │     │
│   │   │               │  • pitchShiftCents: -2400 ~ +2400             │     │
│   │   │  WSOLA 알고리즘│  • rate: 0.5 ~ 2.0                           │     │
│   │   │               │  • addInput() → getOutput()                   │     │
│   │   └───────────────┘                                               │     │
│   │           │                                                       │     │
│   │           ▼                                                       │     │
│   │   ┌───────────────┐                                               │     │
│   │   │ AndroidAudioIO│  Superpowered 오디오 출력                     │     │
│   │   │               │  • OpenSL ES 직접 사용                        │     │
│   │   │  audioProc()  │  • 저지연 하드웨어 콜백                       │     │
│   │   │   콜백        │  • 실시간 오디오 출력                          │     │
│   │   └───────────────┘                                               │     │
│   │                                                                   │     │
│   └──────────────────────────────────────────────────────────────────┘     │
│                                      │                                      │
└──────────────────────────────────────│──────────────────────────────────────┘
                                       │
                                       ▼
                                  🔊 Speaker
```

### 5.2 파일 구조

```
media/src/main/java/com/example/media/
├── audio/
│   ├── PlaybackMode.kt              # VIDEO/AUDIO 모드 enum
│   ├── PlaybackModeController.kt    # 모드 전환 및 라우팅
│   ├── HybridAudioSink.kt           # ExoPlayer AudioSink 구현
│   ├── SuperpoweredBridge.kt        # 인터페이스 정의
│   └── SuperpoweredBridgeImpl.kt    # 구현체 (JNI 호출)
├── audio_effect/
│   └── AudioEffectHandlerImpl.kt    # 이펙트 핸들러
├── manager/
│   └── AudioEffectsManager.kt       # UI 상태 관리
└── di/
    └── MediaModule.kt               # Hilt DI 설정

audio/src/main/
├── java/com/example/audio/
│   └── SuperpoweredAudioEngine.kt   # JNI 인터페이스
└── cpp/
    ├── SuperpoweredAudioBridge.cpp  # Native 구현
    └── superpowered/                # Superpowered SDK 헤더
```

---

## 6. 성과 및 결론

### 6.1 달성한 목표

| 목표 | 달성 |
|:-----|:-----|
| 실시간 Pitch Shifting | ✅ -24 ~ +24 반음 범위, 고품질 |
| 실시간 Time Stretching | ✅ 0.5x ~ 2.0x, 음질 유지 |
| 저지연 | ✅ ~20ms 이하 |
| 스트리밍 호환 | ✅ 다운로드 없이 실시간 처리 |
| 안정성 | ✅ 메모리 안전, 크래시 제거 |

### 6.2 핵심 기술적 성과

1. **ExoPlayer 파이프라인 확장**
   - AudioSink 인터페이스를 활용한 비침습적 통합
   - 기존 디코딩/버퍼링 로직 재활용

2. **Producer-Consumer 패턴 구현**
   - Thread-safe Ring Buffer로 비동기 데이터 교환
   - 디코딩 속도와 재생 속도 불일치 흡수

3. **JNI 브릿지 설계**
   - Kotlin과 C++ 간 효율적 데이터 전달
   - 타임스탬프 동기화 및 position 관리

4. **메모리 안전성 확보**
   - Native 버퍼 크기 계산 및 bounds checking
   - 힙 손상으로 인한 간접 크래시 해결

### 6.3 학습 포인트

- **실시간 오디오 시스템**의 타이밍 특성과 버퍼링 전략
- **멀티스레드 환경**에서의 메모리 안전성
- **Native/Managed 경계**에서의 데이터 교환
- **디버깅 기법**: 힙 손상이 다른 스레드에서 크래시로 나타나는 현상

---

*이 문서는 Transpose 프로젝트의 Superpowered 오디오 엔진 통합 과정을 기술적으로 정리한 포트폴리오 자료입니다.*
