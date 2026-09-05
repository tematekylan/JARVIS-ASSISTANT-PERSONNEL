package com.thackman.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.thackman.ai.ui.screens.MainAppNavigation
import com.thackman.ai.ui.theme.THackmanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            THackmanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF030609)),
                    color = Color(0xFF030609)
                ) {
                    MainAppNavigation()
                }
            }
        }
    }
}
