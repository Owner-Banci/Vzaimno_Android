package com.vzaimno.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.vzaimno.app.app.navigation.VzaimnoNavHost
import com.vzaimno.app.core.designsystem.theme.VzaimnoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VzaimnoTheme {
                Surface {
                    VzaimnoNavHost()
                }
            }
        }
    }
}
