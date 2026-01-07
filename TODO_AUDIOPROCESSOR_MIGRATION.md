# AudioProcessor 주입 방식으로 전환 작업 계획서

> **브랜치**: `feature/audioprocessor-injection`
> **기준 브랜치**: signalsmith-stretch
> **작성일**: 2025-01-07
> **상태**: 구현 완료, 테스트 필요

---

## 진행 상황

| Phase | 상태 | 설명 |
|:------|:-----|:-----|
| Phase 1 | ✅ 완료 | `SignalsmithAudioProcessor.kt` 구현 |
| Phase 2 | ✅ 완료 | `SignalsmithProcessor.cpp` Native 코드 구현 |
| Phase 3 | ✅ 완료 | `ProcessorRenderersFactory.kt`, DI 모듈 수정 |
| Phase 4 | ⏸️ 대기 | 기존 코드 정리 (테스트 후 진행) |
| 테스트 | ❌ 미완료 | Android 빌드 및 기능 테스트 필요 |

### 생성된 파일

```
media/src/main/java/com/example/media/audio/
├── SignalsmithAudioProcessor.kt   ← AudioProcessor 구현
└── ProcessorRenderersFactory.kt   ← AudioProcessor 주입용 팩토리

audio/src/main/cpp/
└── SignalsmithProcessor.cpp       ← 동기식 Native DSP 처리
```

### 수정된 파일

```
audio/src/main/cpp/CMakeLists.txt          ← SignalsmithProcessor.cpp 추가
media/src/main/java/com/example/media/di/MediaModule.kt  ← DI 설정 추가
media/src/main/java/com/example/media/audio_effect/AudioEffectHandlerImpl.kt  ← 프로세서 연동
```

### 다음 단계

1. Android Studio에서 빌드 테스트
2. 기능 테스트 (Pitch/Tempo 변경, Seek, 일시정지)
3. 테스트 통과 시 기존 HybridAudioSink 방식 제거

---

## 1. 배경 및 목적

### 1.1 현재 구조 (AudioSink 교체 방식)

```
Decoder → HybridAudioSink → Ring Buffer (C++) → Native DSP → AudioTrack
                ↑
         AudioSink 전체 교체
```

**문제점**:
- Position 계산을 직접 구현해야 함 (복잡한 `isSeeking`, `firstPtsUs` 관리)
- 일시정지 후 Position 점프 버그 (Wall Clock 기반의 한계)
- Seek 동기화 문제
- 비디오-오디오 동기화가 어려움

### 1.2 목표 구조 (AudioProcessor 주입 방식)

```
Decoder → DefaultAudioProcessorChain → DefaultAudioSink → AudioTrack
                    ↑
          SignalsmithAudioProcessor (새로 구현)
```

**기대 효과**:
- ExoPlayer가 Position, 동기화 자동 관리
- Seek, 일시정지 등 모든 상태 관리가 ExoPlayer에 위임
- 코드 단순화 (Ring Buffer, Position 계산 제거 가능)

---

## 2. Media3 AudioProcessor 구조 이해

### 2.1 AudioProcessor 인터페이스

```kotlin
interface AudioProcessor {
    // 1. 포맷 설정 - 입력 포맷을 받고 출력 포맷 반환
    fun configure(inputAudioFormat: AudioFormat): AudioFormat
    
    // 2. 활성화 여부 (pitch/tempo가 1.0이 아니면 true)
    fun isActive(): Boolean
    
    // 3. PCM 데이터 큐잉
    fun queueInput(inputBuffer: ByteBuffer)
    
    // 4. 처리된 데이터 가져오기
    fun getOutput(): ByteBuffer
    
    // 5. 스트림 종료 신호
    fun queueEndOfStream()
    fun isEnded(): Boolean
    
    // 6. 상태 초기화
    fun flush()
    fun reset()
}
```

### 2.2 DefaultAudioSink에 주입하는 방법

```kotlin
// 방법 1: DefaultAudioSink.Builder 사용
val audioProcessors = arrayOf(
    SignalsmithAudioProcessor(),  // 커스텀 프로세서
)

val audioSink = DefaultAudioSink.Builder(context)
    .setAudioProcessors(audioProcessors)
    .build()

// 방법 2: DefaultRenderersFactory 오버라이드
class CustomRenderersFactory(context: Context) : DefaultRenderersFactory(context) {
    override fun buildAudioProcessors(): Array<AudioProcessor> {
        return arrayOf(SignalsmithAudioProcessor())
    }
}
```

---

## 3. 구현 계획

### 3.1 새로 만들 파일

| 파일 | 설명 |
|:-----|:-----|
| `SignalsmithAudioProcessor.kt` | AudioProcessor 인터페이스 구현 |
| `SignalsmithProcessorBridge.kt` | JNI 브릿지 (기존 것 재활용 가능) |
| `SignalsmithProcessor.cpp` | Native DSP 처리 (기존 것 수정) |

### 3.2 삭제/수정할 파일

| 파일 | 변경 |
|:-----|:-----|
| `HybridAudioSink.kt` | 삭제 또는 단순화 |
| `SignalsmithBridgeImpl.kt` | Position 관련 코드 제거 |
| `SignalsmithAudioBridge.cpp` | Ring Buffer 제거, 동기식 처리로 변경 |
| `HybridRenderersFactory.kt` | AudioProcessor 주입 방식으로 변경 |

### 3.3 단계별 구현

#### Phase 1: SignalsmithAudioProcessor 기본 구현

```kotlin
@UnstableApi
class SignalsmithAudioProcessor : AudioProcessor {
    
    private var inputAudioFormat = AudioProcessor.AudioFormat.NOT_SET
    private var outputAudioFormat = AudioProcessor.AudioFormat.NOT_SET
    
    private var pitchSemitones = 0f
    private var tempoRate = 1.0f
    
    private var inputBuffer = AudioProcessor.EMPTY_BUFFER
    private var outputBuffer = AudioProcessor.EMPTY_BUFFER
    private var inputEnded = false
    
    // Native 엔진 참조
    private var nativeHandle: Long = 0
    
    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        this.inputAudioFormat = inputAudioFormat
        
        // Signalsmith는 출력 샘플레이트를 변경하지 않음
        this.outputAudioFormat = inputAudioFormat
        
        // Native 초기화
        nativeHandle = nativeInit(
            inputAudioFormat.sampleRate,
            inputAudioFormat.channelCount
        )
        
        return outputAudioFormat
    }
    
    override fun isActive(): Boolean {
        // pitch나 tempo가 기본값이 아니면 활성화
        return pitchSemitones != 0f || tempoRate != 1.0f
    }
    
    override fun queueInput(inputBuffer: ByteBuffer) {
        // Native로 PCM 전달하고 처리된 결과 받기
        this.outputBuffer = nativeProcess(nativeHandle, inputBuffer)
    }
    
    override fun getOutput(): ByteBuffer {
        val output = outputBuffer
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        return output
    }
    
    // ... 나머지 구현
    
    // JNI 메서드
    private external fun nativeInit(sampleRate: Int, channels: Int): Long
    private external fun nativeProcess(handle: Long, input: ByteBuffer): ByteBuffer
    private external fun nativeSetPitch(handle: Long, semitones: Float)
    private external fun nativeSetTempo(handle: Long, rate: Float)
    private external fun nativeFlush(handle: Long)
    private external fun nativeRelease(handle: Long)
}
```

#### Phase 2: Native 코드 수정

```cpp
// SignalsmithProcessor.cpp - 동기식 처리

class SignalsmithProcessor {
public:
    SignalsmithProcessor(int sampleRate, int channels) {
        stretch.presetDefault(channels, sampleRate);
    }
    
    // 동기식 처리 - Ring Buffer 없이 직접 변환
    void process(const short* input, int inputFrames, 
                 short* output, int outputFrames) {
        // 1. short → float 변환
        // 2. Signalsmith 처리
        // 3. float → short 변환
    }
    
    void setPitch(float semitones) {
        stretch.setTransposeSemitones(semitones);
    }
    
    void setTempo(float rate) {
        currentTempo = rate;
    }
    
private:
    signalsmith::stretch::SignalsmithStretch<float> stretch;
    float currentTempo = 1.0f;
};
```

#### Phase 3: ExoPlayer 통합

```kotlin
// CustomRenderersFactory.kt
class CustomRenderersFactory(
    context: Context,
    private val signalsmithProcessor: SignalsmithAudioProcessor
) : DefaultRenderersFactory(context) {
    
    init {
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_OFF)
    }
    
    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ): AudioSink {
        return DefaultAudioSink.Builder(context)
            .setAudioProcessors(arrayOf(signalsmithProcessor))
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
            .build()
    }
}
```

#### Phase 4: 기존 코드 정리

- `HybridAudioSink` 삭제
- `PlaybackMode` 단순화 (VIDEO/AUDIO 구분 불필요해질 수 있음)
- Position 관련 코드 제거 (ExoPlayer가 관리)

---

## 4. 검증 항목

### 4.1 기능 테스트

| 테스트 | 설명 |
|:-------|:-----|
| Pitch 변경 | -24 ~ +24 반음 범위 |
| Tempo 변경 | 0.5x ~ 2.0x 범위 |
| Seek | 빠른 연속 Seek에서 Position 정확성 |
| 일시정지/재개 | Position이 점프하지 않는지 |
| 비디오 동기화 | VIDEO_WITH_DSP 모드에서 립싱크 |

### 4.2 성능 테스트

| 항목 | 현재 (AudioSink 교체) | 목표 (AudioProcessor) |
|:-----|:---------------------|:---------------------|
| 지연시간 | ~20ms | 측정 필요 |
| CPU 사용량 | 측정 필요 | 동등 이하 |
| 메모리 사용량 | Ring Buffer 포함 | 감소 예상 |

---

## 5. 리스크 및 대안

### 5.1 잠재적 문제

1. **지연시간 증가**: DefaultAudioSink의 버퍼링이 추가됨
2. **Tempo 처리**: AudioProcessor는 입출력 샘플 수가 다르면 복잡해짐
3. **실시간 파라미터 변경**: 재생 중 pitch/tempo 변경 시 글리치 가능성

### 5.2 Tempo 처리 전략

Tempo 변경 시 입력 프레임 수 ≠ 출력 프레임 수 문제:

```
입력: 1024 frames, tempo = 0.5x
출력: 2048 frames (2배 늘어남)
```

**해결 방안**:
- 내부 버퍼링으로 입출력 프레임 수 맞추기
- `getOutput()`을 여러 번 호출하여 분할 출력

### 5.3 실패 시 롤백 계획

AudioProcessor 방식이 성능/품질 문제가 있으면:
- 현재 AudioSink 교체 방식 유지
- Position 버그만 별도로 수정

---

## 6. 작업 명령어

```bash
# 1. 새 브랜치 생성
git checkout -b feature/audioprocessor-injection

# 2. 작업 시작 전 확인
cat TODO_AUDIOPROCESSOR_MIGRATION.md

# 3. 구현 순서
# Phase 1 → Phase 2 → Phase 3 → Phase 4 → 테스트
```

---

## 7. 참고 자료

- [Media3 AudioProcessor 소스코드](https://github.com/androidx/media/blob/main/libraries/exoplayer/src/main/java/androidx/media3/exoplayer/audio/AudioProcessor.java)
- [SonicAudioProcessor 구현 예시](https://github.com/androidx/media/blob/main/libraries/exoplayer/src/main/java/androidx/media3/exoplayer/audio/SonicAudioProcessor.java)
- [DefaultAudioSink.Builder](https://github.com/androidx/media/blob/main/libraries/exoplayer/src/main/java/androidx/media3/exoplayer/audio/DefaultAudioSink.java)

---

*이 문서를 참고하여 `feature/audioprocessor-injection` 브랜치에서 작업을 진행하세요.*
