package com.mentorme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.mentorme.app.core.designsystem.MentorMeTheme
import com.mentorme.app.ui.navigation.MentorMeNavigation

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MentorMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MentorMeApp()
                }
            }
        }
    }
}

@Composable
fun MentorMeApp() {
    // TODO: Add authentication state management with ViewModel
    var isAuthenticated by remember { mutableStateOf(false) }

    MentorMeNavigation(isAuthenticated = isAuthenticated)
}
