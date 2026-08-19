package com.jarimanis.jarimanis.ui.features.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState // <--- IMPORT BARU YANG SANGAT PENTING
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jarimanis.jarimanis.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    navController: NavController,
    nextDestination: String
) {
    // KUNCI PERBAIKAN 1: Selalu pantau tujuan terbaru, jangan pakai yang pertama kali terbaca
    val currentDestination by rememberUpdatedState(newValue = nextDestination)

    LaunchedEffect(key1 = true) {
        delay(2000L.milliseconds)
        navController.navigate(currentDestination) { // Gunakan currentDestination di sini
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_jari_manis),
            contentDescription = "Logo Jari Manis",
            modifier = Modifier.size(250.dp)
        )
    }
}