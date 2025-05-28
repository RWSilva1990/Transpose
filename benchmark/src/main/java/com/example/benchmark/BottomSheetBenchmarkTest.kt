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
import androidx.tracing.Trace
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
            iterations = 1,
            startupMode = StartupMode.COLD,
            setupBlock = { pressHome() },
            measureBlock = {
                startActivityAndWait()


                if (device.wait(Until.hasObject(By.desc("NationalPlaylistItem")), 3000)) {
                    val nationalPlaylistItem =
                        device.findObject(UiSelector().description("NationalPlaylistItem"))
                    nationalPlaylistItem.click()
                } else {
                    throw AssertionError("NationalPlaylistItem not found within 3 seconds!")
                }

                if (device.wait(Until.hasObject(By.desc("CommonVideoItem")), 3000)) {
                    val commonVideoItem =
                        device.findObject(UiSelector().description("CommonVideoItem"))
                    commonVideoItem.click()
                } else {
                    throw AssertionError("CommonVideoItem not found within 3 seconds!")
                }

                if (!device.wait(Until.hasObject(By.desc("PlayerThumbnailView")), 3000)) {
                    throw AssertionError("PlayerThumbnailView not found within 5 seconds!")
                }

                Thread.sleep(3000)

//                val playerThumbnailView = device.findObject(UiSelector().description("PlayerThumbnailView"))
//                val rect = playerThumbnailView.bounds
//                val centerX = rect.centerX()
//                val centerY = rect.centerY()
//
//                device.swipe(centerX, centerY + 300, centerX, centerY - 300, 10)
//
//                device.wait(Until.hasObject(By.desc("BottomPlayerCloseButton")), 3000)
//
//                val updatedPlayerThumbnailView = device.findObject(UiSelector().description("PlayerThumbnailView"))
//                val updatedRect = updatedPlayerThumbnailView.bounds
//                val updatedCenterX = updatedRect.centerX()
//                val updatedCenterY = updatedRect.centerY()
//
//                device.swipe(updatedCenterX, updatedCenterY - 300, updatedCenterX, updatedCenterY + 300, 10)

            }
        )
    }
}
