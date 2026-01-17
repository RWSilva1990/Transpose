# 3D Audio Upgrade Plan (OpenAL Soft + BS2B)

Goal: Replace the basic Android `Virtualizer` path with a high-fidelity headphone experience that combines **OpenAL Soft's HRTF virtual speaker renderer** and a light **BS2B (Bauer stereophonic-to-binaural) crossfeed** stage, while keeping the existing `AudioProcessor`/AudioTrack pipeline.

## 1. Dependencies & Build

| Item | Notes |
| --- | --- |
| OpenAL Soft | Build the latest release with `-DANDROID_STL=c++_static`, disable device backends we do not need (ALSA/Pulse) and enable `ALSOFT_EFX`, `ALSOFT_HRTF`. Compile as static `.a` and link in `audio` module with CMake `ExternalProject_Add`. |
| HRTF Datasets | Bundle MIT KEMAR (already in tree) or OpenAL Soft's built-in SOFA tables. Provide selector for "Studio" (default), "Concert" (wider). |
| libbs2b | Small C library; compile as static library and expose `bs2b_t`. Only need headphone crossfeed mode, set default `Fcut=700 Hz`, `Feed=4.5 dB`. |

## 2. Audio Flow w/ Existing Pipeline

```
ExoPlayer PCM -> SignalsmithAudioProcessor (Java) -> JNI (SignalsmithProcessor.cpp)
                                                 -> [NEW] if 3D-on:
                                                        1) Copy float frames into bs2b (crossfeed subtle warm-up)
                                                        2) Submit to OpenAL Soft buffer queue (AL_BUFFER)
                                                        3) Use OpenAL HRTF renderer to spatialize virtual speaker pair (L/R at ±θ)
                                                        4) Pull rendered headphone stereo back into output buffer for AudioTrack
```

- Keep AudioTrack ownership unchanged; OpenAL Soft is used purely as an offline renderer inside our processor (no device control). Use `alcCreateContext` with `ALC_SOFT_loopback` device so we can render offline blocks.
- Buffering: OpenAL Soft expects float samples. Extend `SignalsmithProcessor::processBlock` to write frames into a preallocated float buffer -> bs2b -> `alBufferDataSOFT` -> `alcRenderSamplesSOFT` to grab spatialized output.
- Latency: Choose block size 256 or 512 frames (match current processing block). OpenAL Soft loopback adds ~1 block latency which is acceptable.

## 3. Parameter & UI Plan

| Control | Implementation |
| --- | --- |
| Mode Switch | Present "Virtual Studio (fast)" vs "Immersive (OpenAL)". When immersive is enabled, bypass Android `Virtualizer` and run bs2b + OpenAL path. |
| Crossfeed Amount | Map to bs2b `feed/cut` knobs but expose as "Ear Blend" (Low/Medium/High). |
| Speaker Angle | Convert to OpenAL Soft source positions: default ±30°, allow ±15°..±75°. Link to existing azimuth slider for consistency. |
| Elevation | Optional: allow 0° (ear level) vs +15° (stage). Set via OpenAL Soft `AL_POSITION` y-axis. |
| Room Blend | Provide 0..1 mix between OpenAL output and dry stereo to avoid overly hollow sound. |

## 4. JNI/C++ Tasks

1. Add new module `openal_bridge.cpp` responsible for:
   - Initializing loopback context: `alcOpenDevice(NULL)`, `alcLoopbackOpenDeviceSOFT`, `alcCreateContext` with `ALC_HRTF_SOFT=ALC_TRUE`.
   - Creating two virtual speakers (OpenAL sources) and a listener at origin.
   - Managing buffer queue per process block.
2. Integrate bs2b: create `bs2b_t` instance, feed interleaved float frames (`bs2b_process_float`).
3. Provide JNI setters for new UI knobs (mode, ear blend, angle, elevation, room blend).
4. Ensure thread safety: reuse existing audio mutex or confine OpenAL calls to processing thread.

## 5. Testing Checklist

- **Functional**: Toggle immersive mode during playback, ensure no clicks (crossfade between old/new path).
- **Performance**: Measure CPU cost on mid-tier device (Snapdragon 778G). Target <10% of one core at 48 kHz.
- **HRTF Data**: Validate both MIT + OpenAL packaged datasets load correctly; fallback to built-in if file missing.
- **Fail-safe**: If OpenAL context creation fails, automatically fall back to Android Virtualizer and notify the UI.

## 6. Deliverables

1. New JNI bindings + CMake integration for OpenAL Soft and libbs2b.
2. Extended `SignalsmithVirtualizerSection` UI with mode switch, ear-blend slider, stage angle preset chips.
3. QA script + sample audio project verifying crossfeed + HRTF effect.
4. Docs entry describing the difference between the two 3D modes for release notes.
