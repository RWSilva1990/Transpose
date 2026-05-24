package com.example.transpose

import android.app.Activity
import android.os.Bundle
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.media.manager.AudioEffectsManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application : android.app.Application(), ImageLoaderFactory {

    @Inject
    lateinit var audioEffectsManager: AudioEffectsManager

    private var startedActivityCount = 0

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(
            object : android.app.Application.ActivityLifecycleCallbacks {
                override fun onActivityStarted(activity: Activity) {
                    startedActivityCount += 1
                }

                override fun onActivityStopped(activity: Activity) {
                    startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
                    if (startedActivityCount == 0 && !activity.isChangingConfigurations) {
                        audioEffectsManager.disableVocalRemovalForBackground()
                    }
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
                override fun onActivityResumed(activity: Activity) = Unit
                override fun onActivityPaused(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            }
        )
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(150L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(false)
            .build()
    }
}
