package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioDarkBg

import android.util.Log
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK asynchronously
        try {
            MobileAds.initialize(this) { initializationStatus ->
                Log.d("MobileAds", "Google Mobile Ads SDK Initialized: ${initializationStatus.adapterStatusMap}")
            }
        } catch (e: Exception) {
            Log.e("MobileAds", "Failed to initialize Google Mobile Ads SDK", e)
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioDarkBg
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
