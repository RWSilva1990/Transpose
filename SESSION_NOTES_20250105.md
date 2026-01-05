# 세션 기록 - 2025년 1월 5일

## 오늘 완료한 작업

### 1. Superpowered Pitch 기본값 설정
- 기본 피치를 **-2 semitones**로 설정
- C++ 초기화: `pitchShiftCents = -200`
- Kotlin UI 상태: `DEFAULT_PITCH_VALUE = 80`

### 2. Pitch 라우팅 시스템 구현
- `AudioEffectHandlerImpl`에 `PlaybackModeController` 주입
- AUDIO 모드: Superpowered 직접 호출 (ExoPlayer 우회)
- VIDEO 모드: ExoPlayer PlaybackParameters 사용
- `HybridAudioSink.setPlaybackParameters()`에서 AUDIO 모드 시 무시

### 3. Seek 후 Position 오류 수정
- `flush()` 시 `streamOffsetUs = -1`로 리셋
- 새 seek 위치에서 offset 재감지

### 4. Native 버퍼 오버플로우 수정 (SIGSEGV 크래시 해결)
```cpp
// 수정 전
tempInputBuffer = new float[outBufferSize * 2 * 8];  // 4096 floats

// 수정 후
static const int MAX_FRAMES_TO_FEED = 4096;
static const size_t TEMP_INPUT_BUFFER_SIZE = MAX_FRAMES_TO_FEED * 2;  // 8192 floats
tempInputBuffer = new float[TEMP_INPUT_BUFFER_SIZE];
```

### 5. 포트폴리오 문서 작성
- 파일: `SUPERPOWERED_INTEGRATION.md`
- ExoPlayer 파이프라인 설명
- Superpowered 통합 아키텍처
- 기술적 챌린지 및 해결 과정

---

## 내일 할 작업: Signalsmith Stretch + AudioTrack 구현

### 목표
Superpowered 대신 오픈소스 라이브러리로 대체:
- **DSP**: Signalsmith Stretch (MIT 라이선스)
- **출력**: AudioTrack 또는 Oboe

### 아키텍처
```
ExoPlayer (디코딩) → Ring Buffer → Signalsmith Stretch → AudioTrack → Speaker
```

### 참고 자료
- Signalsmith Stretch: https://signalsmith-audio.co.uk/code/stretch
- Oboe: https://github.com/google/oboe
- Chrome Extension에서 Signalsmith로 실시간 pitch 변경 구현 사례 있음

### 구현 순서 (제안)
1. Signalsmith Stretch 라이브러리 추가 (header-only)
2. AudioTrack 또는 Oboe 출력 구현
3. 기존 Superpowered 코드를 Signalsmith로 교체
4. 성능 테스트

### 핵심 코드 위치
- `audio/src/main/cpp/SuperpoweredAudioBridge.cpp` - Native DSP
- `media/src/main/java/com/example/media/audio/HybridAudioSink.kt` - AudioSink
- `media/src/main/java/com/example/media/audio/SuperpoweredBridgeImpl.kt` - Bridge

---

## 수정된 파일 목록

| 파일 | 변경 내용 |
|:-----|:---------|
| `audio/src/main/cpp/SuperpoweredAudioBridge.cpp` | 버퍼 크기 수정, 기본 피치 설정 |
| `media/.../AudioEffectHandlerImpl.kt` | PlaybackModeController 주입, 모드별 라우팅 |
| `media/.../PlaybackModeController.kt` | 기본 피치 적용, semitonesToRatio 함수 |
| `media/.../HybridAudioSink.kt` | AUDIO 모드에서 ExoPlayer pitch 무시 |
| `media/.../SuperpoweredBridgeImpl.kt` | flush() 시 offset 리셋 |
| `media/.../AudioEffectsManager.kt` | DEFAULT_PITCH_VALUE = 80 |
| `media/src/main/java/com/example/media/di/MediaModule.kt` | DI 업데이트 |

---

## 세션 ID
이 대화를 다시 불러오려면 opencode 세션 기능 사용 가능
