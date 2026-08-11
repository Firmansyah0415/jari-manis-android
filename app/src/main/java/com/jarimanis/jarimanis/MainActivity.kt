package com.jarimanis.jarimanis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.jarimanis.jarimanis.data.local.SessionManager
import com.jarimanis.jarimanis.data.network.ApiClient
import com.jarimanis.jarimanis.data.repository.AuthRepository
import com.jarimanis.jarimanis.data.repository.ZonaRepository // Import baru
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.ui.features.student.ZonaViewModel // Import baru
import com.jarimanis.jarimanis.ui.navigation.AppNavigation
import com.jarimanis.jarimanis.ui.theme.JariManisTheme
import com.jarimanis.jarimanis.utils.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Merakit Komponen Data (Dari bawah ke atas)
        val sessionManager = SessionManager(applicationContext)
        val authRepository = AuthRepository(ApiClient.authApi)
        val zonaRepository = ZonaRepository(ApiClient.zonaApi) // Tambahan perakitan ZonaRepository

        // 2. Memasukkannya ke ViewModel Factory
        val factory = ViewModelFactory(authRepository, sessionManager, zonaRepository) // Update factory
        val authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        val zonaViewModel = ViewModelProvider(this, factory)[ZonaViewModel::class.java] // Pembuatan ZonaViewModel

        // 3. Menampilkan UI Jetpack Compose
        setContent {
            JariManisTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    zonaViewModel = zonaViewModel, // Parameter error kini teratasi!
                    sessionManager = sessionManager
                )
            }
        }
    }
}