package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.LendShareAppContent
import com.example.ui.RentalViewModel
import com.example.ui.RentalViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Instantiate our custom Sharing ecosystem ViewModel
    val viewModel = ViewModelProvider(
        this,
        RentalViewModelFactory(application)
    )[RentalViewModel::class.java]

    setContent {
      MyApplicationTheme {
        LendShareAppContent(viewModel = viewModel)
      }
    }
  }
}
