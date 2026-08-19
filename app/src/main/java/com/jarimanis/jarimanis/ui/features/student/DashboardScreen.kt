package com.jarimanis.jarimanis.ui.features.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    // --- PARAMETER BARU UNTUK TES KEBUGARAN ---
    isPreTestKebugaranDone: Boolean = false,
    isPostTestKebugaranDone: Boolean = false,

    onRefreshRequest: () -> Unit = {}
) {
    val offWhiteBackground = Color(0xFFF8F9FA)
    val coroutineScope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onRefreshRequest()
    }

    Scaffold(
        topBar = {
            // JANGAN DIUBAH (Sesuai Permintaan)
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

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    onRefreshRequest()
                    delay(1000.milliseconds)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // MENGGUNAKAN LAZYCOLUMN AGAR PENGELOMPOKAN LEBIH JELAS
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp) // Jarak antar kelompok
            ) {

                // ==========================================
                // KELOMPOK 1: ZONA HARIAN
                // ==========================================
                item {
                    Column {
                        SectionTitle("Misi Harian Anda")
                        Spacer(modifier = Modifier.height(12.dp))

                        // Baris 1: Recall & Fisik
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MenuCard("Recall\n24 Jam", Icons.Default.ShoppingCart, Color(0xFF4CAF50), modifier = Modifier.weight(1f)) { navController.navigate("recall_makanan_route") }
                            MenuCard("Aktivitas\nFisik", Icons.AutoMirrored.Filled.DirectionsRun, Color(0xFFFF9800), modifier = Modifier.weight(1f)) { navController.navigate("aktivitas_fisik_route") }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Baris 2: TTD & Hygiene
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MenuCard("Minum\nTTD", Icons.Default.Favorite, Color(0xFFE91E63), modifier = Modifier.weight(1f)) { navController.navigate("minum_ttd_route") }
                            MenuCard("Personal\nHygiene", Icons.Default.Face, Color(0xFF2196F3), modifier = Modifier.weight(1f)) { navController.navigate("personal_hygiene_route") }
                        }
                    }
                }

                // ==========================================
                // KELOMPOK 2: PUSAT PEMBELAJARAN
                // ==========================================
                item {
                    Column {
                        SectionTitle("Pusat Pembelajaran")
                        Spacer(modifier = Modifier.height(12.dp))
                        ModulEdukasiCard { navController.navigate("edukasi_route") }
                    }
                }

                // ==========================================
                // KELOMPOK 3: EVALUASI & PENGUKURAN
                // ==========================================
                item {
                    Column {
                        SectionTitle("Evaluasi & Pengukuran")
                        Spacer(modifier = Modifier.height(12.dp))

                        TesKebugaranCard(
                            isPreTestDone = isPreTestKebugaranDone,
                            isPostTestDone = isPostTestKebugaranDone,
                            onPreTestClick = { navController.navigate("tes_kebugaran/pre") },
                            onPostTestClick = { navController.navigate("tes_kebugaran/post") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PostTestCard(
                            totalHariAktif = totalHariAktif,
                            isPostTestDone = isPostTestDone,
                            onClick = {
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
// KOMPONEN-KOMPONEN KARTU
// ===============================================

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.DarkGray
    )
}

@Composable
fun MenuCard(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.aspectRatio(1f).clickable { onClick() }
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

// --- KOMPONEN KARTU TES KEBUGARAN (DENGAN VALIDASI IKON) ---
@Composable
fun TesKebugaranCard(
    isPreTestDone: Boolean,
    isPostTestDone: Boolean,
    onPreTestClick: () -> Unit,
    onPostTestClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE0F7FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.FitnessCenter, contentDescription = "Kebugaran", tint = Color(0xFF00BCD4))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Pengukuran Kebugaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Ukur kondisi fisikmu secara berkala", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                // TOMBOL PRE-TEST
                OutlinedButton(
                    onClick = onPreTestClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        // Jika sudah diisi, ubah warna agar terlihat 'Selesai'
                        contentColor = if (isPreTestDone) Color(0xFF4CAF50) else Color(0xFF00BCD4),
                        containerColor = if (isPreTestDone) Color(0xFFE8F5E9) else Color.Transparent
                    ),
                    border = BorderStroke(1.dp, if (isPreTestDone) Color(0xFF4CAF50) else Color(0xFF00BCD4))
                ) {
                    if (isPreTestDone) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Selesai", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pre-Test")
                    } else {
                        Text("Isi Pre-Test")
                    }
                }

                // TOMBOL POST-TEST
                Button(
                    onClick = onPostTestClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPostTestDone) Color(0xFF4CAF50) else Color(0xFF00BCD4)
                    )
                ) {
                    if (isPostTestDone) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Selesai", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Post-Test")
                    } else {
                        Text("Isi Post-Test")
                    }
                }
            }
        }
    }
}

@Composable
fun PostTestCard(totalHariAktif: Int, isPostTestDone: Boolean, onClick: () -> Unit) {
    val isUnlocked = totalHariAktif >= 5
    val progress = (totalHariAktif / 5f).coerceIn(0f, 1f)
    val sisaHari = if (5 - totalHariAktif > 0) 5 - totalHariAktif else 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.secondaryContainer else Color.LightGray.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable(enabled = isUnlocked && !isPostTestDone) { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isUnlocked) MaterialTheme.colorScheme.secondary else Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    val iconToDisplay = when {
                        !isUnlocked -> Icons.Filled.Lock
                        isPostTestDone -> Icons.Filled.CheckCircle
                        else -> Icons.Filled.LockOpen
                    }
                    Icon(iconToDisplay, contentDescription = "Post Test", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Evaluasi Akhir Kuesioner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

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