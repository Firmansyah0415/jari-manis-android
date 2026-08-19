package com.jarimanis.jarimanis.ui.features.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

// Data Class untuk menyimpan daftar materi
data class MateriEdukasi(
    val idVideo: String,
    val judul: String,
    val deskripsi: String
)

// Object ini bertindak sebagai "Database Sementara" agar datanya bisa dibaca di layar Detail
object EdukasiData {
    val daftarMateri = listOf(
        MateriEdukasi("MIrQJNfGKrA", "Apa itu Anemia?", "Kenali gejala dan bahaya kurang darah merah bagi remaja."),
        MateriEdukasi("JaWAXzDYeG8", "Pentingnya Tablet Tambah Darah (TTD)", "Mengapa meminum TTD sangat penting untuk menjaga konsentrasi belajar?"),
        MateriEdukasi("3e2SZB6zzaA", "Isi Piringku & Gizi Seimbang", "Panduan mengatur porsi makan harian yang sehat dan bergizi.")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdukasiScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modul Edukasi Kesehatan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA),
        // PERBAIKAN 1: Tambahkan ini agar layar mengakomodasi UI Navigasi bawaan HP (gestur swipe bawah)
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            // PERBAIKAN 2: Gunakan contentPadding untuk memberikan jarak di DALAM daftar gulir
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 48.dp // Beri ruang napas yang ekstra di bagian bawah agar item terakhir bisa naik
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Tonton video edukasi di bawah ini untuk menambah wawasanmu seputar gizi dan kesehatan!",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(EdukasiData.daftarMateri) { materi ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.medium, // Menggunakan bentuk melengkung bawaan Material 3
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("detail_edukasi/${materi.idVideo}") }
                ) {
                    Column {
                        // Thumbnail Video dari YouTube
                        AsyncImage(
                            model = "https://img.youtube.com/vi/${materi.idVideo}/hqdefault.jpg",
                            contentDescription = "Thumbnail ${materi.judul}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            contentScale = ContentScale.Crop
                        )

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = materi.judul,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = materi.deskripsi,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}