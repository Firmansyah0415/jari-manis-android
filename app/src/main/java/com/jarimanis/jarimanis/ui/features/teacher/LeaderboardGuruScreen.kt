package com.jarimanis.jarimanis.ui.features.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.ui.features.student.ZonaViewModel
import com.jarimanis.jarimanis.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardGuruScreen(
    authViewModel: AuthViewModel,
    zonaViewModel: ZonaViewModel,
    token: String
) {
    val siswaPoinList by authViewModel.siswaList.collectAsState() // Data Total Poin dari API guru/leaderboard
    val fisikState by zonaViewModel.leaderboardState.collectAsState() // Data Aktivitas Fisik

    var selectedTab by remember { mutableStateOf(0) } // 0 = Total Poin, 1 = Aktivitas Fisik
    var selectedKelasId by remember { mutableStateOf<Int?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val offWhiteBackground = Color(0xFFF8F9FA)

    // Mengekstrak daftar kelas unik dari siswa (Otomatis tanpa panggil API kelas lagi)
    val daftarKelasTersedia = remember(siswaPoinList) {
        siswaPoinList.mapNotNull { it.kelas }.distinctBy { it.id }.sortedBy { it.nama_kelas }
    }

    // Filter Lokal untuk Tab Total Poin
    val filteredPoinList = remember(siswaPoinList, selectedKelasId) {
        val list = if (selectedKelasId == null) siswaPoinList else siswaPoinList.filter { it.kelas?.id == selectedKelasId }
        // Urutkan ulang secara lokal untuk berjaga-jaga
        list.sortedByDescending { it.totalSkor ?: 0 }
    }

    // Trigger API Aktivitas Fisik (Hanya memanggil saat Tab Fisik dibuka atau Filter berubah)
    LaunchedEffect(selectedTab, selectedKelasId) {
        if (token.isNotEmpty() && selectedTab == 1) {
            zonaViewModel.fetchLeaderboardFisik(token = token, lingkup = null, kelasId = selectedKelasId)
        }
    }

    // Trigger awal untuk mengambil list Poin jika kosong
    LaunchedEffect(Unit) {
        if (token.isNotEmpty() && siswaPoinList.isEmpty()) {
            authViewModel.fetchSiswaList(token)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Papan Peringkat (Sekolah)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)
            )
        },
        containerColor = offWhiteBackground
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- 1. TABS (POIN vs FISIK) ---
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = offWhiteBackground,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f)) }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("🌟 Total Poin", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("🏃 Aktivitas Fisik", fontWeight = FontWeight.Bold) })
            }

            // --- 2. DROPDOWN FILTER KELAS ---
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    val labelTeks = daftarKelasTersedia.find { it.id == selectedKelasId }?.nama_kelas?.let { "Kelas $it" } ?: "🏆 Semua Kelas"
                    OutlinedTextField(
                        value = labelTeks,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                        DropdownMenuItem(
                            text = { Text("🏆 Semua Kelas (Satu Sekolah)", fontWeight = FontWeight.Bold) },
                            onClick = { selectedKelasId = null; expandedDropdown = false }
                        )
                        Divider()
                        daftarKelasTersedia.forEach { kelas ->
                            DropdownMenuItem(
                                text = { Text("Kelas ${kelas.nama_kelas}") },
                                onClick = { selectedKelasId = kelas.id; expandedDropdown = false }
                            )
                        }
                    }
                }
            }

            // --- 3. KONTEN LEADERBOARD SESUAI TAB ---
            if (selectedTab == 0) {
                // TAMPILAN TOTAL POIN
                if (filteredPoinList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada data poin.", color = Color.Gray) }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(filteredPoinList) { index, siswa ->
                            // Konversi model UserProfile ke struktur LeaderboardItem agar bisa pakai Card yang sama
                            val mappedItem = LeaderboardItem(
                                peringkat = index + 1,
                                id = siswa.id,
                                nama = siswa.name,
                                kelas = siswa.kelas?.nama_kelas ?: "-",
                                fotoProfil = siswa.fotoProfil,
                                totalSkor = siswa.totalSkor ?: 0,
                                totalMenit = 0 // Tidak relevan untuk tab ini, disembunyikan di UI
                            )
                            GuruLeaderboardCard(item = mappedItem, isFisik = false)
                        }
                    }
                }
            } else {
                // TAMPILAN AKTIVITAS FISIK
                when (val state = fisikState) {
                    is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = Color.Red) }
                    is Resource.Success -> {
                        if (state.data.data.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada data olahraga di kelas ini.", color = Color.Gray) }
                        } else {
                            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(state.data.data) { item ->
                                    GuruLeaderboardCard(item = item, isFisik = true)
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

// Desain Kartu Universal untuk Guru (Mirip Siswa tapi fleksibel untuk Poin/Fisik)
@Composable
fun GuruLeaderboardCard(item: LeaderboardItem, isFisik: Boolean) {
    val isTop3 = item.peringkat <= 3
    val backgroundColor = when (item.peringkat) {
        1 -> Color(0xFFFFF8E1) // Emas pudar
        2 -> Color(0xFFF5F5F5) // Perak pudar
        3 -> Color(0xFFFFF3E0) // Perunggu pudar
        else -> Color.White
    }
    val medalIcon = when (item.peringkat) {
        1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "${item.peringkat}"
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(
            elevation = if (isTop3) 6.dp else 2.dp,
            shape = RoundedCornerShape(16.dp),
            spotColor = if (isTop3) Color(0xFFFFD700).copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f),
            ambientColor = Color.Transparent
        ).clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                if (isTop3) Text(text = medalIcon, fontSize = 28.sp)
                else Text(text = medalIcon, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                if (!item.fotoProfil.isNullOrEmpty()) {
                    AsyncImage(model = "${ApiClient.BASE_URL}profil/${item.fotoProfil}", contentDescription = "Foto Siswa", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Filled.AccountCircle, "Avatar", tint = Color.Gray, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray, maxLines = 1)
                Text(text = "Kelas: ${item.kelas}", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = "Skor", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${item.totalSkor}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                if (isFisik) {
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
}