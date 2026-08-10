package com.jarimanis.jarimanis.ui.features.rapor

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star // <-- IMPORT ICON BINTANG
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarimanis.jarimanis.ui.features.dashboard.ZonaViewModel
import com.jarimanis.jarimanis.utils.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaporScreen(viewModel: ZonaViewModel, token: String) {
    val context = LocalContext.current
    val raporState by viewModel.raporState.collectAsState()

    // --- WARNA BACKGROUND UTAMA ---
    val offWhiteBackground = Color(0xFFF8F9FA)

    // --- MENGAMBIL TOTAL POIN SECARA DINAMIS DARI DATA RAPOR ---
    val totalPoints = (raporState as? Resource.Success)?.data?.data?.user?.totalSkor ?: 0

    // --- STATE TANGGAL ---
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }

    val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
            // Fetch ulang data ketika tanggal diubah
            if (token.isNotEmpty()) {
                viewModel.fetchRapor(token, apiDateFormat.format(selectedDate))
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = System.currentTimeMillis() } // Mencegah pilih hari esok

    // Otomatis mengambil data Rapor saat halaman ini pertama kali dibuka (Default: Hari ini)
    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) viewModel.fetchRapor(token, apiDateFormat.format(selectedDate))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rapor Kesehatanku", fontWeight = FontWeight.Bold) },
                actions = {
                    // --- TAMPILAN TOTAL POIN MIRIP DASHBOARD ---
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
                                tint = Color(0xFFFFD700), // Warna Emas
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
                // --- WARNA MENYATU DENGAN BACKGROUND ---
                colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)
            )
        },
        containerColor = offWhiteBackground // Menggunakan warna yang sama dengan TopAppBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // === FILTER TANGGAL ===
            Text("Pilih Tanggal Rapor:", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.DarkGray),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(displayDateFormat.format(selectedDate), fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = raporState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                }
                is Resource.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchRapor(token, apiDateFormat.format(selectedDate)) }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Coba Lagi")
                    }
                }
                is Resource.Success -> {
                    val data = state.data.data

                    // Menghitung berapa misi harian yang sudah selesai (Pre & Post test tidak dihitung untuk progress bar harian)
                    var selesaiHarian = 0
                    if (data.recallMakanan != null) selesaiHarian++
                    if (data.aktivitasFisik != null) selesaiHarian++
                    if (data.minumTtd != null) selesaiHarian++
                    if (data.personalHygiene != null) selesaiHarian++

                    val progress = selesaiHarian / 4f

                    // === KARTU PROGRES HARIAN ===
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Progress Zona Harian", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$selesaiHarian dari 4 Zona Selesai",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White
                            )
                        }
                    }

                    // === DAFTAR KARTU DETAIL ===

                    // 1. PRE-TEST
                    RaporItemCard(
                        title = "Pre-Test (Kuesioner Awal)",
                        isDone = data.preTest != null,
                        subLabel = if (data.preTest != null) "Skor: ${data.preTest.skor}" else "Belum Dikerjakan"
                    )

                    // 2. RECALL MAKANAN
                    RaporItemCard(
                        title = "Recall 24 Jam",
                        isDone = data.recallMakanan != null,
                        subLabel = if (data.recallMakanan != null) "Skor: ${data.recallMakanan.skorTotal} | Kategori: ${data.recallMakanan.kategori}" else "Belum Dikerjakan"
                    )

                    // 3. AKTIVITAS FISIK
                    RaporItemCard(
                        title = "Aktivitas Fisik",
                        isDone = data.aktivitasFisik != null,
                        subLabel = if (data.aktivitasFisik != null) "Durasi: ${data.aktivitasFisik.durasiMenit} Min | Skor: ${data.aktivitasFisik.skor} (${data.aktivitasFisik.kategori})" else "Belum Dikerjakan"
                    )

                    // 4. MINUM TTD
                    val statusTtd = if (data.minumTtd?.sudahMinum == 1) "Sudah Minum" else "Belum Minum"
                    RaporItemCard(
                        title = "Minum TTD",
                        isDone = data.minumTtd != null,
                        subLabel = if (data.minumTtd != null) "Skor: ${data.minumTtd.skor} | Keterangan: $statusTtd" else "Belum Dikerjakan"
                    )

                    // 5. PERSONAL HYGIENE
                    RaporItemCard(
                        title = "Personal Hygiene",
                        isDone = data.personalHygiene != null,
                        subLabel = if (data.personalHygiene != null) "Skor: ${data.personalHygiene.skorTotal} | Kategori: ${data.personalHygiene.kategori}" else "Belum Dikerjakan"
                    )

                    // 6. POST-TEST
                    RaporItemCard(
                        title = "Post-Test (Evaluasi Akhir)",
                        isDone = data.postTest != null,
                        subLabel = if (data.postTest != null) "Skor: ${data.postTest.skor}" else "Belum Memenuhi Syarat / Belum Dikerjakan"
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
                else -> {} // Idle
            }
        }
    }
}

// Komponen Card Item yang disempurnakan
@Composable
fun RaporItemCard(title: String, isDone: Boolean, subLabel: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDone) Color.DarkGray else Color.Red,
                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            if (isDone) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Selesai", tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
            } else {
                Icon(Icons.Filled.Warning, contentDescription = "Belum", tint = Color(0xFFFF9800), modifier = Modifier.size(32.dp))
            }
        }
    }
}