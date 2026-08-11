package com.jarimanis.jarimanis.ui.features.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.ui.features.student.ZonaViewModel
import com.jarimanis.jarimanis.ui.features.profil.ProfileScreen
import com.jarimanis.jarimanis.ui.features.student.RaporScreen
import com.jarimanis.jarimanis.ui.features.teacher.DashboardGuruScreen

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    object Users : BottomNavItem("users", "Users", Icons.Filled.People)
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
    dashboardAdminContent: @Composable () -> Unit // <--- 1. TAMBAHAN PARAMETER ADMIN
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- 2. FILTER BOTTOM NAVIGATION BERDASARKAN ROLE ---
    val items = when (role) {
        "admin" -> {
            listOf(BottomNavItem.Home, BottomNavItem.Users, BottomNavItem.Profil) // Admin punya 3 menu
        }
        "guru" -> {
            listOf(BottomNavItem.Home, BottomNavItem.Profil) // Guru punya 2 menu
        }
        else -> {
            listOf(BottomNavItem.Home, BottomNavItem.Rapor, BottomNavItem.Profil) // Siswa punya 3 menu
        }
    }

    Scaffold(
        bottomBar = {
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
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
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
                when (role) {
                    "admin" -> {
                        dashboardAdminContent()
                    }
                    "guru" -> {
                        DashboardGuruScreen(
                            viewModel = authViewModel,
                            token = token,
                            onSiswaClick = onNavigateToDetailSiswa
                        )
                    }
                    else -> {
                        dashboardSiswaContent()
                    }
                }
            }
            composable(BottomNavItem.Rapor.route) {
                RaporScreen(viewModel = zonaViewModel, token = token)
            }
            composable(BottomNavItem.Users.route) {
                if (role == "admin") {
                    com.jarimanis.jarimanis.ui.features.admin.DaftarUserScreen(
                        viewModel = authViewModel,
                        token = token
                    )
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