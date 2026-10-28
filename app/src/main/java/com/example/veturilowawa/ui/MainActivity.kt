package com.example.veturilowawa.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.veturilowawa.ui.screens.StationListScreen
import com.example.veturilowawa.ui.theme.VeturiloWawaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeturiloWawaTheme {
                val viewModel: MainViewModel = viewModel()
                StationListScreen(viewModel = viewModel)
            }
        }
    }
}
