package com.example.transpose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.main.MainScreen
import com.example.ui.theme.TransposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var sharedUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedUrl = extractSharedUrl(intent)

        setContent {
            TransposeTheme {
                val externalUrlPlaybackViewModel: ExternalUrlPlaybackViewModel = hiltViewModel()
                val url = sharedUrl

                LaunchedEffect(url) {
                    if (!url.isNullOrBlank()) {
                        externalUrlPlaybackViewModel.openUrl(url)
                        sharedUrl = null
                    }
                }

                MainScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedUrl = extractSharedUrl(intent)
    }

    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type != null && intent.type != "text/plain") return null

        return intent.getStringExtra(Intent.EXTRA_TEXT)
            ?.trim()
            ?.takeIf { it.startsWith("https://") || it.startsWith("http://") }
    }
}
