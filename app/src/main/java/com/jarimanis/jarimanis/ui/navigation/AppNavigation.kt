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
// Ganti / pastikan path import ini sesuai dengan lokasi file MainScreen Anda
import com.jarimanis.jarimanis.ui.features.main.MainScreen
import com.jarimanis.jarimanis.ui.features.profil.EditProfileScreen
import com.jarimanis.jarimanis.ui.features.student.RaporScreen
import com.jarimanis.jarimanis.ui.features.teacher.DetailRaporSiswaScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    zonaViewModel: ZonaViewModel,
    sessionManager: SessionManager
) {
    // 1. Membaca 3 Pilar Data secara Real-Time dari DataStore
    val token by sessionManager.getToken.collectAsState(initial = null)
    val role by sessionManager.getRole.collectAsState(initial = null)
    val isPretestDone by sessionManager.getPretestStatus.collectAsState(initial = false)

    // Membaca Data User dari ViewModel untuk Dashboard (Nama, Poin, & Hari Aktif)
    val userProfile by authViewModel.userProfile.collectAsState(initial = null)

    // 2. Logika Gerbang Utama (Start Destination)
    val startDestination = if (token == null) {
        "login"
    } else if (role == "guru" || role == "admin") { // <--- TAMBAHKAN ROLE ADMIN DI SINI
        "main_route"
    } else {
        if (isPretestDone) "main_route" else "pre_test_route"
    }

    val coroutineScope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = startDestination) {

        // ========================
        // AREA OTENTIKASI
        // ========================

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    val destination = if (role == "guru" || role == "admin") { // <--- TAMBAHKAN ROLE ADMIN DI SINI
                        "main_route"
                    } else {
                        if (isPretestDone) "main_route" else "pre_test_route"
                    }

                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    val destination = if (role == "guru") "main_route" else "pre_test_route"

                    navController.navigate(destination) {
                        popUpTo("register") { inclusive = true }
                    }
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
                    coroutineScope.launch {
                        sessionManager.updatePretestStatus(true)
                    }
                    navController.navigate("main_route") {
                        popUpTo("pre_test_route") { inclusive = true }
                    }
                }
            )
        }

        // ========================
        // AREA MAIN SCREEN (BOTTOM NAVIGATION)
        // ========================

        composable("main_route") {
            MainScreen(
                role = role ?: "siswa",
                token = token ?: "",
                authViewModel = authViewModel,
                zonaViewModel = zonaViewModel,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToEditProfil = {
                    navController.navigate("edit_profil")
                },
                onNavigateToTentang = { // <--- TAMBAHKAN BLOK INI
                    navController.navigate("tentang_aplikasi_route")
                },
                onNavigateToDetailSiswa = { id ->
                    navController.navigate("detail_rapor_siswa/$id")
                },
                dashboardSiswaContent = {
                    // --- SUNTIKAN DATA NAMA, POIN, DAN HARI KE DASHBOARD ---
                    DashboardScreen(
                        navController = navController,
                        userName = userProfile?.name ?: "Siswa",
                        totalPoints = userProfile?.totalSkor ?: 0,
                        totalHariAktif = userProfile?.totalHariAktif ?: 0,

                        // KODE YANG DIPERBARUI DI SINI:
                        isPostTestDone = userProfile?.isPostTestDone == true,

                        onRefreshRequest = {
                            if (!token.isNullOrEmpty()) authViewModel.fetchProfile(token!!)
                        }
                    )
                },
                // --- TAMBAHKAN BLOK ADMIN INI ---
                dashboardAdminContent = {
                    DashboardAdminScreen(
                        viewModel = authViewModel,
                        token = token ?: "",
                        // Arahkan klik ke halaman Rapor Detail!
                        onSiswaClick = { id ->
                            navController.navigate("detail_rapor_siswa/$id")
                        }
                    )
                }
            )
        }

        // ========================
        // AREA ZONA (FULL SCREEN)
        // ========================

        composable("recall_makanan_route") {
            RecallMakananScreen(navController, zonaViewModel, token ?: "")
        }

        composable("aktivitas_fisik_route") {
            AktivitasFisikScreen(navController, zonaViewModel, token ?: "")
        }

        composable("minum_ttd_route") {
            MinumTtdScreen(navController, zonaViewModel, token ?: "")
        }

        composable("personal_hygiene_route") {
            PersonalHygieneScreen(navController, zonaViewModel, token ?: "")
        }

        composable(BottomNavItem.Rapor.route) {
            RaporScreen(viewModel = zonaViewModel, token = token ?: "")
        }

        composable("edit_profil") {
            EditProfileScreen(
                viewModel = authViewModel,
                token = token ?: "",
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "detail_rapor_siswa/{siswaId}",
            arguments = listOf(navArgument("siswaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val siswaId = backStackEntry.arguments?.getInt("siswaId") ?: 0
            DetailRaporSiswaScreen(
                siswaId = siswaId,
                viewModel = authViewModel,
                token = token ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("edukasi_route") {
            EdukasiScreen(navController = navController)
        }

        composable(
            route = "detail_edukasi/{videoId}",
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            DetailEdukasiScreen(navController = navController, videoId = videoId)
        }

        // ========================
        // AREA POST-TEST (TAMBAHAN BARU)
        // ========================
        composable("post_test_route") {
            PostTestScreen(
                navController = navController,
                viewModel = zonaViewModel,
                token = token ?: "",
                onSuccessSubmit = {
                    // Setelah sukses post-test, kembali ke Home dan refresh profil untuk update poin
                    if (!token.isNullOrEmpty()) authViewModel.fetchProfile(token!!)
                    navController.popBackStack()
                }
            )
        }

        // ========================
        // AREA TENTANG APLIKASI
        // ========================
        composable("tentang_aplikasi_route") {
            // Pastikan Anda melakukan import TentangAplikasiScreen di atas file
            com.jarimanis.jarimanis.ui.features.profil.TentangAplikasiScreen(
                navController = navController
            )
        }
    }
}