package com.jarimanis.jarimanis.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jarimanis.jarimanis.data.local.SessionManager
import com.jarimanis.jarimanis.ui.features.admin.DashboardAdminScreen
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.ui.features.auth.LoginScreen
import com.jarimanis.jarimanis.ui.features.auth.RegisterScreen
import com.jarimanis.jarimanis.ui.features.student.*
import com.jarimanis.jarimanis.ui.features.main.BottomNavItem
import com.jarimanis.jarimanis.ui.features.main.MainScreen
import com.jarimanis.jarimanis.ui.features.profil.EditProfileScreen
import com.jarimanis.jarimanis.ui.features.profil.TentangAplikasiScreen
import com.jarimanis.jarimanis.ui.features.teacher.DetailRaporSiswaScreen
import com.jarimanis.jarimanis.ui.features.splash.SplashScreen // <-- PASTIKAN IMPORT INI SESUAI
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    zonaViewModel: ZonaViewModel,
    sessionManager: SessionManager
) {
    val token by sessionManager.getToken.collectAsState(initial = null)
    val role by sessionManager.getRole.collectAsState(initial = null)
    val isPretestDone by sessionManager.getPretestStatus.collectAsState(initial = false)
    val userProfile by authViewModel.userProfile.collectAsState(initial = null)

    // Logika perhitungan tujuan halaman setelah Splash Screen
    val nextDestination = if (token == null) {
        "login"
    } else if (role == "guru" || role == "admin") {
        "main_route"
    } else {
        if (isPretestDone) "main_route" else "pre_test_route"
    }

    val coroutineScope = rememberCoroutineScope()

    // --- START DESTINATION SEKARANG ADALAH SPLASH SCREEN ---
    NavHost(navController = navController, startDestination = "splash") {

        // ========================
        // AREA SPLASH SCREEN
        // ========================
        composable("splash") {
            SplashScreen(
                navController = navController,
                nextDestination = nextDestination
            )
        }

        // ========================
        // AREA OTENTIKASI
        // ========================
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    val destination = if (role == "guru" || role == "admin") {
                        "main_route"
                    } else {
                        if (isPretestDone) "main_route" else "pre_test_route"
                    }
                    navController.navigate(destination) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    val destination = if (role == "guru") "main_route" else "pre_test_route"
                    navController.navigate(destination) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // ========================
        // AREA PRE-TEST
        // ========================
        composable("pre_test_route") {
            PreTestScreen(
                navController = navController,
                viewModel = zonaViewModel,
                token = token ?: "",
                onSuccessSubmit = {
                    coroutineScope.launch { sessionManager.updatePretestStatus(true) }
                    navController.navigate("main_route") { popUpTo("pre_test_route") { inclusive = true } }
                }
            )
        }

        // ========================
        // AREA MAIN SCREEN (BOTTOM NAVIGATION)
        // ========================
        composable("main_route") {
            MainScreen(
                role = role ?: "",
                token = token ?: "",
                authViewModel = authViewModel,
                zonaViewModel = zonaViewModel,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        // Menggunakan graph.id adalah cara teraman membersihkan tumpukan aplikasi
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToEditProfil = { navController.navigate("edit_profil") },
                onNavigateToTentang = { navController.navigate("tentang_aplikasi_route") },
                onNavigateToDetailSiswa = { id -> navController.navigate("detail_rapor_siswa/$id") },
                dashboardSiswaContent = {
                    DashboardScreen(
                        navController = navController,
                        userName = userProfile?.name ?: "Siswa",
                        totalPoints = userProfile?.totalSkor ?: 0,
                        totalHariAktif = userProfile?.totalHariAktif ?: 0,
                        isPostTestDone = userProfile?.isPostTestDone == true,
                        isPreTestKebugaranDone = userProfile?.isPreTestKebugaranDone ?: false,
                        isPostTestKebugaranDone = userProfile?.isPostTestKebugaranDone ?: false,
                        onRefreshRequest = { if (!token.isNullOrEmpty()) authViewModel.fetchProfile(token!!) }
                    )
                },
                dashboardAdminContent = {
                    DashboardAdminScreen(
                        viewModel = authViewModel,
                        token = token ?: "",
                        onSiswaClick = { id -> navController.navigate("detail_rapor_siswa/$id") }
                    )
                }
            )
        }

        // ========================
        // AREA ZONA & LAINNYA
        // ========================
        composable("recall_makanan_route") { RecallMakananScreen(navController, zonaViewModel, token ?: "") }
        composable("aktivitas_fisik_route") { AktivitasFisikScreen(navController, zonaViewModel, token ?: "") }
        composable("minum_ttd_route") { MinumTtdScreen(navController, zonaViewModel, token ?: "") }
        composable("personal_hygiene_route") { PersonalHygieneScreen(navController, zonaViewModel, token ?: "") }
        composable(BottomNavItem.Rapor.route) { RaporScreen(viewModel = zonaViewModel, token = token ?: "") }

        composable("edit_profil") {
            EditProfileScreen(viewModel = authViewModel, token = token ?: "", onNavigateBack = { navController.popBackStack() })
        }

        composable("detail_rapor_siswa/{siswaId}", arguments = listOf(navArgument("siswaId") { type = NavType.IntType })) { backStackEntry ->
            val siswaId = backStackEntry.arguments?.getInt("siswaId") ?: 0
            DetailRaporSiswaScreen(siswaId = siswaId, viewModel = authViewModel, token = token ?: "", onNavigateBack = { navController.popBackStack() })
        }

        composable("edukasi_route") { EdukasiScreen(navController = navController) }

        composable("detail_edukasi/{videoId}", arguments = listOf(navArgument("videoId") { type = NavType.StringType })) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            DetailEdukasiScreen(navController = navController, videoId = videoId)
        }

        composable("post_test_route") {
            PostTestScreen(
                navController = navController,
                viewModel = zonaViewModel,
                token = token ?: "",
                onSuccessSubmit = {
                    if (!token.isNullOrEmpty()) authViewModel.fetchProfile(token!!)
                    navController.popBackStack()
                }
            )
        }

        composable("tentang_aplikasi_route") { TentangAplikasiScreen(navController = navController) }

        composable("tes_kebugaran/{tipeTes}", arguments = listOf(navArgument("tipeTes") { type = NavType.StringType })) { backStackEntry ->
            val tipeTes = backStackEntry.arguments?.getString("tipeTes") ?: "pre"
            val gender = userProfile?.gender ?: "L"

            TesKebugaranScreen(
                navController = navController,
                viewModel = zonaViewModel,
                token = token ?: "",
                tipeTes = tipeTes,
                gender = gender
            )
        }

        composable("leaderboard_fisik_route") { LeaderboardScreen(viewModel = zonaViewModel, token = token ?: "") }
    }
}