cmake_minimum_required(VERSION 3.22.1)

project("superpowered_audio")

set(SUPERPOWERED_PATH ${CMAKE_SOURCE_DIR}/superpowered)

include_directories(${CMAKE_SOURCE_DIR})
include_directories(${SUPERPOWERED_PATH})

add_library(
    superpowered_audio
    SHARED
    SuperpoweredAudioBridge.cpp
    ${SUPERPOWERED_PATH}/OpenSource/SuperpoweredAndroidAudioIO.cpp
)

target_link_libraries(
    superpowered_audio
    log
    android
    OpenSLES
    ${SUPERPOWERED_PATH}/libSuperpoweredAndroid${ANDROID_ABI}.a
)