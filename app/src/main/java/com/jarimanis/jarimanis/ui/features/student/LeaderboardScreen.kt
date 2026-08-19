package com.jarimanis.jarimanis.ui.features.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jarimanis.jarimanis.data.network.ApiClient
import com.jarimanis.jarimanis.data.network.LeaderboardItem
import com.jarimanis.jarimanis.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: ZonaViewModel,
    token: String
) {
    val leaderboardState by viewModel.leaderboardState.collectAsState()
    val offWhiteBackground = Color(0xFFF8F9FA)
    val raporState by viewModel.raporState.collectAsState()
    val totalPoints = (raporState as? Resource.Success)?.data?.data?.user?.totalSkor ?: 0

    // --- STATE FILTER (sekolah atau kelas) ---
    var selectedLingkup by remember { mutableStateOf("sekolah") }

    // Otomatis tarik data saat halaman dibuka ATAU saat filter berubah
    LaunchedEffect(selectedLingkup) {
        if (token.isNotEmpty()) {
            viewModel.fetchLeaderboardFisik(token, selectedLingkup)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Aktivitas Fisik", fontWeight = FontWeight.Bold) },
                // Hapus navigationIcon panah kembali jika ini ada di Bottom Nav
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = "Poin", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$totalPoints Poin", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)
            )
        },
        containerColor = offWhiteBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- UI TOMBOL FILTER (TABS) ---
            SecondaryTabRow(
                selectedTabIndex = if (selectedLingkup == "sekolah") 0 else 1,
                containerColor = offWhiteBackground,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(if (selectedLingkup == "sekolah") 0 else 1),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            ) {
                Tab(
                    selected = selectedLingkup == "sekolah",
                    onClick = { selectedLingkup = "sekolah" },
                    text = { Text("Satu Sekolah", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedLingkup == "kelas",
                    onClick = { selectedLingkup = "kelas" },
                    text = { Text("Hanya Kelasku", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- KONTEN LEADERBOARD ---
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = leaderboardState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is Resource.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.fetchLeaderboardFisik(token, selectedLingkup) }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                    is Resource.Success -> {
                        val data = state.data.data
                        if (data.isEmpty()) {
                            Text(
                                text = "Belum ada siswa yang berolahraga di kategori ini.\nAyo jadilah yang pertama!",
                                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Text(
                                        text = if (selectedLingkup == "sekolah") "🏆 Peringkat Aktif (Sekolah)" else "🏆 Peringkat Aktif (Kelas)",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                items(data) { item ->
                                    LeaderboardItemCard(item = item)
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

// ... (Biarkan fungsi LeaderboardItemCard sama persis seperti sebelumnya) ...
@Composable
fun LeaderboardItemCard(item: LeaderboardItem) {
    // Penentuan desain berdasarkan Peringkat
    val isTop3 = item.peringkat <= 3

    val backgroundColor = when (item.peringkat) {
        1 -> Color(0xFFFFF8E1) // Emas pudar
        2 -> Color(0xFFF5F5F5) // Perak pudar
        3 -> Color(0xFFFFF3E0) // Perunggu pudar
        else -> Color.White
    }

    val medalIcon = when (item.peringkat) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "${item.peringkat}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isTop3) 6.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isTop3) Color(0xFFFFD700).copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f),
                ambientColor = Color.Transparent
            )
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 1. ANGKA PERINGKAT / MEDALI ---
            Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                if (isTop3) {
                    Text(text = medalIcon, fontSize = 28.sp)
                } else {
                    Text(text = medalIcon, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // --- 2. FOTO PROFIL ---
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.fotoProfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = "${ApiClient.BASE_URL}profil/${item.fotoProfil}",
                        contentDescription = "Foto Siswa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.AccountCircle, "Avatar", tint = Color.Gray, modifier = Modifier.size(40.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- 3. NAMA & KELAS ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nama,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    maxLines = 1
                )
                Text(
                    text = "Kelas: ${item.kelas}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // --- 4. SKOR TOTAL & MENIT ---
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = "Skor", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${item.totalSkor}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, contentDescription = "Menit", tint = Color(0xFF2196F3), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${item.totalMenit} m", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}