package com.jarimanis.jarimanis.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jarimanis.jarimanis.data.local.SessionManager
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.ui.features.auth.LoginScreen
import com.jarimanis.jarimanis.ui.features.auth.RegisterScreen
import com.jarimanis.jarimanis.ui.features.dashboard.DashboardScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    sessionManager: SessionManager // Tambahkan ini agar Navigasi bisa membaca DataStore
) {
    // Membaca status Token dan Role secara realtime dari DataStore
    val token by sessionManager.getToken.collectAsState(initial = null)
    val role by sessionManager.getRole.collectAsState(initial = "")

    // Logika Gerbang: Jika token kosong = login. Jika ada = dashboard.
    val startDestination = if (token == null) "login" else "dashboard"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    // Tidak perlu navController.navigate() karena state 'token' akan berubah
                    // dan otomatis memicu NavHost me-render ulang ke 'dashboard'
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    // Sama seperti login, otomatis terlempar ke dashboard
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                role = role ?: "User",
                onLogoutClick = {
                    authViewModel.logout() // Menghapus token
                    // Saat token terhapus, Navigasi akan otomatis mendeteksi dan kembali ke "login"
                }
            )
        }
    }
}