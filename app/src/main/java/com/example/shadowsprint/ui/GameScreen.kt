package com.example.shadowsprint.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.shadowsprint.game.GameSurfaceView

@Composable
fun GameScreen() {
    var gameView by remember { mutableStateOf<GameSurfaceView?>(null) }
    
    // Joystick State
    var joystickX by remember { mutableStateOf(0f) }
    var joystickY by remember { mutableStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                GameSurfaceView(context).also { gameView = it }
            },
            modifier = Modifier.fillMaxSize()
        )

        // CONTROLS OVERLAY
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            
            // --- LEFT: JOYSTICK ---
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull()
                                if (change != null) {
                                    if (change.pressed) {
                                        val position = change.position
                                        val cX = size.width / 2
                                        val cY = size.height / 2
                                        val dX = (position.x - cX) / (size.width / 2)
                                        val dY = (position.y - cY) / (size.height / 2)
                                        
                                        val dist = kotlin.math.sqrt(dX*dX + dY*dY)
                                        val finalX = if(dist > 1) dX/dist else dX
                                        val finalY = if(dist > 1) dY/dist else dY
                                        
                                        joystickX = finalX
                                        joystickY = finalY
                                        
                                        gameView?.gameManager?.setJoystickLines(finalX, finalY)
                                    } else {
                                        joystickX = 0f
                                        joystickY = 0f
                                        gameView?.gameManager?.setJoystickLines(0f, 0f)
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = (joystickX * 50).dp, y = (joystickY * 50).dp)
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                )
            }

            // --- RIGHT: ACTION BUTTONS ---
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Bottom) {
                ControlBtn("DASH", Color.Blue) { gameView?.gameManager?.onDash() }
                ControlBtn("ATK", Color.Red) { gameView?.gameManager?.onAttack() }
                ControlBtn("JUMP", Color.Green) { gameView?.gameManager?.onJump() }
            }
        }
    }
}

@Composable
fun ControlBtn(text: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
