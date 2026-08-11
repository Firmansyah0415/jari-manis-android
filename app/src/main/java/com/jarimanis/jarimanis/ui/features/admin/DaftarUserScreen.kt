package com.jarimanis.jarimanis.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jarimanis.jarimanis.data.model.UserProfile
import com.jarimanis.jarimanis.data.network.ApiClient
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.utils.Resource
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarUserScreen(
    viewModel: AuthViewModel,
    token: String
) {
    val userListState by viewModel.adminUserList.collectAsState()
    val sekolahList by viewModel.sekolahList.collectAsState()
    val kelasList by viewModel.kelasList.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val roles = listOf("siswa", "guru")

    var selectedSekolahId by remember { mutableStateOf<Int?>(null) }
    var selectedSekolahName by remember { mutableStateOf("Semua Sekolah") }
    var selectedKelasId by remember { mutableStateOf<Int?>(null) }
    var selectedKelasName by remember { mutableStateOf("Semua Kelas") }

    var expandedSekolah by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) }

    // Fungsi helper untuk menarik data ulang berdasarkan Tab dan Filter aktif
    fun refreshData() {
        viewModel.fetchAdminUsers(token, role = roles[selectedTabIndex], sekolahId = selectedSekolahId, kelasId = selectedKelasId)
    }

    val offWhiteBackground = Color(0xFFF8F9FA)

    LaunchedEffect(selectedTabIndex) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column { Text("Manajemen User", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)
            )
        },
        containerColor = offWhiteBackground
    ) { paddingValues ->

        // --- PERBAIKAN SCROLL: Menggunakan LazyColumn sebagai Root ---
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            item {
                // --- TAB ROW (SISWA / GURU) MATERIAL 3 TERBARU ---
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Daftar Siswa", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Daftar Guru", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            item {
                // === FILTER DROPDOWN MATERIAL 3 ===
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FilterAlt, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Filter Pencarian", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // DROPDOWN SEKOLAH
                            ExposedDropdownMenuBox(
                                expanded = expandedSekolah,
                                onExpandedChange = { expandedSekolah = !expandedSekolah },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedSekolahName,
                                    onValueChange = {}, readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSekolah) },
                                    modifier = Modifier.menuAnchor(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp), singleLine = true
                                )
                                ExposedDropdownMenu(expanded = expandedSekolah, onDismissRequest = { expandedSekolah = false }) {
                                    DropdownMenuItem(text = { Text("Semua Sekolah", fontSize = 14.sp) }, onClick = {
                                        selectedSekolahId = null; selectedSekolahName = "Semua Sekolah"
                                        selectedKelasId = null; selectedKelasName = "Semua Kelas"
                                        expandedSekolah = false; refreshData()
                                    })
                                    sekolahList.forEach { sekolah ->
                                        DropdownMenuItem(text = { Text(sekolah.nama, fontSize = 14.sp) }, onClick = {
                                            selectedSekolahId = sekolah.id; selectedSekolahName = sekolah.nama
                                            selectedKelasId = null; selectedKelasName = "Semua Kelas"
                                            expandedSekolah = false; viewModel.fetchKelas(sekolah.id); refreshData()
                                        })
                                    }
                                }
                            }

                            // DROPDOWN KELAS
                            ExposedDropdownMenuBox(
                                expanded = expandedKelas,
                                onExpandedChange = { if (selectedSekolahId != null) expandedKelas = !expandedKelas },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedKelasName,
                                    onValueChange = {}, readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                                    modifier = Modifier.menuAnchor(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp), enabled = selectedSekolahId != null, singleLine = true
                                )
                                ExposedDropdownMenu(expanded = expandedKelas, onDismissRequest = { expandedKelas = false }) {
                                    DropdownMenuItem(text = { Text("Semua Kelas", fontSize = 14.sp) }, onClick = {
                                        selectedKelasId = null; selectedKelasName = "Semua Kelas"
                                        expandedKelas = false; refreshData()
                                    })
                                    kelasList.forEach { kelas ->
                                        DropdownMenuItem(text = { Text(kelas.nama_kelas, fontSize = 14.sp) }, onClick = {
                                            selectedKelasId = kelas.id; selectedKelasName = kelas.nama_kelas
                                            expandedKelas = false; refreshData()
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // === LIST USER & STATISTIK JUMLAH ===
            when (val state = userListState) {
                is Resource.Loading -> {
                    item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                }
                is Resource.Error -> {
                    item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text(state.message, color = Color.Red) } }
                }
                is Resource.Success -> {
                    val users = state.data.data

                    item {
                        // --- MENAMPILKAN STATISTIK TOTAL USER DI SINI ---
                        val roleName = roles[selectedTabIndex].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                        Text(
                            text = "Total $roleName: ${users.size} Akun",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (users.isEmpty()) {
                        item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("Belum ada data ${roles[selectedTabIndex]}", color = Color.Gray) } }
                    } else {
                        // Looping item langsung di dalam LazyColumn Root
                        items(users) { user ->
                            UserItemCard(user = user, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun UserItemCard(user: UserProfile, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                if (!user.fotoProfil.isNullOrEmpty()) {
                    AsyncImage(model = "${ApiClient.BASE_URL}storage/profil/${user.fotoProfil}", contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "Username: @${user.username}", fontSize = 12.sp, color = Color.DarkGray)
                Text(text = "${user.sekolah?.nama ?: "-"} | Kelas: ${user.kelas?.nama_kelas ?: "-"}", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}