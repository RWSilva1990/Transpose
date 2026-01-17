# Transpose 프로젝트 오디오 시스템 분석서

## 1. 프로젝트 구조 개요

### 1.1 모듈 구성

```
Transpose/
├── app/                          # 앱 진입점
├── core/
│   ├── domain/                   # 비즈니스 로직 (Repository 인터페이스)
│   ├── data/                     # 데이터 레이어 (Room, Firebase, NewPipe)
│   ├── ui/                       # 공유 UI 컴포넌트
│   └── utils/                    # 유틸리티
├── audio/                        # 네이티브 C++ 오디오 처리
├── media/                        # 미디어 재생 및 이펙트 관리
├── feature/
│   ├── main/                     # 메인 네비게이션
│   ├── home/                     # 홈 화면
│   ├── library/                  # 라이브러리
│   └── convert/                  # 오디오 편집 (이펙트 UI)
└── build-logic/convention/       # Gradle 컨벤션 플러그인
```

### 1.2 모듈 의존성 그래프

```
feature:convert → media → audio → core:utils
       ↓
   core:ui → core:domain
```

---

## 2. 오디오 이펙트 라이브러리 상세 분석

### 2.1 사용 라이브러리 목록

| 라이브러리 | 버전/출처 | 라이선스 | 용도 | 소스 크기 |
|-----------|----------|---------|------|----------|
| **Signalsmith Stretch** | v2.0+ | MIT | Pitch Shift, Time Stretch | 3.2 MB |
| **Signalsmith Basics** | Latest | MIT | Chorus, Limiter, Reverb, Crunch, EQ | (포함) |
| **DaisySP** | Latest | MIT | Phaser, Flanger, Tremolo, Autowah, Decimator | 480 KB |
| **MIT HRTF Library** | 2012 | MIT | 3D 공간 오디오 (Head-Related Transfer Function) | 15 MB |
| **FFTConvolver** | Latest | MIT | FFT 기반 컨볼루션 | 240 KB |
| **IIR1** | Latest | MIT | Biquad 필터 (사용 준비) | 212 KB |
| **Superpowered** | Legacy | 상용 | (레거시, 미사용) | 27 MB |

**총 네이티브 소스 크기**: 47 MB
**컴파일된 라이브러리 크기** (stripped):
- arm64-v8a: 2.8 MB
- armeabi-v7a: 2.7 MB
- x86_64: 2.9 MB
- x86: 2.9 MB

### 2.2 구현된 이펙트 (13개)

#### Core Effects
| 이펙트 | 라이브러리 | 파라미터 |
|-------|-----------|---------|
| **Pitch Shift** | Signalsmith Stretch | semitones (-24 ~ +24) |
| **Tempo** | Signalsmith Stretch | rate (0.5 ~ 2.0) |

#### Signalsmith Basics Effects
| 이펙트 | 파라미터 |
|-------|---------|
| **Chorus** | mix, depthMs, detune, stereo |
| **Limiter** | inputGainDb, limitDb, attackMs, releaseMs |
| **Reverb** | dry, wet, roomMs, decaySec |
| **Crunch (Distortion)** | driveDb, fuzz, toneHz |
| **5-Band EQ** | band[0-4]: freq, gainDb |
| **Compressor** | thresholdDb, ratio, attackMs, releaseMs, makeupGainDb |

#### DaisySP Effects
| 이펙트 | 파라미터 |
|-------|---------|
| **Phaser** | lfoFreq, lfoDepth, feedback, poles |
| **Flanger** | lfoFreq, lfoDepth, feedback, delayMs |
| **Tremolo** | freq, depth, waveform |
| **Autowah** | wah, mix, level |
| **Decimator** | bitcrush, downsample |

#### Spatial Audio
| 이펙트 | 라이브러리 | 파라미터 |
|-------|-----------|---------|
| **HRTF Virtualizer** | MIT HRTF + FFTConvolver | intensity, azimuth |

---

## 3. 아키텍처 흐름: UI → 오디오 처리

### 3.1 데이터 흐름도

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              UI Layer                                    │
│  ConvertAudioEditScreen.kt → ConvertAudioEditViewModel.kt               │
│  (Compose UI Components)      (StateFlow delegation)                    │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          Manager Layer                                   │
│  AudioEffectsManager.kt                                                  │
│  - StateFlow로 모든 이펙트 상태 관리                                      │
│  - Android AudioEffect: MediaController SessionCommand로 전달            │
│  - Signalsmith/DaisySP: SignalsmithAudioProcessor 직접 호출              │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
┌──────────────────────────────┐  ┌──────────────────────────────────────┐
│   Android AudioEffect API    │  │    SignalsmithAudioProcessor.kt      │
│   (BassBoost, Equalizer,     │  │    - Media3 AudioProcessor 구현       │
│    PresetReverb, Virtualizer,│  │    - JNI를 통해 C++ 호출              │
│    HapticGenerator)          │  │    - PCM 16-bit 처리                  │
└──────────────────────────────┘  └──────────────────────────────────────┘
                                                    │
                                                    ▼ JNI
┌─────────────────────────────────────────────────────────────────────────┐
│                        Native C++ Layer                                  │
│  SignalsmithProcessor.cpp (클래스 기반, per-instance)                    │
│  SignalsmithAudioBridge.cpp (전역 상태, Lock-free SPSC Ring Buffer)      │
│                                                                          │
│  [Audio Pipeline]                                                        │
│  Input → Signalsmith Stretch → Chorus → Limiter → Reverb → Crunch →     │
│          EQ → Compressor → HRTF → Phaser → Flanger → Tremolo →          │
│          Autowah → Decimator → Output                                   │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 재생 모드

```kotlin
enum class PlaybackMode {
    VIDEO,           // 비디오 재생, DSP 없음
    AUDIO,           // 오디오 전용, 전체 DSP 활성화
    VIDEO_WITH_DSP   // 비디오 + DSP (하이브리드)
}
```

`PlaybackModeController`가 `HybridAudioSink`를 통해 모드 전환 관리.

### 3.3 파일 경로 참조

| 레이어 | 파일 |
|-------|------|
| UI | `feature/convert/.../ConvertAudioEditScreen.kt` |
| ViewModel | `feature/convert/.../ConvertAudioEditViewModel.kt` |
| Manager | `media/.../manager/AudioEffectsManager.kt` |
| Processor | `media/.../audio/SignalsmithAudioProcessor.kt` |
| Native | `audio/src/main/cpp/SignalsmithProcessor.cpp` |
| Native Bridge | `audio/src/main/cpp/SignalsmithAudioBridge.cpp` |

---

## 4. 스레드 안정성 분석

### 4.1 C++ 레이어 스레드 모델

#### SignalsmithProcessor.cpp (클래스 기반)
```cpp
// 모든 파라미터는 std::atomic으로 스레드 세이프
std::atomic<float> pitchSemitones_;
std::atomic<bool> chorusEnabled_;
std::atomic<float> chorusMix_;
// ... 50+ atomic 변수

// memory_order_relaxed 사용 (성능 최적화)
void setPitchSemitones(float semitones) {
    pitchSemitones_.store(semitones, std::memory_order_relaxed);
}
```

**평가**:
- UI 스레드에서 파라미터 변경 → Audio 스레드에서 읽기
- `memory_order_relaxed`로 최소 오버헤드
- 이펙트 객체(chorus, reverb 등)는 process()에서만 접근 → 단일 스레드

#### SignalsmithAudioBridge.cpp (전역 상태)
```cpp
// Lock-free SPSC Ring Buffer
class SpscRingBuffer {
    std::atomic<size_t> head_;
    std::atomic<size_t> tail_;
    // Producer: Java 스레드 (writePcm)
    // Consumer: Render 스레드 (processAudio)
};
```

**평가**:
- Producer-Consumer 패턴으로 Lock-free
- 전역 static 변수들도 모두 atomic
- 언더런 카운트로 버퍼 부족 모니터링

### 4.2 Kotlin/Java 레이어

```kotlin
// SignalsmithAudioProcessor.kt
@Volatile
private var nativeHandle: Long = 0

@Volatile
private var pitchSemitones: Float = 0f
```

**평가**:
- `@Volatile`로 가시성 보장
- JNI 호출은 synchronized 없이 진행 (C++에서 atomic 처리)

### 4.3 스레드 안정성 요약

| 컴포넌트 | 스레드 모델 | 동기화 방식 | 안전성 |
|---------|-----------|-----------|-------|
| UI/ViewModel | Main Thread | Coroutine/StateFlow | ✅ |
| AudioEffectsManager | Main Thread | StateFlow | ✅ |
| SignalsmithProcessor | JNI Thread | std::atomic | ✅ |
| Ring Buffer | Multi-thread | Lock-free SPSC | ✅ |
| Effect Objects | Single Thread | None (process only) | ✅ |

---

## 5. 성능 및 리소스 분석

### 5.1 CPU 사용량 예측

| 이펙트 | 연산 복잡도 | CPU 영향 |
|-------|-----------|---------|
| Pitch Shift | O(n log n) FFT | 높음 |
| EQ (5-band) | O(n) IIR | 낮음 |
| Reverb | O(n) + Delay lines | 중간 |
| HRTF Convolution | O(n log n) FFT | 높음 |
| Phaser/Flanger | O(n) Allpass | 낮음 |
| Compressor | O(n) Envelope | 낮음 |

**최적화 플래그**:
```cmake
target_compile_options(signalsmith_audio PRIVATE -O3 -ffast-math -fsigned-char)
```

### 5.2 메모리 사용량

```
Ring Buffer: 44100 * 2 * 10 * 4 bytes = ~3.5 MB (10초 버퍼)
Process Block: 512 frames * 2 channels * 4 bytes = 4 KB
Effect Buffers: 각 이펙트당 ~4-8 KB
HRTF Impulse Response: ~32 KB per filter
```

**예상 총 메모리**: 약 5-8 MB (활성 이펙트에 따라 변동)

### 5.3 배터리 영향

| 요소 | 영향도 | 설명 |
|-----|-------|-----|
| FFT 연산 (Pitch, HRTF) | 높음 | NEON SIMD 최적화 적용 |
| Block 처리 (512 frames) | 중간 | 효율적인 버퍼 크기 |
| Atomic 연산 | 낮음 | relaxed ordering |
| 비활성 이펙트 | 없음 | Bypass 로직 |

**권장사항**:
- 불필요한 이펙트 비활성화
- Pitch/HRTF 동시 사용 시 배터리 소모 증가
- 백그라운드 재생 시 DSP 비활성화 고려

### 5.4 지연 시간 (Latency)

```cpp
const int PROCESS_BLOCK_FRAMES = 512;  // ~11.6ms @ 44100Hz

// Signalsmith Stretch latency
stretch_.inputLatency()   // 입력 지연
stretch_.outputLatency()  // 출력 지연
```

**예상 총 지연**: 약 20-50ms (pitch shift 활성화 시)

---

## 6. 새 이펙트 라이브러리 추가 가이드

### 6.1 고려사항 체크리스트

| 항목 | 설명 | 중요도 |
|-----|------|-------|
| **라이선스** | MIT/BSD 권장, GPL 주의 | 필수 |
| **크기** | APK 크기 영향, 각 아키텍처별 | 중요 |
| **C++ 표준** | C++17 호환 필요 | 필수 |
| **NEON 지원** | ARM 최적화 여부 | 권장 |
| **스레드 안정성** | 단일 스레드 또는 atomic 필요 | 필수 |
| **Sample Rate** | 44.1kHz/48kHz 지원 | 필수 |
| **Stereo 지원** | 2채널 처리 가능 여부 | 필수 |

### 6.2 추가 절차

1. **CMakeLists.txt 수정**
```cmake
# 새 라이브러리 경로 추가
set(NEW_LIB_PATH ${CMAKE_SOURCE_DIR}/new_library)
include_directories(${NEW_LIB_PATH})

# 소스 파일 추가
add_library(signalsmith_audio SHARED
    ...
    ${NEW_LIB_PATH}/effect.cpp
)
```

2. **SignalsmithProcessor.cpp 수정**
```cpp
#include "new_library/effect.h"

class SignalsmithProcessor {
    // 이펙트 인스턴스 추가
    NewEffect newEffect_;
    std::atomic<bool> newEffectEnabled_;
    std::atomic<float> newEffectParam_;

    // applyEffects()에 추가
    void applyEffects(int frames) {
        if (newEffectEnabled_.load(std::memory_order_relaxed)) {
            // 이펙트 처리
        }
    }
};
```

3. **JNI 함수 추가**
```cpp
extern "C" JNIEXPORT void JNICALL
Java_com_example_media_audio_SignalsmithAudioProcessor_nativeSetNewEffectEnabled(
    JNIEnv*, jobject, jlong handle, jboolean enabled);
```

4. **Kotlin 인터페이스 추가**
```kotlin
// SignalsmithAudioProcessor.kt
fun setNewEffectEnabled(enabled: Boolean) { ... }
private external fun nativeSetNewEffectEnabled(handle: Long, enabled: Boolean)
```

5. **AudioEffectsManager.kt 추가**
```kotlin
private val _isNewEffectEnabled = MutableStateFlow(false)
val isNewEffectEnabled: StateFlow<Boolean> = _isNewEffectEnabled.asStateFlow()
```

6. **UI 컴포넌트 추가**
```kotlin
// feature/convert/.../components/signalsmith/NewEffectSection.kt
@Composable
fun NewEffectSection(viewModel: ConvertAudioEditViewModel) { ... }
```

### 6.3 권장 라이브러리

| 라이브러리 | 용도 | 라이선스 | 크기 |
|-----------|------|---------|------|
| **Rubber Band** | 고품질 Time Stretch | GPL (주의) | ~2 MB |
| **SoundTouch** | Pitch/Tempo | LGPL | ~500 KB |
| **r8brain** | 리샘플링 | MIT | ~100 KB |
| **FFTW3** | FFT 최적화 | GPL (주의) | ~1 MB |
| **Speex DSP** | 리버브/AEC | BSD | ~300 KB |

---

## 7. 현재 아키텍처의 장단점

### 7.1 장점

1. **완전한 MIT 라이선스 스택**: Signalsmith + DaisySP로 상용 배포 가능
2. **Lock-free 설계**: 오디오 글리치 최소화
3. **모듈화**: 이펙트 추가/제거 용이
4. **스레드 안전**: Atomic 변수로 동기화 문제 없음
5. **Bypass 최적화**: 비활성 이펙트 시 CPU 소모 없음

### 7.2 단점 및 개선 포인트

1. **중복 구현**: SignalsmithProcessor + SignalsmithAudioBridge 두 가지 구현 존재
2. **전역 상태**: Bridge 버전이 static 변수 사용 (테스트 어려움)
3. **Superpowered 레거시**: 27MB 미사용 코드 존재 (제거 권장)
4. **HRTF 고정 샘플레이트**: 44100Hz로 하드코딩됨

### 7.3 권장 개선사항

1. 미사용 Superpowered 디렉토리 제거 (27MB 절감)
2. HRTF 동적 샘플레이트 지원
3. 이펙트 체인 순서 설정 기능
4. 프리셋 시스템 확장

---

## 8. 요약

### 핵심 기술 스택

| 레이어 | 기술 |
|-------|------|
| UI | Jetpack Compose |
| 상태관리 | StateFlow + Hilt DI |
| 재생 | Media3 ExoPlayer |
| 오디오처리 | JNI + C++17 |
| DSP | Signalsmith Stretch + Basics + DaisySP |
| 공간음향 | MIT HRTF + FFTConvolver |

### 컴파일된 바이너리 크기

| 아키텍처 | 크기 |
|---------|------|
| arm64-v8a | 2.8 MB |
| armeabi-v7a | 2.7 MB |
| x86_64 | 2.9 MB |
| x86 | 2.9 MB |
| **총합 (4 ABI)** | **11.3 MB** |

### 이펙트 카테고리

- **Core**: 2개 (Pitch, Tempo)
- **Dynamics**: 3개 (Limiter, Compressor, Crunch)
- **Modulation**: 5개 (Chorus, Phaser, Flanger, Tremolo, Autowah)
- **EQ/Filter**: 2개 (5-Band EQ, Decimator)
- **Spatial**: 2개 (HRTF Virtualizer, Reverb)

---

*문서 작성일: 2025-01-14*
*분석 대상 브랜치: feature/audioprocessor-injection*
