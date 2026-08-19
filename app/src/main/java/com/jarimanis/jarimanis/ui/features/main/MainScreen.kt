package com.jarimanis.jarimanis.ui.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jarimanis.jarimanis.ui.features.admin.LeaderboardAdminScreen
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.ui.features.student.ZonaViewModel
import com.jarimanis.jarimanis.ui.features.profil.ProfileScreen
import com.jarimanis.jarimanis.ui.features.student.RaporScreen
import com.jarimanis.jarimanis.ui.features.teacher.DashboardGuruScreen
import com.jarimanis.jarimanis.ui.features.student.LeaderboardScreen
import com.jarimanis.jarimanis.ui.features.teacher.LeaderboardGuruScreen

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    object Users : BottomNavItem("users", "Users", Icons.Filled.People)
    object Leaderboard : BottomNavItem("leaderboard", "Peringkat", Icons.Filled.EmojiEvents)
    object Rapor : BottomNavItem("rapor", "Rapor", Icons.Filled.Star)
    object Profil : BottomNavItem("profil", "Profil", Icons.Filled.Person)
}

@Composable
fun MainScreen(
    role: String,
    token: String,
    authViewModel: AuthViewModel,
    zonaViewModel: ZonaViewModel,
    onLogoutClick: () -> Unit,
    onNavigateToEditProfil: () -> Unit,
    onNavigateToTentang: () -> Unit,
    onNavigateToDetailSiswa: (Int) -> Unit,
    dashboardSiswaContent: @Composable () -> Unit,
    dashboardAdminContent: @Composable () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // KUNCI: Eksekusi eksplisit untuk siswa. Jika kosong (lagi proses logout), sembunyikan navigasi.
    val items = when (role) {
        "admin" -> listOf(BottomNavItem.Home, BottomNavItem.Users, BottomNavItem.Leaderboard, BottomNavItem.Profil)
        "guru" -> listOf(BottomNavItem.Home, BottomNavItem.Leaderboard, BottomNavItem.Profil)
        "siswa" -> listOf(BottomNavItem.Home, BottomNavItem.Leaderboard, BottomNavItem.Rapor, BottomNavItem.Profil)
        else -> emptyList()
    }

    Scaffold(
        bottomBar = {
            if (items.isNotEmpty()) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                // KUNCI: Jangan pernah gunakan "else -> dashboardSiswa". Eksekusi sesuai role!
                when (role) {
                    "admin" -> dashboardAdminContent()
                    "guru" -> DashboardGuruScreen(viewModel = authViewModel, token = token, onSiswaClick = onNavigateToDetailSiswa)
                    "siswa" -> dashboardSiswaContent()
                    else -> Box(modifier = Modifier.fillMaxSize()) // Pelindung saat logout instan
                }
            }
            composable(BottomNavItem.Leaderboard.route) {
                when (role) {
                    "admin" -> LeaderboardAdminScreen(authViewModel = authViewModel, zonaViewModel = zonaViewModel, token = token, onSiswaClick = onNavigateToDetailSiswa)
                    "guru" -> LeaderboardGuruScreen(authViewModel = authViewModel, zonaViewModel = zonaViewModel, token = token)
                    "siswa" -> LeaderboardScreen(viewModel = zonaViewModel, token = token)
                    else -> Box(modifier = Modifier.fillMaxSize())
                }
            }
            composable(BottomNavItem.Rapor.route) {
                RaporScreen(viewModel = zonaViewModel, token = token)
            }
            composable(BottomNavItem.Users.route) {
                if (role == "admin") {
                    com.jarimanis.jarimanis.ui.features.admin.DaftarUserScreen(viewModel = authViewModel, token = token)
                }
            }
            composable(BottomNavItem.Profil.route) {
                ProfileScreen(
                    viewModel = authViewModel,
                    token = token,
                    onLogoutClick = onLogoutClick,
                    onEditClick = onNavigateToEditProfil,
                    onTentangClick = onNavigateToTentang
                )
            }
        }
    }
}