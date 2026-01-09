cmake_minimum_required(VERSION 3.22.1)

project("signalsmith_audio")

# C++17 for Signalsmith Stretch
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Library paths
set(SIGNALSMITH_PATH ${CMAKE_SOURCE_DIR}/signalsmith)
set(MIT_HRTF_PATH ${CMAKE_SOURCE_DIR}/mit-hrtf-lib)
set(FFTCONVOLVER_PATH ${CMAKE_SOURCE_DIR}/FFTConvolver)
set(DAISYSP_PATH ${CMAKE_SOURCE_DIR}/DaisySP/Source)

# Include directories
include_directories(${CMAKE_SOURCE_DIR})
include_directories(${SIGNALSMITH_PATH})
include_directories(${SIGNALSMITH_PATH}/signalsmith-linear)
include_directories(${SIGNALSMITH_PATH}/basics/include)
include_directories(${SIGNALSMITH_PATH}/basics/modules/dsp/include)
include_directories(${SIGNALSMITH_PATH}/basics/modules/hilbert-iir/include)
include_directories(${SIGNALSMITH_PATH}/basics/modules/linear/include)
include_directories(${MIT_HRTF_PATH}/include)
include_directories(${FFTCONVOLVER_PATH})

# DaisySP includes
include_directories(${DAISYSP_PATH})
include_directories(${DAISYSP_PATH}/Utility)
include_directories(${DAISYSP_PATH}/Effects)
include_directories(${DAISYSP_PATH}/Synthesis)

add_library(
    signalsmith_audio
    SHARED
    SignalsmithAudioBridge.cpp
    SignalsmithProcessor.cpp
    # MIT HRTF
    ${MIT_HRTF_PATH}/source/mit_hrtf_lib.c
    # FFTConvolver
    ${FFTCONVOLVER_PATH}/AudioFFT.cpp
    ${FFTCONVOLVER_PATH}/FFTConvolver.cpp
    ${FFTCONVOLVER_PATH}/TwoStageFFTConvolver.cpp
    ${FFTCONVOLVER_PATH}/Utilities.cpp
    # DaisySP Effects
    ${DAISYSP_PATH}/Effects/phaser.cpp
    ${DAISYSP_PATH}/Effects/flanger.cpp
    ${DAISYSP_PATH}/Effects/tremolo.cpp
    ${DAISYSP_PATH}/Effects/autowah.cpp
    ${DAISYSP_PATH}/Effects/decimator.cpp
    # DaisySP Synthesis (needed for Tremolo's Oscillator)
    ${DAISYSP_PATH}/Synthesis/oscillator.cpp
    # DaisySP Utility
    ${DAISYSP_PATH}/Utility/dcblock.cpp
    ${DAISYSP_PATH}/Utility/metro.cpp
)

target_compile_options(signalsmith_audio PRIVATE
    -O3
    -ffast-math
    -fsigned-char
)

target_link_libraries(
    signalsmith_audio
    log
    android
)
