package com.example.wavora

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.wavora.theme.WavoraTheme

class MainActivity : ComponentActivity() {

    companion object {
        var isVideoPlaying: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MediaViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val uiState by viewModel.uiState.collectAsState()
            val accent = com.example.wavora.theme.WavoraAccents.getOrElse(uiState.customization.accentColorIndex) {
                com.example.wavora.theme.WavoraAccents[0]
            }.color
            val bg = com.example.wavora.theme.WavoraBgThemes.getOrElse(uiState.customization.bgThemeIndex) {
                com.example.wavora.theme.WavoraBgThemes[0]
            }.bg

            WavoraTheme(accentColor = accent, bgColor = bg) {
                MainNavigation(viewModel = viewModel)
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isVideoPlaying) {
            try {
                enterPictureInPictureMode(PictureInPictureParams.Builder().build())
            } catch (_: Exception) {}
        }
    }
}
