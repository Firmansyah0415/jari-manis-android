package com.jarimanis.jarimanis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.jarimanis.jarimanis.data.local.SessionManager
import com.jarimanis.jarimanis.data.network.ApiClient
import com.jarimanis.jarimanis.data.repository.AuthRepository
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.ui.navigation.AppNavigation
import com.jarimanis.jarimanis.ui.theme.JariManisTheme
import com.jarimanis.jarimanis.utils.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Merakit Komponen Data (Dari bawah ke atas)
        val sessionManager = SessionManager(applicationContext)
        val authRepository = AuthRepository(ApiClient.authApi)

        // 2. Memasukkannya ke ViewModel Factory
        val factory = ViewModelFactory(authRepository, sessionManager)
        val authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // 3. Menampilkan UI Jetpack Compose
        setContent {
            JariManisTheme {
                // Masukkan sessionManager ke dalam parameter
                AppNavigation(
                    authViewModel = authViewModel,
                    sessionManager = sessionManager
                )
            }
        }
    }
}