package com.jarimanis.jarimanis.ui.features.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox // <-- IMPORT API TERBARU
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    userName: String = "Siswa",
    totalPoints: Int = 0,
    totalHariAktif: Int = 0,
    isPostTestDone: Boolean = false,
    onRefreshRequest: () -> Unit = {}
) {
    val offWhiteBackground = Color(0xFFF8F9FA)
    val coroutineScope = rememberCoroutineScope()

    // --- STATE PULL TO REFRESH (Versi 1.3.0+) ---
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Jari Manis", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Halo, $userName!", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Poin",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalPoints Poin",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)
            )
        },
        containerColor = offWhiteBackground
    ) { paddingValues ->

        // --- PENGGUNAAN API BARU PullToRefreshBox ---
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    onRefreshRequest() // Tarik data dari API
                    delay(1000.milliseconds) // Animasi berputar minimal 1 detik
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pilih Zona Aktivitas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ModulEdukasiCard { navController.navigate("edukasi_route") }
                    }

                    item { MenuCard("Recall\n24 Jam", Icons.Default.ShoppingCart, Color(0xFF4CAF50)) { navController.navigate("recall_makanan_route") } }
                    item { MenuCard("Aktivitas\nFisik", Icons.AutoMirrored.Filled.DirectionsRun, Color(0xFFFF9800)) { navController.navigate("aktivitas_fisik_route") } }
                    item { MenuCard("Minum\nTTD", Icons.Default.Favorite, Color(0xFFE91E63)) { navController.navigate("minum_ttd_route") } }
                    item { MenuCard("Personal\nHygiene", Icons.Default.Face, Color(0xFF2196F3)) { navController.navigate("personal_hygiene_route") } }
                    // --- KARTU POST-TEST (Membentang penuh di paling bawah) ---
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        PostTestCard(
                            totalHariAktif = totalHariAktif,
                            isPostTestDone = isPostTestDone,
                            onClick = {
                                // Jangan navigasi jika sudah dikerjakan
                                if (totalHariAktif >= 5 && !isPostTestDone) {
                                    navController.navigate("post_test_route")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ===============================================
// KOMPONEN-KOMPONEN KARTU (TIDAK ADA PERUBAHAN)
// ===============================================

@Composable
fun MenuCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
        }
    }
}

@Composable
fun ModulEdukasiCard(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Edukasi", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Modul Edukasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = "Tonton video seputar gizi & kesehatan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Masuk", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

// --- TAMBAHKAN KOMPONEN KARTU INI DI LUAR FUNGSI DASHBOARD ---
@Composable
fun PostTestCard(totalHariAktif: Int, isPostTestDone: Boolean, onClick: () -> Unit) { // Tambah parameter isPostTestDone
    val isUnlocked = totalHariAktif >= 5
    val progress = (totalHariAktif / 5f).coerceIn(0f, 1f)
    val sisaHari = if (5 - totalHariAktif > 0) 5 - totalHariAktif else 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.secondaryContainer else Color.LightGray.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 0.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isUnlocked) MaterialTheme.colorScheme.secondary else Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    // LOGIKA IKON BARU:
                    // 1. Jika belum 5 hari -> Gembok Terkunci
                    // 2. Jika sudah 5 hari & SUDAH dikerjakan -> Centang
                    // 3. Jika sudah 5 hari & BELUM dikerjakan -> Gembok Terbuka
                    val iconToDisplay = when {
                        !isUnlocked -> Icons.Filled.Lock
                        isPostTestDone -> Icons.Filled.CheckCircle
                        else -> Icons.Filled.LockOpen
                    }

                    Icon(iconToDisplay, contentDescription = "Post Test", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Evaluasi Akhir (Post-Test)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    // LOGIKA TEKS BARU:
                    if (!isUnlocked) {
                        Text("Misi Harian: $totalHariAktif/5 Selesai", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                    } else if (isPostTestDone) {
                        Text("Misi Final Selesai! Terima kasih.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    } else {
                        Text("Tersedia! Klik untuk mengerjakan misi final.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.LightGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Selesaikan $sisaHari hari lagi untuk membuka Post-Test!", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}