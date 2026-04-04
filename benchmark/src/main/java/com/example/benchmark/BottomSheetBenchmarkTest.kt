package com.example.benchmark

import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BottomSheetBenchmarkTest {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @OptIn(ExperimentalMetricApi::class)
    @Test
    fun startupWithAutoPlay() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.transpose",
            metrics = listOf(StartupTimingMetric(), FrameTimingMetric(), TraceSectionMetric("*")),
            iterations = 5,
            startupMode = StartupMode.COLD,
            setupBlock = { pressHome() },
            measureBlock = {
                startActivityAndWait()

                if (device.wait(Until.hasObject(By.desc("NationalPlaylistItem")), 10000)) {
                    device.findObject(UiSelector().description("NationalPlaylistItem")).click()
                } else {
                    throw AssertionError("NationalPlaylistItem not found within 10s")
                }

                if (device.wait(Until.hasObject(By.desc("CommonVideoItem")), 10000)) {
                    device.findObject(UiSelector().description("CommonVideoItem")).click()
                } else {
                    throw AssertionError("CommonVideoItem not found within 10s")
                }

                if (!device.wait(Until.hasObject(By.desc("PlayerThumbnailView")), 10000)) {
                    throw AssertionError("PlayerThumbnailView not found within 10s")
                }

                Thread.sleep(3000)
            }
        )
    }

    @OptIn(ExperimentalMetricApi::class)
    @Test
    fun bottomSheetDragPerformance() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.transpose",
            metrics = listOf(
                FrameTimingMetric(),
                TraceSectionMetric("PlayerBottomSheet", TraceSectionMetric.Mode.Sum),
                TraceSectionMetric("MainPlayerLayout", TraceSectionMetric.Mode.Sum),
                TraceSectionMetric("PlayerThumbnailView", TraceSectionMetric.Mode.Sum)
            ),
            iterations = 5,
            startupMode = StartupMode.WARM,
            setupBlock = {
                startActivityAndWait()

                // 1. 홈 화면 로드 (네트워크 의존, 넉넉하게 15초)
                if (!device.wait(Until.hasObject(By.desc("NationalPlaylistItem")), 15000)) {
                    throw AssertionError(
                        "NationalPlaylistItem not found within 15s - check network"
                    )
                }
                device.findObject(UiSelector().description("NationalPlaylistItem")).click()

                // 2. 플레이리스트 영상 목록 로드
                if (!device.wait(Until.hasObject(By.desc("CommonVideoItem")), 15000)) {
                    throw AssertionError(
                        "CommonVideoItem not found within 15s - playlist may not have loaded"
                    )
                }
                device.findObject(UiSelector().description("CommonVideoItem")).click()

                // 3. 플레이어 표시 대기 - 실패해도 진행 (bottomSheet는 올라와 있을 수 있음)
                device.wait(Until.hasObject(By.desc("PlayerThumbnailView")), 15000)

                // 영상 로딩 및 재생 안정화 대기
                Thread.sleep(5000)
            },
            measureBlock = {
                val screenWidth = device.displayWidth
                val screenHeight = device.displayHeight
                val centerX = screenWidth / 2

                val bottomY = (screenHeight * 0.85).toInt()
                val topY = (screenHeight * 0.15).toInt()

                repeat(3) {
                    // expand
                    device.swipe(centerX, bottomY, centerX, topY, 15)
                    Thread.sleep(800)

                    // collapse
                    device.swipe(centerX, topY, centerX, bottomY, 15)
                    Thread.sleep(800)
                }
            }
        )
    }
}
