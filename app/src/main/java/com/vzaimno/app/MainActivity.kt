package com.vzaimno.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vzaimno.app.app.navigation.VzaimnoNavHost
import com.vzaimno.app.core.designsystem.theme.AppThemeRepository
import com.vzaimno.app.core.designsystem.theme.VzaimnoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appThemeRepository: AppThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState = appThemeRepository.themeState.collectAsStateWithLifecycle().value
            VzaimnoTheme(darkTheme = themeState.isDarkTheme) {
                Surface {
                    VzaimnoNavHost()
                }
            }
        }
    }
}
