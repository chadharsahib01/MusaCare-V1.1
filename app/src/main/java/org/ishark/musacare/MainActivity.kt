package org.ishark.musacare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.ishark.musacare.ui.AppViewModel
import org.ishark.musacare.ui.screens.MusaCareAppScreen
import org.ishark.musacare.ui.theme.MusaCareTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MusaCareRoot() }
    }
}

@Composable
private fun MusaCareRoot(vm: AppViewModel = hiltViewModel()) {
    MusaCareTheme {
        MusaCareAppScreen(vm)
    }
}
