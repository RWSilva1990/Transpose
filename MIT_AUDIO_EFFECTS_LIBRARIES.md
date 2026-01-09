# MIT 라이선스 실시간 오디오 이펙트 C++ 라이브러리

> Pitch Shifting / Time Stretching 제외 (이미 Signalsmith Stretch로 구현됨)

---

## 1. 종합 DSP 프레임워크

| 라이브러리 | GitHub | 라이선스 | Header-only | 주요 기능 | 실시간 | Stars |
|:-----------|:-------|:---------|:------------|:----------|:-------|:------|
| **Signalsmith DSP** | [Signalsmith-Audio/dsp](https://github.com/Signalsmith-Audio/dsp) | MIT | ✅ | FFT, Filters, Delay, Envelopes, Spectral | ✅ | 249 |
| **sndfilter** | [velipso/sndfilter](https://github.com/velipso/sndfilter) | 0BSD | ❌ | Reverb, Compressor, Biquad (LP/HP/BP/Notch/Peak/Shelf) | ✅ | 482 |
| **Gimmel** | [Mahdi03/Gimmel](https://github.com/Mahdi03/Gimmel) | MIT | ✅ | Delay, Reverb, Chorus, Flanger, Phaser, Distortion | ✅ | 신규 |
| **KFR** | [kfrlib/kfr](https://github.com/kfrlib/kfr) | GPL/상용 | ❌ | FFT, FIR/IIR, Biquad (SIMD 최적화) | ✅ | 1.5k+ |
| **Q** | [cycfi/q](https://github.com/cycfi/q) | MIT | ❌ | Pitch Detection, Filters, Envelope | ✅ | 600+ |
| **eDSP** | [mohabouje/eDSP](https://github.com/mohabouje/eDSP) | GPL-3.0 | ❌ | Filters, FFT, Statistics | ✅ | 400+ |

---

## 2. 이펙트별 추천 라이브러리

### 2.1 Equalizer (EQ)

| 라이브러리 | 라이선스 | 기능 | 특징 |
|:-----------|:---------|:-----|:-----|
| **Signalsmith DSP** (`filters.h`) | MIT | Biquad, LP/HP/BP/Notch/Peak/Shelf | Header-only, 고품질 |
| **sndfilter** (`biquad.c`) | 0BSD | 10-band EQ 가능 | Chromium 기반 공식 |
| **rtff** | MIT | Frequential Filtering | STFT 기반 |

**추천**: `Signalsmith DSP` - 이미 Signalsmith Stretch 사용 중이므로 통합 용이

### 2.2 Reverb

| 라이브러리 | 라이선스 | 알고리즘 | 특징 |
|:-----------|:---------|:---------|:-----|
| **sndfilter** (`reverb.c`) | 0BSD | Freeverb3 Progenitor2 기반 | 알고리즘 리버스 엔지니어링, 고품질 |
| **Gimmel** | MIT | Schroeder Reverb | 교육용 문서화 |

**추천**: `sndfilter` - 완성도 높고 0BSD 라이선스 (MIT보다 자유로움)

### 2.3 Compressor / Limiter

| 라이브러리 | 라이선스 | 기능 | 특징 |
|:-----------|:---------|:-----|:-----|
| **sndfilter** (`compressor.c`) | 0BSD | Dynamic Range Compression | Chromium 기반, Attack/Release/Knee |
| **chowdsp_compressor** | BSD-3 | Compressor, Limiter | JUCE 플러그인에서 분리 가능 |

**추천**: `sndfilter`

### 2.4 Chorus / Flanger / Phaser

| 라이브러리 | 라이선스 | 기능 | 특징 |
|:-----------|:---------|:-----|:-----|
| **Gimmel** | MIT | Chorus, Flanger, Phaser | Header-only, 교육용 문서화 |
| **Signalsmith DSP** (`delay.h`) | MIT | Delay Line (Chorus/Flanger 구현 가능) | Multi-tap delay |

**추천**: `Gimmel` - 이펙트별 구현 완료됨

### 2.5 Distortion / Saturation

| 라이브러리 | 라이선스 | 기능 | 특징 |
|:-----------|:---------|:-----|:-----|
| **Gimmel** | MIT | Overdrive, Distortion | Waveshaping 기반 |
| **chowdsp_utils** | BSD-3 | Saturator, Waveshaper | JUCE 의존성 |

**추천**: `Gimmel`

---

## 3. 통합 권장 조합

Transpose 앱에 가장 적합한 조합:

```
┌─────────────────────────────────────────────────────────────┐
│                    권장 라이브러리 스택                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   Pitch/Tempo: Signalsmith Stretch (이미 구현됨)              │
│                                                              │
│   EQ:          Signalsmith DSP (filters.h)                   │
│   Reverb:      sndfilter (reverb.c)                          │
│   Compressor:  sndfilter (compressor.c)                      │
│   Modulation:  Gimmel (Chorus/Flanger/Phaser)                │
│   Distortion:  Gimmel (Overdrive)                            │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 이유:
1. **Signalsmith DSP**: 이미 Signalsmith Stretch 사용 중, 동일 제작자, Header-only
2. **sndfilter**: 0BSD (가장 자유로운 라이선스), Chromium에서 검증된 코드
3. **Gimmel**: 모듈레이션 이펙트 전문, Header-only, 교육용 문서화

---

## 4. 라이선스 비교

| 라이선스 | 상업적 사용 | 소스 공개 의무 | 라이선스 표기 |
|:---------|:-----------|:--------------|:-------------|
| **0BSD** | ✅ | ❌ | ❌ (필요 없음) |
| **MIT** | ✅ | ❌ | ✅ (필요) |
| **BSD-3** | ✅ | ❌ | ✅ (필요) |
| **GPL** | ✅ | ⚠️ (전체 소스) | ✅ |

**0BSD > MIT > BSD-3** 순으로 자유로움

---

## 5. 참고 링크

### 라이브러리 목록
- [Awesome Music DSP](https://github.com/olilarkin/awesome-musicdsp) - 음악 DSP 리소스 큐레이션

### 개별 라이브러리
| 이름 | URL |
|:-----|:----|
| Signalsmith DSP | https://signalsmith-audio.co.uk/code/dsp/ |
| sndfilter | https://github.com/velipso/sndfilter |
| Gimmel | https://github.com/Mahdi03/Gimmel |
| Q | https://github.com/cycfi/q |
| chowdsp_utils | https://github.com/Chowdhury-DSP/chowdsp_utils |

---

*작성일: 2025-01-08*
*브랜치: add-superpowered-sdk*
