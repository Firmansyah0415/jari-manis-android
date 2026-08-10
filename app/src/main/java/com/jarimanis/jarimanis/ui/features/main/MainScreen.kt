package com.jarimanis.jarimanis.ui.features.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel // TAMBAHAN IMPORT
import com.jarimanis.jarimanis.ui.features.dashboard.ZonaViewModel
import com.jarimanis.jarimanis.ui.features.profil.ProfileScreen
import com.jarimanis.jarimanis.ui.features.rapor.RaporScreen
import com.jarimanis.jarimanis.ui.features.teacher.DashboardGuruScreen

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    object Rapor : BottomNavItem("rapor", "Rapor", Icons.Filled.Star)
    object Profil : BottomNavItem("profil", "Profil", Icons.Filled.Person)
}

@Composable
fun MainScreen(
    role: String,
    token: String,
    authViewModel: AuthViewModel, // 1. TAMBAHKAN PARAMETER INI
    zonaViewModel: ZonaViewModel,
    onLogoutClick: () -> Unit,
    onNavigateToEditProfil: () -> Unit,
    onNavigateToTentang: () -> Unit,
    onNavigateToDetailSiswa: (Int) -> Unit,
    dashboardSiswaContent: @Composable () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = if (role == "guru") {
        listOf(BottomNavItem.Home, BottomNavItem.Profil)
    } else {
        listOf(BottomNavItem.Home, BottomNavItem.Rapor, BottomNavItem.Profil)
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
                if (role == "guru") {
                    DashboardGuruScreen(
                        viewModel = authViewModel,
                        token = token,
                        onSiswaClick = onNavigateToDetailSiswa
                    )
                } else {
                    dashboardSiswaContent()
                }
            }
            composable(BottomNavItem.Rapor.route) {
                RaporScreen(viewModel = zonaViewModel, token = token)
            }
            composable(BottomNavItem.Profil.route) {
                // 2. BERIKAN AUTHVIEWMODEL DAN TOKEN KE PROFIL SCREEN
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