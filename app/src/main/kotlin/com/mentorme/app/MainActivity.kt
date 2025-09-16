// MainActivity.kt
package com.mentorme.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mentorme.app.core.designsystem.MentorMeTheme
import com.mentorme.app.core.designsystem.MMGradients
import com.mentorme.app.ui.navigation.AppNav

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Trong suốt hoàn toàn cho status + navigation bars
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(0x00000000, 0x00000000),
            navigationBarStyle = SystemBarStyle.auto(0x00000000, 0x00000000)
        )
        super.onCreate(savedInstanceState)
        setContent {
            MentorMeTheme {
                // nền gradient theo design system
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MMGradients.Primary)
                ) {
                    val nav = rememberNavController()
                    AppNav(nav)              // <— NavHost của app
                }
            }
        }
    }
}
