package com.jarimanis.jarimanis.ui.features.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jarimanis.jarimanis.data.model.UserProfile
import com.jarimanis.jarimanis.data.network.ApiClient
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardGuruScreen(
    viewModel: AuthViewModel,
    token: String,
    onSiswaClick: (Int) -> Unit
) {
    val siswaList by viewModel.siswaList.collectAsState()
    val isLoading by viewModel.isSiswaLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var selectedKelasId by remember { mutableStateOf<Int?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val daftarKelasTersedia = remember(siswaList) {
        siswaList.mapNotNull { it.kelas }.distinctBy { it.id }.sortedBy { it.nama_kelas }
    }

    val filteredSiswaList = remember(siswaList, selectedKelasId) {
        if (selectedKelasId == null) siswaList else siswaList.filter { it.kelas?.id == selectedKelasId }
    }

    // --- LOGIKA WAKTU UNTUK GREETING ---
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Selamat Pagi"
        in 12..14 -> "Selamat Siang"
        in 15..17 -> "Selamat Sore"
        else -> "Selamat Malam"
    }

    val offWhiteBackground = Color(0xFFF8F9FA)

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            viewModel.fetchSiswaList(token)
            viewModel.fetchProfile(token)
        }
    }

    // GANTI BLOK SCAFFOLD MENJADI INI
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dashboard Guru", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Halo ${userProfile?.name ?: "Guru"}, $greeting", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                // Menggunakan warna offWhite persis seperti dashboard siswa
                colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)
            )
        },
        containerColor = offWhiteBackground
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    viewModel.fetchSiswaList(token)
                    delay(1000.milliseconds)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // KOTAK BIRU GREETING LAMA SUDAH DIHAPUS DI SINI

                // --- UI DROPDOWN FILTER KELAS ---
                if (siswaList.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedDropdown,
                            onExpandedChange = { expandedDropdown = !expandedDropdown }
                        ) {
                            val labelTeks = daftarKelasTersedia.find { it.id == selectedKelasId }?.nama_kelas ?: "Semua Kelas (Tingkat Sekolah)"
                            OutlinedTextField(
                                value = labelTeks,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter Berdasarkan Kelas") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Semua Kelas (Tingkat Sekolah)", fontWeight = FontWeight.Bold) },
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
                }

                // --- KONTEN LEADERBOARD ---
                if (isLoading && !isRefreshing) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredSiswaList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Belum ada data siswa di kelas ini.", color = Color.Gray)
                    }
                } else {
                    LeaderboardContent(filteredSiswaList, onSiswaClick)
                }
            }
        }
    }
}

@Composable
fun LeaderboardContent(siswaList: List<UserProfile>, onSiswaClick: (Int) -> Unit) {
    val top3 = siswaList.take(3)
    val remainingSiswa = siswaList.drop(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), contentAlignment = Alignment.BottomCenter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (top3.size > 1) {
                        PodiumItem(siswa = top3[1], rank = 2, height = 120.dp, color = Color(0xFFC0C0C0), onClick = onSiswaClick)
                    }
                    if (top3.isNotEmpty()) {
                        PodiumItem(siswa = top3[0], rank = 1, height = 160.dp, color = Color(0xFFFFD700), onClick = onSiswaClick)
                    }
                    if (top3.size > 2) {
                        PodiumItem(siswa = top3[2], rank = 3, height = 90.dp, color = Color(0xFFCD7F32), onClick = onSiswaClick)
                    }
                }
            }
        }

        if (remainingSiswa.isNotEmpty()) {
            item {
                Text(
                    text = "Peringkat Lainnya",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    textAlign = TextAlign.Start
                )
            }
            itemsIndexed(remainingSiswa) { index, siswa ->
                ItemSiswaCard(siswa = siswa, rank = index + 4, onClick = { onSiswaClick(siswa.id) })
            }
        }
    }
}

@Composable
fun RowScope.PodiumItem(siswa: UserProfile, rank: Int, height: Dp, color: Color, onClick: (Int) -> Unit) {
    Column(
        modifier = Modifier.weight(1f).clickable { onClick(siswa.id) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            if (!siswa.fotoProfil.isNullOrEmpty()) {
                AsyncImage(
                    model = "${ApiClient.BASE_URL}storage/profil/${siswa.fotoProfil}",
                    contentDescription = "Foto Peringkat $rank",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(50.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = siswa.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = siswa.kelas?.nama_kelas ?: "-", style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = "${siswa.totalSkor ?: 0} Poin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(height).padding(horizontal = 4.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(color),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(text = "$rank", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun ItemSiswaCard(siswa: UserProfile, rank: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "$rank", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                if (!siswa.fotoProfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = "${ApiClient.BASE_URL}storage/profil/${siswa.fotoProfil}",
                        contentDescription = "Foto Siswa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = siswa.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "Kelas: ${siswa.kelas?.nama_kelas ?: "-"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${siswa.totalSkor ?: 0}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text(text = "Poin", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}