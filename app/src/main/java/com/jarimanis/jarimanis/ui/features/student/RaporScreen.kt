package com.jarimanis.jarimanis.ui.features.student

import android.app.DatePickerDialog
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow // <-- TAMBAHAN IMPORT UNTUK SHADOW
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarimanis.jarimanis.data.model.RaporItem
import com.jarimanis.jarimanis.utils.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaporScreen(viewModel: ZonaViewModel, token: String) {
    val context = LocalContext.current
    val raporState by viewModel.raporState.collectAsState()

    val offWhiteBackground = Color(0xFFF8F9FA)
    val totalPoints = (raporState as? Resource.Success)?.data?.data?.user?.totalSkor ?: 0

    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
            if (token.isNotEmpty()) viewModel.fetchRapor(token, apiDateFormat.format(selectedDate))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) viewModel.fetchRapor(token, apiDateFormat.format(selectedDate))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rapor Kesehatanku", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
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
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                is Resource.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchRapor(token, apiDateFormat.format(selectedDate)) }, modifier = Modifier.padding(top = 8.dp)) { Text("Coba Lagi") }
                }
                is Resource.Success -> {
                    val data = state.data.data
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
                            Text("$selesaiHarian dari 4 Zona Selesai", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondaryContainer)
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)), color = MaterialTheme.colorScheme.secondaryContainer, trackColor = Color.White)
                        }
                    }

                    // 1. PRE-TEST
                    AdvancedRaporCard(title = "Pre-Test (Kuesioner Awal)", data = data.preTest)

                    // 2. RECALL MAKANAN (Dengan Dropdown)
                    AdvancedRaporCard(
                        title = "Recall 24 Jam",
                        data = data.recallMakanan,
                        isRecall = true // Memicu UI Dropdown
                    )

                    // 3. AKTIVITAS FISIK (Nama, Ikon Jam, Ikon Bintang)
                    AdvancedRaporCard(title = "Aktivitas Fisik", data = data.aktivitasFisik)

                    // 4. MINUM TTD
                    AdvancedRaporCard(
                        title = "Minum TTD",
                        data = data.minumTtd,
                        customStatus = if (data.minumTtd?.sudahMinum == 1) "Sudah Minum TTD" else "Belum Minum TTD"
                    )

                    // 5. PERSONAL HYGIENE (Dengan Dropdown)
                    AdvancedRaporCard(
                        title = "Personal Hygiene",
                        data = data.personalHygiene,
                        isHygiene = true // Memicu UI Dropdown
                    )

                    // 6. POST-TEST
                    AdvancedRaporCard(title = "Post-Test (Evaluasi Akhir)", data = data.postTest, isPostTest = true)

                    Spacer(modifier = Modifier.height(32.dp))
                }
                else -> {}
            }
        }
    }
}

// =========================================================
// KARTU ITEM RAPOR YANG SANGAT CERDAS & FLEKSIBEL
// =========================================================
@Composable
fun AdvancedRaporCard(
    title: String,
    data: RaporItem?,
    customStatus: String? = null,
    isRecall: Boolean = false,
    isHygiene: Boolean = false,
    isPostTest: Boolean = false
) {
    val isDone = data != null
    val isExpandable = isDone && (isRecall || isHygiene)
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Jarak antar kartu sedikit dijauhkan
            // --- PERBAIKAN 1: SHADOW CUSTOM YANG LEMBUT ---
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.LightGray.copy(alpha = 0.5f), // Warna bayangan lebih soft
                ambientColor = Color.Transparent
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isExpandable) { expanded = !expanded }
            .animateContentSize(), // Letakkan sebelum background agar animasinya mulus
        colors = CardDefaults.cardColors(containerColor = Color.White),
        // Matikan default elevation bawaan Material 3 agar tidak bentrok
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {

            // --- BAGIAN ATAS (Judul & Ikon Checkmark) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

                if (isExpandable) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (isDone) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Selesai", tint = Color(0xFF4CAF50), modifier = Modifier.size(28.dp))
                } else {
                    Icon(Icons.Filled.Warning, contentDescription = "Belum", tint = Color(0xFFFF9800), modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- BAGIAN TENGAH (Rangkuman) ---
            if (isDone) {
                val score = data?.skorTotal ?: data?.skor

                if (!data?.namaAktivitas.isNullOrEmpty()) {
                    Text(text = "Aktivitas: ${data?.namaAktivitas}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (score != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = "Skor", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "$score", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (data?.durasiMenit != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Schedule, contentDescription = "Durasi", tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${data.durasiMenit} Min", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (!data?.kategori.isNullOrEmpty()) {
                        Text(text = "• ${data?.kategori}", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    } else if (customStatus != null) {
                        Text(text = "• $customStatus", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                Text(
                    text = if (isPostTest) "Belum memenuhi syarat / belum dikerjakan" else "Belum dikerjakan hari ini",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // --- BAGIAN BAWAH (ISI DROPDOWN DETAIL) ---
            if (expanded && isDone) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // --- PERBAIKAN 2: FORMAT DETAIL RECALL 24 JAM ---
                if (isRecall && data?.detailJawaban != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Detail Konsumsi:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                        // 1. Mengelompokkan berdasarkan Waktu (Makan Pagi, Selingan Pagi, dll)
                        val groupedData = data.detailJawaban.entries.groupBy { entry ->
                            entry.key.substringBefore("_") // Mengambil kata sebelum "_"
                        }

                        // 2. Mengatur urutan agar rapi dari Pagi ke Malam
                        val waktuOrder = listOf("Makan Pagi", "Selingan Pagi", "Makan Siang", "Selingan Siang", "Makan Malam", "Selingan Malam")
                        val sortedGroups = groupedData.entries.sortedBy { group ->
                            val index = waktuOrder.indexOf(group.key)
                            if (index == -1) 99 else index // Jika tidak dikenali, taruh paling bawah
                        }

                        // 3. Menampilkan ke UI
                        sortedGroups.forEach { (waktu, items) ->
                            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                Text(text = waktu, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                                items.forEach { item ->
                                    // Memotong teks panjang menjadi ringkas (misal: "Makanan Pokok")
                                    var cleanCategory = item.key.substringAfter("_")
                                    if (cleanCategory.contains(" (")) {
                                        cleanCategory = cleanCategory.substringBefore(" (")
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("• $cleanCategory", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                                        Text(item.value, fontSize = 13.sp, color = Color.DarkGray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                    }
                                }
                            }
                        }
                    }
                }

                // Detail Personal Hygiene
                if (isHygiene) {
                    val hygieneList = listOf(
                        "Mandi 2x Sehari" to data?.mandi2xSehari,
                        "Pakai Sabun" to data?.pakaiSabun,
                        "Sikat Gigi Pagi" to data?.sikatGigiPagi,
                        "Sikat Gigi Malam" to data?.sikatGigiMalam,
                        "Cuci Tangan Sblm Makan" to data?.cuciTanganSebelumMakan,
                        "Cuci Tangan Stlh BAB" to data?.cuciTanganSetelahBab,
                        "Memakai Alas Kaki" to data?.pakaiAlasKaki,
                        "Pakai Pakaian Bersih" to data?.pakaiPakaianBersih,
                        "Handuk Pribadi Bersih" to data?.handukPribadiBersih,
                        "Cuci Tangan dr Luar" to data?.cuciTanganLuarRumah
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Skenario yang dilakukan:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        hygieneList.forEach { (label, status) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (status == 1) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                    contentDescription = null,
                                    tint = if (status == 1) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, fontSize = 13.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }
}