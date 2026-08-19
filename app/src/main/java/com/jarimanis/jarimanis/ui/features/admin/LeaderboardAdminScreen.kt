package com.jarimanis.jarimanis.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterAlt
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
fun LeaderboardAdminScreen(
    authViewModel: AuthViewModel,
    zonaViewModel: ZonaViewModel,
    token: String,
    onSiswaClick: (Int) -> Unit
) {
    val poinState by authViewModel.adminDashboardState.collectAsState()
    val fisikState by zonaViewModel.leaderboardState.collectAsState()
    val sekolahList by authViewModel.sekolahList.collectAsState()
    val kelasList by authViewModel.kelasList.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSekolahId by remember { mutableStateOf<Int?>(null) }
    var selectedSekolahName by remember { mutableStateOf("Semua Sekolah") }
    var selectedKelasId by remember { mutableStateOf<Int?>(null) }
    var selectedKelasName by remember { mutableStateOf("Semua Kelas") }
    var expandedSekolah by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) }

    val offWhiteBackground = Color(0xFFF8F9FA)

    // Fungsi pemicu pemuatan data
    fun refreshData() {
        if (selectedTab == 0) {
            authViewModel.fetchAdminDashboard(token, selectedSekolahId, selectedKelasId)
        } else {
            zonaViewModel.fetchLeaderboardFisik(token, lingkup = null, sekolahId = selectedSekolahId, kelasId = selectedKelasId)
        }
    }

    LaunchedEffect(selectedTab, selectedSekolahId, selectedKelasId) {
        refreshData()
    }
    LaunchedEffect(Unit) {
        if (sekolahList.isEmpty()) authViewModel.fetchSekolah()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Papan Peringkat (Global)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)
            )
        },
        containerColor = offWhiteBackground
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- TABS (POIN vs FISIK) ---
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

            // --- FILTER SEKOLAH & KELAS ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FilterAlt, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Filter Area", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(expanded = expandedSekolah, onExpandedChange = { expandedSekolah = !expandedSekolah }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedSekolahName, onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSekolah) },
                                modifier = Modifier.menuAnchor(), textStyle = LocalTextStyle.current.copy(fontSize = 12.sp), singleLine = true
                            )
                            ExposedDropdownMenu(expanded = expandedSekolah, onDismissRequest = { expandedSekolah = false }) {
                                DropdownMenuItem(text = { Text("Semua Sekolah", fontSize = 14.sp) }, onClick = {
                                    selectedSekolahId = null; selectedSekolahName = "Semua Sekolah"
                                    selectedKelasId = null; selectedKelasName = "Semua Kelas"
                                    expandedSekolah = false
                                })
                                sekolahList.forEach { sekolah ->
                                    DropdownMenuItem(text = { Text(sekolah.nama, fontSize = 14.sp) }, onClick = {
                                        selectedSekolahId = sekolah.id; selectedSekolahName = sekolah.nama
                                        selectedKelasId = null; selectedKelasName = "Semua Kelas"
                                        expandedSekolah = false; authViewModel.fetchKelas(sekolah.id)
                                    })
                                }
                            }
                        }
                        ExposedDropdownMenuBox(expanded = expandedKelas, onExpandedChange = { if (selectedSekolahId != null) expandedKelas = !expandedKelas }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedKelasName, onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                                modifier = Modifier.menuAnchor(), textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                enabled = selectedSekolahId != null, singleLine = true
                            )
                            ExposedDropdownMenu(expanded = expandedKelas, onDismissRequest = { expandedKelas = false }) {
                                DropdownMenuItem(text = { Text("Semua Kelas", fontSize = 14.sp) }, onClick = {
                                    selectedKelasId = null; selectedKelasName = "Semua Kelas"; expandedKelas = false
                                })
                                kelasList.forEach { kelas ->
                                    DropdownMenuItem(text = { Text(kelas.nama_kelas, fontSize = 14.sp) }, onClick = {
                                        selectedKelasId = kelas.id; selectedKelasName = kelas.nama_kelas; expandedKelas = false
                                    })
                                }
                            }
                        }
                    }
                }
            }

            // --- KONTEN DATA ---
            if (selectedTab == 0) {
                // TAMPILAN POIN
                when (val state = poinState) {
                    is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = Color.Red) }
                    is Resource.Success -> {
                        val leaderboard = state.data.data.leaderboard
                        if (leaderboard.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada data siswa.", color = Color.Gray) }
                        } else {
                            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                                itemsIndexed(leaderboard) { index, siswa ->
                                    val mappedItem = LeaderboardItem(
                                        peringkat = index + 1, id = siswa.id, nama = siswa.name,
                                        kelas = "${siswa.sekolah?.nama ?: "-"} | ${siswa.kelas?.nama_kelas ?: "-"}",
                                        fotoProfil = siswa.fotoProfil, totalSkor = siswa.totalSkor ?: 0, totalMenit = 0
                                    )
                                    AdminLeaderboardCard(item = mappedItem, isFisik = false, onClick = { onSiswaClick(siswa.id) }, modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                    else -> {}
                }
            } else {
                // TAMPILAN FISIK
                when (val state = fisikState) {
                    is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = Color.Red) }
                    is Resource.Success -> {
                        val data = state.data.data
                        if (data.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada data olahraga.", color = Color.Gray) }
                        } else {
                            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                                items(data) { item ->
                                    AdminLeaderboardCard(item = item, isFisik = true, onClick = { onSiswaClick(item.id) }, modifier = Modifier.padding(horizontal = 16.dp))
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

@Composable
fun AdminLeaderboardCard(item: LeaderboardItem, isFisik: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isTop3 = item.peringkat <= 3
    val bgColor = when (item.peringkat) {
        1 -> Color(0xFFFFF8E1); 2 -> Color(0xFFF5F5F5); 3 -> Color(0xFFFFF3E0); else -> Color.White
    }
    val medalIcon = when (item.peringkat) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "${item.peringkat}" }

    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() }.shadow(
            elevation = if (isTop3) 6.dp else 1.dp, shape = RoundedCornerShape(12.dp),
            spotColor = if (isTop3) Color(0xFFFFD700).copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f), ambientColor = Color.Transparent
        ).clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                if (isTop3) Text(medalIcon, fontSize = 24.sp)
                else Text(medalIcon, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                if (!item.fotoProfil.isNullOrEmpty()) {
                    AsyncImage(model = "${ApiClient.BASE_URL}profil/${item.fotoProfil}", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Filled.AccountCircle, null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nama, fontWeight = FontWeight.Bold, color = Color.DarkGray, maxLines = 1)
                Text(item.kelas, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = "Skor", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${item.totalSkor}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                if (isFisik) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = "Menit", tint = Color(0xFF2196F3), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${item.totalMenit} m", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}