package com.example.shadowsprint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shadowsprint.ui.GameScreen
import com.example.shadowsprint.ui.StartScreen
import com.example.shadowsprint.ui.theme.ShadowSprintTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShadowSprintTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShadowSprintApp()
                }
            }
        }
    }
}

@Composable
fun ShadowSprintApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "start") {
        composable("start") {
            StartScreen(onPlayClick = { navController.navigate("game") })
        }
        composable("game") {
            GameScreen()
        }
    }
}