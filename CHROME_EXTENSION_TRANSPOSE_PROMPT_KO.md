# Chrome Extension (MV3) “transpose” 부트스트랩용 단일 프롬프트 (한국어)

## 목표 (3–5개)
- YouTube 등 HTML5 비디오 사이트에서 재생 중인 오디오에 **실시간 피치/이펙트 체인**을 적용한다.
- **템포(속도)** 는 `HTMLMediaElement.playbackRate`로 처리해 영상/음성 속도를 동기화한다.
- **피치(음높이)** 는 `AudioWorklet` 기반 DSP(필요 시 WASM 포팅)로 처리해 “영상 pitch=1.0 유지” 개념을 따른다.
- MV3 제약(서비스 워커에서 AudioContext 불가 등)을 준수하면서 **MVP → v1 → v2** 로 과도한 범위 확장을 피한다.

아래 블록을 **새 크롬 확장 워크스페이스(OpenCode)** 에 그대로 복사/붙여넣기 하세요.

```markdown
당신은 Chrome Extension(MV3) 및 Web Audio/AudioWorklet 전문가 에이전트입니다.
새 워크스페이스에서 “transpose” 프로젝트를 부트스트랩(파일 생성 포함)하세요.

# 0) 절대 지켜야 할 제약
- DRM/보호된 콘텐츠를 “우회”하거나 “복호화”하는 기능을 주장/구현하지 마세요. HTML5로 재생 가능한 일반 오디오 처리만 다룹니다.
- 성능 수치(ms, CPU%, 지연 등)를 임의로 만들어 말하지 마세요.
- 범위를 통제하세요: MVP 먼저, 동작 확인 후 단계적으로 확장합니다.
- 가능하면 네트워크 설치(패키지 다운로드) 없이 진행하세요. 번들러/패키지 설치가 필요하면 반드시 먼저 사용자 동의를 얻고 진행하세요.
- MV3에서 `service worker`는 AudioContext를 오래 유지/구동하는 장소가 아닙니다(오디오 처리는 content script/page world 또는 offscreen document에서).

# 1) 먼저 할 일: 명확화 질문(최소 10개, 답변 받기 전까지 설계 확정 금지)
아래 질문에 대해 사용자의 선호/제약을 물어보고, 답변을 받은 뒤 “선택한 아키텍처(A/B)”를 확정하세요.

1) 1차 타겟 사이트는 YouTube만인가요, 아니면 “모든 HTML5 video 사이트”인가요?
2) 처리 대상은 “페이지의 주 비디오 1개”만인가요, 아니면 동시에 여러 video도 지원해야 하나요?
3) 기본 UX는 어떤가요? (팝업 슬라이더 중심 / 단축키 중심 / 둘 다)
4) 템포 범위는 어느 정도로 제한할까요? (예: 0.5–2.0 같은 범위, 과도한 값 제한 필요 여부)
5) 피치 단위는 무엇이 좋나요? (semitone, cent, ratio)
6) 피치 쉬프팅 품질 목표는? (MVP는 ‘기본 동작’ 우선 vs 초기부터 고품질 목표)
7) 이펙트 프리셋을 제공할까요? (예: “음성 강화”, “베이스 부스트”, “공간감”)
8) 파라미터 저장은 탭별(사이트별)인가요, 전역인가요? (chrome.storage 동작 정의)
9) 오디오 라우팅 정책: 원본 오디오를 mute하고 처리 오디오만 재생할까요? (특히 B안에서 중요)
10) “동기화”에 민감한가요? (영상-오디오 lip sync) 어느 정도 지연 허용 가능?
11) 개발 언어 선호: 순수 JS(빌드 없음) vs TS(빌드 필요) 중 무엇인가요?
12) 번들러 허용 여부: (허용 시) `esbuild`/`vite` 중 선호가 있나요?
13) WASM 포팅을 MVP에 포함할까요, 아니면 v1/v2로 미룰까요?
14) 단축키(예: pitch up/down, bypass) 필요하나요? 충돌 우려(YouTube 단축키) 수용 가능한가요?
15) UI는 한국어만? 영어도 필요?

답변을 받으면, 아래 2)에서 A/B 중 하나를 선택하고 “선택 이유”를 5줄 이내로 요약하세요.

# 2) 아키텍처 옵션(필수): A/B 비교 + ASCII 다이어그램 2개
## A) MediaElementAudioSourceNode 기반 WebAudio 그래프 (권장: MVP 시작점)
개요:
- content script가 페이지의 `<video>`(HTMLMediaElement)를 찾아 AudioContext를 만들고,
  `createMediaElementSource(video)`로 입력을 받아 이펙트 체인을 구성한 뒤 destination으로 출력.
- 템포는 `video.playbackRate`로(영상+음성 속도 동기화), 피치/이펙트는 Worklet에서.

장점:
- 구현 단순, 지연/동기화 관리가 비교적 쉽다(영상과 동일 컨텍스트에서 재생).
- tabCapture 권한 없이도 가능(대부분 사이트에서).

단점/리스크:
- 사이트/브라우저 정책에 따라 오디오 캡처/연결이 제한될 수 있음(사용자 제스처, CORS, autoplay 등).
- YouTube SPA에서 video 교체/재생 요소 변경 대응 필요.

ASCII 다이어그램(A):
[Popup UI] --(chrome.runtime msg)--> [Content Script]
      |                                   |
      |                          find/track <video>
      |                                   |
      v                                   v
 [storage.local]                    [AudioContext]
                                         |
                                   MediaElementSource(video)
                                         |
                                  [FX Chain + AudioWorklet]
                                         |
                                   AudioContext.destination
                                         |
                                   (소리 출력: 탭)

## B) tabCapture + offscreen document 기반 처리 (대안/확장)
개요:
- `chrome.tabCapture`로 탭 오디오(및 선택적으로 비디오)를 캡처하고, (사용자 액션으로만 시작 가능)
  offscreen document에서 AudioContext + Worklet로 처리 후,
  처리된 오디오를 extension 측에서 재생한다.
  - 주의: tabCapture가 시작되면 탭의 기존 오디오가 중단될 수 있어, offscreen에서 반드시 재출력(AudioContext.destination)해야 한다.
  - streamId는 짧은 수명/즉시 소비가 필요하므로 생성 후 바로 getUserMedia로 연결한다.
- 특정 사이트에서 A가 막힐 때의 대안이 될 수 있음.

장점:
- 페이지 DOM/오디오 정책 제약을 덜 받을 수 있음(입력은 캡처 스트림).
- “탭 전체 오디오” 처리에 가까움(단, 구현 난이도와 UX 이슈 존재).

단점/리스크:
- 동기화/지연 관리 난이도 상승(처리 오디오가 탭 밖에서 재생될 수 있음).
- 권한/UX 부담(tabCapture, offscreen) 및 사용자 제스처 필요.
- “탭 오디오를 다시 탭으로 주입”은 일반적으로 불가능/제한적이라,
  보통 “탭 mute + extension에서 재생” 형태가 된다(립싱크 이슈 가능).

ASCII 다이어그램(B):
[Popup UI] -> [Service Worker] -> create offscreen doc
                      |                 |
                      |           [Offscreen Document]
                      |                 |
                 tabCapture()       MediaStreamAudioSource(captureStream)
                      |                 |
                      v                 v
                 (tab audio)     [FX Chain + AudioWorklet] -> destination -> (extension audio out)
                                      ^
                                      |
                              (tab mute/volume policy 필요)

# 3) 필수 라우팅 정책(Tempo/Pitch)
반드시 아래 정책을 따르도록 구현/문서화하세요.

- Tempo(속도):
  - `HTMLMediaElement.playbackRate` 로 처리한다.
  - 목표: 영상+음성 속도를 동일하게 변경해 싱크를 유지한다.

- Pitch(음높이):
  - “영상 pitch=1.0 유지” 개념: 영상 프레임 타이밍은 tempo가 담당하고,
    피치는 오디오 처리 레이어(AudioWorklet DSP)가 담당한다.
  - 구현: `AudioWorkletProcessor` 기반 pitch shifter(초기에는 JS 구현, 추후 WASM 포팅).
  - MVP에서는 “기본 동작 우선”으로, 품질 고도화는 v1/v2로 단계적 진행.

# 4) 이펙트 목록(필수) + 브라우저 구현 전략(노드 vs Worklet/WASM)
아래 이펙트 이름을 UI/설계에 포함하고, 각 이펙트별 구현 후보를 제시하세요(초기엔 일부만 활성화 가능).
각 항목은 “MVP 구현 방식”과 “향후 고급 구현(WASM/Worklet)”을 구분해 적으세요.

- Chorus:
  - MVP: DelayNode(짧은 딜레이) + LFO(OscillatorNode)로 delayTime 변조 + 믹스(GainNode)
  - 고급: Worklet로 멀티탭/스테레오 확장, anti-aliasing 고려

- Limiter:
  - MVP: DynamicsCompressorNode를 limiter 세팅(높은 ratio, 빠른 attack)
  - 고급: Worklet/WASM true peak limiter(오버샘플링 등은 v2)

- Reverb:
  - MVP: ConvolverNode + (간단 IR 제공) / 또는 간이 comb/allpass(Worklet)
  - 고급: FFTConvolver(WASM) + MIT HRTF/IR 관리

- Crunch(드라이브/왜곡):
  - MVP: WaveShaperNode(소프트 클리핑 커브) + pre/post gain
  - 고급: Worklet로 오버샘플링/안티앨리어싱

- EQ:
  - MVP: BiquadFilterNode(로우/피킹/하이) 다중 밴드
  - 고급: IIR/FFT 기반 파라메트릭 EQ(WASM, iir1/sndfilter 등은 v2 후보)

- Compressor:
  - MVP: DynamicsCompressorNode
  - 고급: Worklet로 RMS/peak 모드, lookahead(v2)

- HRTF(바이노럴/공간화):
  - MVP: StereoPannerNode + ConvolverNode(간단 스테레오 IR)로 “느낌”만
  - 고급: MIT HRTF + FFTConvolver(WASM), 방향/거리 파라미터

- Phaser:
  - MVP: BiquadFilterNode(allpass 대체는 제한) + 간이 Worklet allpass 체인 권장
  - 고급: Worklet all-pass cascade + LFO

- Flanger:
  - MVP: Chorus와 유사(더 짧은 딜레이, 피드백 경로 추가)
  - 고급: Worklet로 안정적 피드백/클리핑 제어

- Tremolo:
  - MVP: GainNode에 LFO로 amplitude modulation
  - 고급: Worklet로 스테레오 위상차/shape

- Autowah:
  - MVP: Envelope follower(Worklet) + BiquadFilterNode(center freq 변조)
  - 고급: Worklet로 정교한 추적/스무딩

- Decimator(비트크러시/다운샘플):
  - MVP: Worklet에서 샘플 홀드(다운샘플) + 비트 마스킹/퀀타이즈
  - 고급: WASM 최적화, 노이즈 쉐이핑(v2)

MVP에서는 “전체 목록 노출 + 일부만 구현/활성” 전략이 가능하지만,
사용자 혼동을 피하려면 “미구현(placeholder)”를 명확히 표시하거나 MVP에서는 숨기고 v1에서 노출하세요.

# 5) Android 프로젝트의 관련 라이브러리(WASM 포팅 후보) 언급(필수)
다음 라이브러리/모듈은 ‘향후 WASM 포팅 후보’로 문서/로드맵에 포함하세요.
- Signalsmith Stretch (MIT): 고품질 time-stretch/pitch-shift 알고리즘 후보
- Signalsmith basics (MIT): DSP 기본 유틸
- DaisySP (MIT): 이펙트/필터/오실레이터 등 참고/포팅 후보
- MIT HRTF + FFTConvolver: 공간화/컨볼루션 고도화 후보
- iir1, sndfilter, RNNoise: v2 이후 옵션(노이즈 억제/필터 확장)

주의:
- MVP에선 “WASM 빌드 체인”이 부담이므로, 먼저 JS Worklet로 기능 골격을 만들고,
  v1/v2에서 Emscripten/WASM 도입을 검토하는 로드맵을 제시하세요.

# 6) 런타임 플로우 다이어그램(필수 1개)
아래와 같은 흐름을 기반으로 실제 메시징/상태 저장/오디오 구성을 설계하세요.

FLOW:
[User] -> [Popup UI] -> (runtime.sendMessage) -> [Content Script / Offscreen]
   -> [Create/Update Audio Graph] -> [AudioWorklet Processor]
   -> [Apply params (pitch/effects)] -> [Audio Output]
   -> [Persist params to chrome.storage] -> [Restore on next visit]

# 7) 구현 산출물(Deliverables) — 반드시 “파일 트리 + 핵심 파일” 생성
최종 결과물로 아래를 제공하세요(최소 요구 사항):
- 프로젝트 파일 트리(실제 생성)
- `manifest.json` (MV3)
- Popup UI: 슬라이더/토글(tempo, pitch, master gain, bypass, 최소 2개 이펙트 on/off)
- Content script:
  - YouTube/HTML5 video 탐지(단일/교체 대응: MutationObserver)
  - AudioContext 생성/재사용
  - MediaElementAudioSourceNode 연결(A안) 또는 tabCapture 연동(B안)
- Messaging:
  - popup <-> service worker(필요 시) <-> content/offscreen
  - 파라미터 변경 이벤트(스로틀/디바운스)
- AudioWorklet:
  - 최소 1개 Processor(예: pitch shifter 또는 bitcrusher)
  - 파라미터 업데이트 메시지 처리
  - sampleRate/quantum(128 frames) 고려한 안정성 코드
- Parameter persistence:
  - `chrome.storage.local` 저장/복원
  - 탭/도메인 스코프 정책을 문서화하고 구현
- Debug 가이드:
  - chrome://extensions 로드, 로그 확인 위치, 일반적인 트러블슈팅

선호 구현(네트워크 설치 없이):
- 순수 JS + ESM 모듈로 구성(빌드 단계 없이).
- TS/번들러가 꼭 필요하면, 먼저 사용자 동의 받고 도입(간단한 번들러 1개만).

# 8) 단계별 실행 계획(에이전트가 그대로 수행할 것)
답변을 받은 뒤, 아래 순서로 작업하세요.

Step 1) 아키텍처 선택(A/B) 확정 + 이유 요약
Step 2) 최소 권한/최소 파일로 MV3 스캐폴딩 생성
Step 3) manifest 작성: permissions/host_permissions, action(popup), content_scripts, background(type: module) 등
Step 4) popup UI 구현: tempo/pitch/master/bypass + 상태 표시(connected video 여부)
Step 5) 메시징 레이어 구현: UI 변경 -> content/offscreen에 적용
Step 6) A안 구현:
  - video 탐지(YouTube SPA 포함), 연결/해제 처리
  - AudioContext 생성은 사용자 제스처 이후로 지연(autoplay 정책 회피)
  - graph: video -> (source) -> (fx) -> destination
Step 7) AudioWorklet 구현:
  - `audioWorklet.addModule(...)`
  - Processor는 최소 “피치 쉬프터(간단 버전)” 또는 “Decimator” 중 하나를 MVP로 구현
  - 파라미터 메시지/스무딩 구현
Step 8) Tempo 정책 구현:
  - popup에서 tempo 변경 시 `video.playbackRate` 설정
  - pitch는 worklet 파라미터로만 변경(tempo와 분리)
Step 9) 파라미터 저장/복원:
  - chrome.storage.local에 저장
  - 탭 재진입/새로고침 시 복원
Step 10) 수동 테스트 체크리스트 수행(아래 10번) + 발견 이슈 수정
Step 11) README(또는 문서)로 빌드/실행/디버깅/제약 정리

# 9) 빌드/실행 방법(Chrome 확장)
반드시 문서/README에 아래를 포함하세요.

- 로드:
  1) Chrome 열기 → `chrome://extensions`
  2) “개발자 모드” ON
  3) “압축해제된 확장 프로그램을 로드” → 프로젝트 폴더 선택
- 권한:
  - A안: 기본적으로 `storage`, `scripting`(필요 시), 대상 사이트 `host_permissions`
  - B안: 추가로 `tabCapture`, `offscreen` 필요 가능
- 디버깅:
  - service worker: 확장 상세 → “서비스 워커” 검사
  - content script: 대상 탭 DevTools 콘솔
  - offscreen: 확장 상세에서 해당 문서/페이지 검사(생성 시)
- 사용자 제스처:
  - AudioContext resume은 사용자 클릭/키 입력 후 수행(팝업에서 “Enable” 버튼 제공 권장)

# 10) 수동 테스트 체크리스트(상세)
다음 항목을 체크박스 형태로 제공하고, MVP 기준으로 전부 통과하도록 구현/수정하세요.

기본 동작
- [ ] YouTube 영상 재생 중 팝업을 열면 “연결됨(connected)” 상태가 표시된다
- [ ] Enable/Start 클릭 후 AudioContext가 정상 resume 된다(autoplay 정책 회피)
- [ ] Tempo 슬라이더 변경 시 `video.playbackRate`가 즉시 반영되고 영상/음성이 함께 빨라/느려진다
- [ ] Pitch 슬라이더 변경 시 재생 속도(tempo)는 유지되고 음높이만 변한다(품질은 MVP 수준 허용)
- [ ] Bypass 토글로 이펙트 체인이 꺼졌다 켜진다

YouTube SPA/DOM 변화
- [ ] 같은 탭에서 다른 영상으로 넘어가도(추천 영상 클릭) 새 video를 재탐지/재연결한다
- [ ] 광고/미니플레이어/시어터 모드 전환 시에도 비정상 종료 없이 유지/복구한다
- [ ] video element가 교체되면 이전 노드 연결을 정리(disconnect)한다

다중 비디오/예외
- [ ] 페이지에 video가 2개 이상일 때 “대상 선택 정책”이 문서화되어 있고, 최소한 오작동하지 않는다
- [ ] video를 찾지 못한 경우 UI에 원인을 표시하고(예: “video 없음”), 크래시하지 않는다

오디오 품질/안정성
- [ ] 샘플레이트(대개 48k)에서 정상 동작한다(AudioContext.sampleRate 사용)
- [ ] AudioWorklet quantum(128 frames)에서 파라미터 변경 시 잡음/폭주가 없도록 스무딩한다
- [ ] CPU 과부하 시 안전장치(품질 낮추기/이펙트 비활성 안내 등) 계획을 문서에 적는다(수치 추정 금지)

상태 저장
- [ ] 새로고침 후에도 마지막 파라미터가 복원된다(정책: 도메인별/전역 중 선택 반영)
- [ ] 다른 탭/다른 사이트에서 설정이 의도대로 분리/공유된다

# 11) 알려진 함정(필수로 문서화 + 코드에서 방어)
아래 이슈를 “Known pitfalls” 섹션으로 문서화하고, 가능한 방어 코드를 넣으세요.

- Autoplay / User gesture:
  - AudioContext 생성/resume은 사용자 제스처 필요. 팝업 Enable 버튼/명확한 안내 필요.
- CORS/보안 정책:
  - 일부 사이트는 미디어 소스/오디오 그래프 연결이 제한될 수 있다.
- YouTube SPA:
  - video element가 교체된다. MutationObserver + 주기적 재검출 필요.
- Multiple videos:
  - 대상 video 선택(가장 큰 면적/현재 재생 중 등) 정책 필요.
- Latency:
  - Worklet/컨볼루션/FFT는 지연을 유발. MVP는 가벼운 체인부터.
- CPU:
  - 리버브/피치 쉬프팅은 비용이 크다. 단계별 기능 플래그/품질 옵션 필요.
- Sample rate 48k:
  - YouTube는 보통 48k. 알고리즘은 sampleRate를 가정하지 말고 파라미터화.
- AudioWorklet quantum:
  - 128-frame 고정. 내부 버퍼링/그라뉼러 설계 시 고려.
- MV3 service worker 한계:
  - 장시간 오디오 처리는 service worker에 두지 말 것. (content/offscreen에서 AudioContext 유지)
- tabCapture 제약:
  - 캡처는 사용자 invocation(확장 버튼 클릭 등) 이후에만 시작 가능. popup 기반 capture는 popup이 닫히면 끊길 수 있으니 offscreen 문서 기반을 기본으로 고려.
  - tabCapture는 탭 오디오를 캡처로 전환하면서 원래 탭 오디오가 중단될 수 있다. 반드시 offscreen AudioContext에서 재생(재출력) 경로를 제공하고, A/V sync 지연을 감안한다.
- offscreen document 제약:
  - offscreen에서 AudioContext/Worklet은 가능하지만, 제어 플레인은 `chrome.runtime` 메시징 중심으로 설계해야 한다.
- ScriptProcessorNode 사용 금지:
  - deprecated. 실시간 DSP는 AudioWorklet로만 구현한다.

# 12) MVP 스코프 + 단계적 로드맵(필수)
## MVP (가장 작은 성공)
- 아키텍처: A안 우선
- 기능:
  - Tempo: playbackRate 슬라이더
  - Pitch: Worklet 기반 기본 pitch shift(품질은 MVP 수준)
  - FX: 최소 2개(예: EQ + Crunch 또는 Tremolo), Bypass, Master gain
  - YouTube SPA 대응(재연결)
  - 파라미터 저장/복원

## v1 (품질/안정성)
- Pitch 알고리즘 개선(그라뉼러/위상 보코더 개선 또는 Signalsmith Stretch 포팅 검토)
- FX 확장(Chorus/Flanger/Phaser/Reverb/Limiter 등) + 프리셋
- 단축키/탭별 프로필/사이트별 기본값
- 오류/상태 텔레메트리(로컬 로그 수준)

## v2 (고급 DSP/WASM)
- Signalsmith Stretch/basics, DaisySP, FFTConvolver(HRTF) WASM 포팅 파이프라인
- 고급 리미터/오버샘플링, true peak 등(수치 약속 금지)
- RNNoise 등 노이즈 억제 옵션(상황/권한/성능 고려)

# 13) 산출물 제출 형식(최종 응답에 포함)
작업 완료 시, 다음을 한 번에 제시하세요.
- 생성된 파일 트리
- 실행 방법(Load unpacked)
- 주요 파일별 역할 요약
- 수동 테스트 체크리스트 결과(통과/미통과 및 미통과 시 남은 TODO)
```
