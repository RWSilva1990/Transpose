cmake_minimum_required(VERSION 3.22.1)

project("signalsmith_audio")

# C++17 for Signalsmith Stretch
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Signalsmith paths
set(SIGNALSMITH_PATH ${CMAKE_SOURCE_DIR}/signalsmith)

include_directories(${CMAKE_SOURCE_DIR})
include_directories(${SIGNALSMITH_PATH})
include_directories(${SIGNALSMITH_PATH}/signalsmith-linear)

add_library(
    signalsmith_audio
    SHARED
    SignalsmithAudioBridge.cpp
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
