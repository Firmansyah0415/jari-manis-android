package com.jarimanis.jarimanis.ui.features.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
    val currentDestination by rememberUpdatedState(newValue = nextDestination)

    // --- 1. STATE UNTUK MEMICU ANIMASI ---
    var startAnimation by remember { mutableStateOf(false) }

    // --- 2. ANIMASI SCALE (Pop-up dengan efek memantul / Bouncy) ---
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f, // Mulai dari ukuran setengah, lalu membesar
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_anim"
    )

    // --- 3. ANIMASI FADE IN (Transparansi / Alpha) ---
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f, // Mulai dari transparan (0), lalu jelas (1)
        animationSpec = tween(durationMillis = 1000), // Durasi fade 1 detik
        label = "alpha_anim"
    )

    LaunchedEffect(key1 = true) {
        // Memicu animasi berjalan tepat saat layar dibuka
        startAnimation = true

        delay(2000L.milliseconds)
        navController.navigate(currentDestination) {
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
            contentScale = ContentScale.Crop, // Memastikan gambar memenuhi area yang dilengkungkan
            modifier = Modifier
                .size(250.dp)
                // Memasukkan nilai animasi ke dalam elemen gambar
                .graphicsLayer {
                    scaleX = scaleAnim
                    scaleY = scaleAnim
                    alpha = alphaAnim
                }
                // Melengkungkan sudut logo (radius 48.dp cocok untuk gambar ukuran 250.dp)
                .clip(RoundedCornerShape(48.dp))
                .background(Color.White)
        )
    }
}