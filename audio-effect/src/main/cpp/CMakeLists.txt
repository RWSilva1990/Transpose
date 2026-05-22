cmake_minimum_required(VERSION 3.22.1)

project("signalsmith_audio")

# C++17 for Signalsmith Stretch
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Library paths
set(SIGNALSMITH_PATH ${CMAKE_SOURCE_DIR}/signalsmith)

# Include directories
include_directories(${CMAKE_SOURCE_DIR})
include_directories(${SIGNALSMITH_PATH})
include_directories(${SIGNALSMITH_PATH}/signalsmith-linear)
include_directories(${SIGNALSMITH_PATH}/basics/include)
include_directories(${SIGNALSMITH_PATH}/basics/modules/dsp/include)
include_directories(${SIGNALSMITH_PATH}/basics/modules/hilbert-iir/include)
include_directories(${SIGNALSMITH_PATH}/basics/modules/linear/include)
include_directories(${CMAKE_SOURCE_DIR}/third_party/onnxruntime/headers)

add_library(onnxruntime SHARED IMPORTED)
set_target_properties(onnxruntime PROPERTIES
    IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/third_party/onnxruntime/jni/${ANDROID_ABI}/libonnxruntime.so
)

add_library(
    signalsmith_audio
    SHARED
    SignalsmithProcessor.cpp
    effects/ReverbPlusEffect.cpp
    VocalRemovalNative.cpp
)

target_compile_options(signalsmith_audio PRIVATE
    -O3
    -ffast-math
    -fsigned-char
)

target_link_libraries(
    signalsmith_audio
    onnxruntime
    log
    android
)
