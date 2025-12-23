package com.example.shadowsprint.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.shadowsprint.game.GameSurfaceView

@Composable
fun GameScreen() {
    AndroidView(
        factory = { context ->
            GameSurfaceView(context)
        },
        modifier = Modifier.fillMaxSize()
    )
}
