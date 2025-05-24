package com.example.baselineprofile

import androidx.benchmark.macro.StartupMode

abstract class AbstractBenchmark(
    protected val startupMode: StartupMode = StartupMode.WARM
) {


}