# AudioProcessor 전환 변경사항 보고서 (Korean)

> 브랜치: `feature/audioprocessor-injection`
>
> 빌드 검증: `./gradlew :app:assembleDebug` 성공

---

## 1) TL;DR

이번 변경의 핵심은 **오디오 DSP(피치/이펙트)를 ExoPlayer(=Media3) 오디오 파이프라인 내부의 `AudioProcessor`로 이동**시키는 것이다.

- **Tempo(재생 속도)**: ExoPlayer 표준 `PlaybackParameters.speed` 사용 (영상+오디오 전체 속도)
- **Pitch(피치)**: `SignalsmithAudioProcessor`(JNI→native)에서만 처리
- **ExoPlayer pitch**: 항상 `1.0f`로 유지 (사용하지 않음)
- **Custom DSP effects**: `SignalsmithAudioProcessor`(JNI→`SignalsmithProcessor.cpp`)로 일원화

---

## 2) 배경(왜 바꿨나)

레거시 구조(`HybridAudioSink` + 별도 엔진/AudioTrack 기반)는 다음 문제를 야기하기 쉬웠다.

- ExoPlayer의 포지션/동기화(Seek, Pause/Resume, A/V sync)와 **외부 오디오 엔진(별도 AudioTrack)**이 동시에 존재
- 모드 전환 시점/싱크 구성 타이밍에 따라 **불필요한 엔진 초기화(또는 오동작)** 가능
- DSP가 파이프라인 밖에 있어, ExoPlayer가 제공하는 표준 동작과 “따로 놀기” 쉬움

이번 전환의 목표는 **ExoPlayer가 동기화/포지션을 책임지고**, 우리는 **PCM 단계에서 피치/이펙트만 안정적으로 처리**하도록 역할을 정리하는 것이다.

---

## 3) 이전 구조(legacy) vs 현재 구조(변경 후)

### 3.1 이전(legacy) 구조

레거시 경로는 ExoPlayer 오디오 싱크를 교체/랩핑해서 PCM을 가로채고, 네이티브 DSP 엔진이 **별도 AudioTrack**으로 출력하는 구조였다.

#### [이전 구조 다이어그램]

```
Decoder
  |
  v
HybridRenderersFactory
  |
  v
HybridAudioSink
  |------------------------------|
  |                              |
  | (VIDEO)                      | (AUDIO / VIDEO_WITH_DSP)
  v                              v
DefaultAudioSink                 SuperpoweredBridge.pushPcm(...)
  |                              |
  v                              v
AudioTrack(ExoPlayer)            SignalsmithBridgeImpl
                                 |
                                 v
                         SignalsmithAudioEngine (별도 AudioTrack + render thread)
                                 |
                                 v
                         Native ring buffer + DSP (SignalsmithAudioBridge.cpp)
                                 |
                                 v
                         AudioTrack(legacy)
```

- 모드(PlaybackMode)에 따라 `HybridAudioSink.handleBuffer(...)`가 **ExoPlayer 기본 sink로 보낼지** / **bridge로 PCM을 밀어 넣을지**를 결정
- 레거시 엔진은 자체 render thread에서 네이티브 DSP 결과를 읽어 별도 AudioTrack으로 출력

---

### 3.2 현재 구조(AudioProcessor 주입)

이제 ExoPlayer는 `ProcessorRenderersFactory`로 구성되고, `SignalsmithAudioProcessor`가 `DefaultAudioSink`의 AudioProcessor 체인에 주입된다.

#### [현재 구조 다이어그램]

```
Decoder
  |
  v
ProcessorRenderersFactory
  |
  v
DefaultAudioSink
  |
  v
DefaultAudioProcessorChain
  |
  v
SignalsmithAudioProcessor (Kotlin)
  |
  v (JNI)
SignalsmithProcessor.cpp (native DSP chain)
  |
  v
DefaultAudioSink -> AudioTrack(ExoPlayer)
```

- ExoPlayer가 **seek/pause/position/A-V sync**를 그대로 관리
- 우리는 AudioProcessor 단계에서 **pitch + custom effects**만 담당
- Tempo는 ExoPlayer speed로 처리 (native time-stretch/tempo 처리 없음)

---

## 4) 라우팅 정책(Tempo / Pitch / Effects)

### 4.1 Tempo(재생 속도)

- 처리 주체: **ExoPlayer**
- 적용 API: `PlaybackParameters.speed`
- 구현 규칙: `exoPlayer.playbackParameters = PlaybackParameters(speed, 1.0f)`

즉, **속도는 ExoPlayer가 처리**하며 영상+오디오 전체에 반영된다.

### 4.2 Pitch(피치)

- 처리 주체: **SignalsmithAudioProcessor → native**
- ExoPlayer pitch: 항상 `1.0f`로 고정
- 적용 API: `SignalsmithAudioProcessor.setPitchSemitones(semitones)`

즉, **피치만 DSP에서 조정**하고 속도는 ExoPlayer가 조정한다.

### 4.3 Custom DSP effects(이펙트)

- 처리 주체: **SignalsmithAudioProcessor → JNI → `SignalsmithProcessor.cpp`**
- 포함 이펙트:
  - Signalsmith basics: Chorus / Limiter / Reverb / Crunch
  - EQ / Compressor
  - MIT HRTF
  - DaisySP: Phaser / Flanger / Tremolo / Autowah / Decimator

---

## 5) 이번 변경에서 구현/수정된 것들(파일/클래스 기준)

### 5.1 ExoPlayer 구성 변경

- `media/src/main/java/com/example/media/di/MediaModule.kt`
  - `ExoPlayer.Builder(context, hybridRenderersFactory)` → `ExoPlayer.Builder(context, processorRenderersFactory)`
  - 즉, 기본 재생 파이프라인이 AudioProcessor 주입 방식으로 전환

### 5.2 Tempo/Pitch 제어 방식 변경

- `media/src/main/java/com/example/media/audio_effect/AudioEffectHandlerImpl.kt`
  - Tempo: ExoPlayer speed만 변경 (pitch는 1.0 유지)
  - Pitch: `SignalsmithAudioProcessor.setPitchSemitones(...)`만 호출
  - 기존처럼 모드별 분기(PlaybackMode)에 따라 라우팅하지 않고, 정책을 고정

### 5.3 AudioProcessor(Kotlin) 확장

- `media/src/main/java/com/example/media/audio/SignalsmithAudioProcessor.kt`
  - 전체 DSP 이펙트 셋에 대한 setter 및 JNI 바인딩 추가
  - non-direct `ByteBuffer` 입력 시 direct scratch buffer로 복사 후 native 처리
  - `configure(...)` 시점에 현재 저장된 파라미터들을 native에 한 번에 주입(초기 상태 동기화)

### 5.4 Native DSP 체인(AudioProcessor용) 구축

- `audio/src/main/cpp/SignalsmithProcessor.cpp`
  - AudioProcessor용 native 체인에 전체 이펙트 포함
  - **Tempo/time-stretch는 구현하지 않음**
    - ExoPlayer speed가 담당하도록 프레임 수가 안정적으로 유지되도록 설계
  - 모든 이펙트가 꺼져 있고 pitch도 0이면 빠른 bypass(복사) 경로로 처리

### 5.5 AudioEffectsManager 대상 변경

- `media/src/main/java/com/example/media/manager/AudioEffectsManager.kt`
  - advanced DSP 제어 대상이 `SignalsmithAudioEngine` → `SignalsmithAudioProcessor`로 변경
  - 이펙트 토글/파라미터 변경이 곧바로 AudioProcessor/native로 전달되는 구조

### 5.6 레거시 엔진 조기 초기화 방지

- `media/src/main/java/com/example/media/audio/HybridAudioSink.kt`
  - `isConfigured` 가드를 추가해서, sink 구성 전 모드 변경 등으로 레거시 엔진이 초기화되는 상황을 방지

---

## 6) 실제 런타임 흐름(어떤 경로로 동작하나)

### 6.1 Tempo / Pitch 경로

```
UI(슬라이더/버튼)
  |
  v
AudioEffectsManager (pitch/tempo는 MediaSession 커맨드로 전달)
  |
  v
CustomMediaSessionCallback
  |
  v
AudioEffectHandlerImpl
  |--- Tempo: exoPlayer.playbackParameters = PlaybackParameters(speed, 1.0f)
  |
  `--- Pitch: signalsmithProcessor.setPitchSemitones(semitones)
```

### 6.2 이펙트(Chorus/EQ/HRTF/DaisySP 등) 경로

```
UI(이펙트 토글/파라미터)
  |
  v
AudioEffectsManager
  |
  v
SignalsmithAudioProcessor.setXxx(...)
  |
  v (JNI)
SignalsmithProcessor.cpp
  |
  v
AudioProcessor 출력 -> DefaultAudioSink -> AudioTrack
```

---

## 7) 어떻게 테스트하면 되나(권장 체크리스트)

### 7.1 빌드

- `./gradlew :app:assembleDebug`

### 7.2 기능 테스트 체크리스트

#### A. 기본 재생

- 영상+오디오가 정상 재생되는지
- pause/resume 시 오디오가 끊기거나 무음/잡음이 발생하지 않는지
- seek 시 크래시/무음/심각한 싱크 문제 없는지

#### B. Tempo 테스트(ExoPlayer speed)

- Tempo 값을 변경했을 때 영상+오디오 속도가 함께 변하는지
- Tempo 변경 시 pitch는 변하지 않아야 함(ExoPlayer pitch=1.0 유지)
- tempo 변경 후 seek/pause/resume에도 설정이 유지되는지

#### C. Pitch 테스트(AudioProcessor)

- Pitch 값을 변경했을 때 음 높이만 변하고, 속도는 유지되는지
- pitch 변경 후 seek/pause/resume에서도 동작이 안정적인지

#### D. 이펙트 테스트(전체)

- Chorus / Limiter / Reverb / Crunch / EQ / Compressor / HRTF / Phaser / Flanger / Tremolo / Autowah / Decimator
  - 각 이펙트 enable/disable 반영 여부
  - 파라미터 변경 반영 여부
  - 여러 이펙트 동시 사용 시 크래시/한쪽 채널 무음/과도한 왜곡 등이 없는지

#### E. 모드 전환(Regression)

- PlaybackMode(영상/오디오/Video with DSP) 전환 시 앱이 안정적인지
- 특히 레거시 엔진이 의도치 않게 초기화되어 “이중 오디오”가 나지 않는지

### 7.3 로그로 확인(추천 태그)

- Kotlin(AudioProcessor): `SignalsmithProcessor`
- Native(AudioProcessor DSP): `SignalsmithProc`
- Legacy 엔진: `SignalsmithEngine`

---

## 8) 남아있는 레거시/주의사항/TODO

### 8.1 PlaybackModeController의 현재 의미

- **PlaybackMode는 여전히 일부 로직(예: 스트림 선택)에 사용될 수 있음**
  - 예: `media/src/main/java/com/example/media/manager/MediaPlaybackManager.kt`에서 `currentMode`를 보고 Auto 품질일 때 video/audio URL 선택 분기
- 하지만 오디오 DSP 파이프라인 자체는 이제 AudioProcessor 경로가 기본이므로,
  - 모드 전환이 “DSP 경로를 바꾸는 스위치”가 아니라
  - “어떤 소스를 선택하느냐”/“UI 상태” 등에 더 가까워질 수 있음

### 8.2 레거시 엔진/브리지 잔존

- `SignalsmithAudioEngine`, `SignalsmithAudioBridge.cpp` 등 레거시 경로는 아직 코드/빌드에 남아있다.
- 안정화 후 다음 단계로 정리(삭제/축소)하는 것이 자연스러운 후속 작업이다.

### 8.3 빌드 산출물(git 주의)

- `audio/build/...` 아래 산출물이 `git status`에 보일 수 있으니 커밋에 포함하지 말 것
- 필요 시 로컬 정리: `git restore audio/build` (추가로 untracked 제거는 `git clean -fd audio/build`)

---

## 9) 한 줄 결론

ExoPlayer는 `ProcessorRenderersFactory` 기반으로 전환되었고, **Tempo는 ExoPlayer speed**, **Pitch/이펙트는 AudioProcessor(native DSP 체인)**로 분리되어 보다 표준적인 동기화/포지션 관리 흐름 위에서 동작하도록 변경되었다.
