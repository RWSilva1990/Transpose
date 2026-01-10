# 세션 기록 - 2025년 1월 5일 ~ 1월 9일

## 현재 오디오 라이브러리 구성

### C++ 라이브러리 (`audio/src/main/cpp/`)

| 라이브러리 | 라이선스 | 용도 | 상태 |
|-----------|---------|------|------|
| **Signalsmith Stretch** | MIT | Pitch/Tempo 변경 | ✅ 사용중 |
| **Signalsmith Basics** | MIT | Chorus, Limiter, Reverb, Crunch | ✅ 사용중 |
| **Signalsmith DSP** | MIT | EQ (Biquad Filters), Compressor | ✅ 사용중 |
| **mit-hrtf-lib** | MIT | HRTF 데이터 (MIT KEMAR) | ✅ 사용중 |
| **FFTConvolver** | MIT | 실시간 FFT 컨볼루션 | ✅ 사용중 |
| **Superpowered** | 상용 | (레거시, 미사용) | ❌ 미사용 |

### 구현된 오디오 이펙트

| 이펙트 | 라이브러리 | 파라미터 |
|-------|-----------|---------|
| **Pitch Shift** | Signalsmith Stretch | semitones (-12 ~ +12) |
| **Tempo** | Signalsmith Stretch | ratio (0.5x ~ 2.0x) |
| **Chorus** | Signalsmith Basics | rate, depth, mix |
| **Limiter** | Signalsmith Basics | threshold, release |
| **Reverb** | Signalsmith Basics | roomSize, damping, wet/dry |
| **Crunch** | Signalsmith Basics | drive, fuzz, tone |
| **5-Band EQ** | Signalsmith DSP | 60Hz, 250Hz, 1kHz, 4kHz, 12kHz |
| **Compressor** | Signalsmith DSP | threshold, ratio, attack, release, makeup |
| **3D HRTF Virtualizer** | mit-hrtf-lib + FFTConvolver | intensity, azimuth |

### Android AudioEffect (ExoPlayer 모드에서만 작동)

| 이펙트 | API | 상태 |
|-------|-----|------|
| Equalizer | android.media.audiofx | ✅ |
| BassBoost | android.media.audiofx | ✅ |
| Virtualizer | android.media.audiofx | ✅ |
| PresetReverb | android.media.audiofx | ✅ |
| LoudnessEnhancer | android.media.audiofx | ✅ |

---

## 1월 9일 작업 내역

### 1. 제거된 기능 (품질 미달)
- ❌ Voice Removal (단순 센터 채널 제거 - 효과 미비)
- ❌ Stereo Widener (기본 M/S 처리 - 효과 미비)
- ❌ 직접 구현한 Spatial Audio (HRTF 아님 - 효과 미비)

### 2. MIT HRTF Virtualizer 구현
**사용 라이브러리:**
- `mit-hrtf-lib` - MIT KEMAR HRTF 측정 데이터
- `FFTConvolver` - 실시간 FFT 컨볼루션

**구현:**
```cpp
// HRTF 데이터 로드 (128 taps, diffused set - 음악에 최적화)
mit_hrtf_get(&azimuth, &elevation, 44100, 1, hrtfL, hrtfR);

// FFTConvolver로 실시간 HRTF 컨볼루션
hrtfConvolverL.init(512, irL, taps);
hrtfConvolverR.init(512, irR, taps);
hrtfConvolverL.process(inputL, outputL, frames);
hrtfConvolverR.process(inputR, outputR, frames);
```

**UI 파라미터:**
- Intensity (0-100%): HRTF 효과 강도
- Azimuth (0-180°): 가상 음원 방향

---

## 1월 5일 작업 내역

### 1. Superpowered Pitch 기본값 설정
- 기본 피치를 **-2 semitones**로 설정
- C++ 초기화: `pitchShiftCents = -200`
- Kotlin UI 상태: `DEFAULT_PITCH_VALUE = 80`

### 2. Pitch 라우팅 시스템 구현
- `AudioEffectHandlerImpl`에 `PlaybackModeController` 주입
- AUDIO 모드: Signalsmith 직접 호출 (ExoPlayer 우회)
- VIDEO 모드: ExoPlayer PlaybackParameters 사용

### 3. Native 버퍼 오버플로우 수정 (SIGSEGV 해결)
```cpp
static const int MAX_FRAMES_TO_FEED = 4096;
static const size_t TEMP_INPUT_BUFFER_SIZE = MAX_FRAMES_TO_FEED * 2;
```

---

## 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        ExoPlayer                                │
│                    (Audio Decoding)                             │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                     HybridAudioSink                             │
│                                                                 │
│  ┌──────────────────┐        ┌──────────────────────────────┐  │
│  │   VIDEO Mode     │        │        AUDIO Mode            │  │
│  │                  │        │                              │  │
│  │  DefaultAudioSink│        │  Ring Buffer → Native C++    │  │
│  │        +         │        │                              │  │
│  │  Android Effects │        │  Signalsmith Stretch         │  │
│  │  (EQ, Reverb,    │        │  (Pitch/Tempo)               │  │
│  │   Virtualizer)   │        │                              │  │
│  └──────────────────┘        │  Signalsmith Basics          │  │
│                              │  (Chorus, Limiter, Reverb)   │  │
│                              │                              │  │
│                              │  Signalsmith DSP             │  │
│                              │  (EQ, Compressor)            │  │
│                              │                              │  │
│                              │  MIT HRTF + FFTConvolver     │  │
│                              │  (3D Virtualizer)            │  │
│                              └──────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                      AudioTrack                                 │
│                   (Audio Output)                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 파일 구조

```
audio/src/main/cpp/
├── CMakeLists.txt
├── SignalsmithAudioBridge.cpp    # 메인 DSP 처리
├── SignalsmithProcessor.cpp      # Pitch/Tempo 처리
├── SuperpoweredAudioBridge.cpp   # (레거시)
├── signalsmith/                  # Signalsmith 라이브러리
│   ├── signalsmith-stretch.h
│   ├── signalsmith-dsp/
│   └── basics/
├── mit-hrtf-lib/                 # MIT HRTF 데이터 (MIT License)
│   ├── include/mit_hrtf_lib.h
│   └── source/
│       ├── mit_hrtf_lib.c
│       ├── normal/               # 일반 HRTF 세트
│       └── diffuse/              # 음악용 HRTF 세트
└── FFTConvolver/                 # FFT 컨볼루션 (MIT License)
    ├── FFTConvolver.cpp
    ├── FFTConvolver.h
    ├── AudioFFT.cpp
    └── AudioFFT.h
```

---

## 수정된 파일 (1월 9일)

| 파일 | 변경 내용 |
|:-----|:---------|
| `audio/src/main/cpp/CMakeLists.txt` | MIT HRTF, FFTConvolver 추가 |
| `audio/src/main/cpp/SignalsmithAudioBridge.cpp` | HRTF 컨볼루션 구현, 이전 이펙트 제거 |
| `audio/src/main/java/.../SignalsmithAudioEngine.kt` | HRTF JNI 바인딩 |
| `media/src/main/java/.../AudioEffectsManager.kt` | HRTF 상태 관리 |
| `feature/convert/.../ConvertAudioEditViewModel.kt` | HRTF ViewModel |
| `feature/convert/.../VirtualizerSection.kt` | HRTF UI 컴포넌트 |
| `core/ui/src/main/res/values/strings.xml` | HRTF 문자열 리소스 |

---

## 다음 작업 (제안)

1. **디바이스 테스트** - HRTF Virtualizer 효과 확인
2. **성능 최적화** - HRTF 컨볼루션 CPU 사용량 모니터링
3. **추가 Azimuth 프리셋** - 자주 쓰는 방향 프리셋 추가 가능

### 3. DaisySP 이펙트 추가 (1월 9일 - 추가 작업)
**사용 라이브러리:**
- **DaisySP** (MIT License) - 임베디드 오디오용 DSP 라이브러리

**구현된 이펙트:**
- **Phaser**: LFO Rate, Depth, Feedback, Stages (1-8)
- **Flanger**: LFO Rate, Depth, Feedback, Delay (0.1-7ms)
- **Tremolo**: Rate, Depth, Waveform (Sine, Triangle, Saw, Ramp, Square)
- **Auto-Wah**: Wah, Mix, Level
- **Bitcrusher (Decimator)**: Bit Crush, Downsample

**수정 사항:**
- `audio/src/main/cpp/CMakeLists.txt`: DaisySP 소스 추가
- `audio/src/main/cpp/DaisySP/Source/Utility/dsp.h`: Android ARMv7 호환성 패치 (Inline Assembly 수정)
- `audio/src/main/cpp/SignalsmithAudioBridge.cpp`: JNI 바인딩 및 이펙트 체인 연결
- `media/src/main/java/.../AudioEffectsManager.kt`: StateFlow 상태 관리 추가
- `feature/convert/src/main/java/.../ConvertAudioEditScreen.kt`: UI 컴포넌트 추가
