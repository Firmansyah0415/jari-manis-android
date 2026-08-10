package com.jarimanis.jarimanis.ui.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jarimanis.jarimanis.ui.components.YouTubeVideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailEdukasiScreen(navController: NavController, videoId: String) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Mencari data video berdasarkan ID yang dilempar dari layar sebelumnya
    val materi = EdukasiData.daftarMateri.find { it.idVideo == videoId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(materi?.judul ?: "Detail Video") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White // Latar belakang putih bersih
    ) { paddingValues ->
        if (materi != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Pemutar Video Lebar Penuh (Tanpa padding pinggir)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black) // Latar hitam saat video sedang dimuat
                ) {
                    YouTubeVideoPlayer(videoId = materi.idVideo, lifecycleOwner = lifecycleOwner)
                }

                // Deskripsi di bawah video
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = materi.judul,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(Modifier, thickness = 2.dp, color = Color(0xFFF8F9FA))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = materi.deskripsi,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )
                }
            }
        } else {
            // Jika ID video tidak ditemukan
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Video tidak ditemukan.", color = Color.Gray)
            }
        }
    }
}