package com.jarimanis.jarimanis.ui.features.teacher

import android.app.DatePickerDialog
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jarimanis.jarimanis.data.model.RaporItem
import com.jarimanis.jarimanis.data.network.ApiClient
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRaporSiswaScreen(
    siswaId: Int,
    viewModel: AuthViewModel,
    token: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val detailRapor by viewModel.detailRaporSiswa.collectAsState()

    val totalPoints = detailRapor?.data?.user?.totalSkor ?: 0

    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }

    val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
            if (token.isNotEmpty()) {
                viewModel.fetchDetailRaporSiswa(token, siswaId, apiDateFormat.format(selectedDate))
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = System.currentTimeMillis() }

    LaunchedEffect(siswaId) {
        if (token.isNotEmpty()) viewModel.fetchDetailRaporSiswa(token, siswaId, apiDateFormat.format(selectedDate))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Rapor Siswa", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
                },
                actions = {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = "Poin", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "$totalPoints Poin", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (detailRapor == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val data = detailRapor!!.data
            val user = data.user

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (user != null && !user.fotoProfil.isNullOrEmpty()) {
                        AsyncImage(model = "${ApiClient.BASE_URL}profil/${user.fotoProfil}", contentDescription = "Foto Siswa", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Filled.AccountCircle, "Avatar", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = user?.name ?: "Nama Tidak Diketahui", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Kelas: ${user?.kelas?.nama_kelas ?: "-"}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                Text("Lihat Progress Tanggal:", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
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

                var selesaiHarian = 0
                if (data.recallMakanan != null) selesaiHarian++
                if (data.aktivitasFisik != null) selesaiHarian++
                if (data.minumTtd != null) selesaiHarian++
                if (data.personalHygiene != null) selesaiHarian++

                val progress = selesaiHarian / 4f

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Progress Zona Harian Siswa", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$selesaiHarian dari 4 Zona Selesai",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            trackColor = Color.White
                        )
                    }
                }

                // ==========================================
                // SECTION 1: KUESIONER PENGETAHUAN
                // ==========================================
                RaporSectionTitleTeacher("Pengetahuan Gizi & Kesehatan")
                AdvancedRaporCardTeacher(title = "Pre-Test (Awal)", data = data.preTest)
                AdvancedRaporCardTeacher(title = "Post-Test (Akhir)", data = data.postTest, isPostTest = true)

                // ==========================================
                // SECTION 2: PENGUKURAN KEBUGARAN
                // ==========================================
                RaporSectionTitleTeacher("Hasil Pengukuran Kebugaran")
                AdvancedRaporCardTeacher(title = "Pre-Test Kebugaran", data = data.preTestKebugaran, isPostTest = true)
                AdvancedRaporCardTeacher(title = "Post-Test Kebugaran", data = data.postTestKebugaran, isPostTest = true)

                // ==========================================
                // SECTION 3: ZONA AKTIVITAS HARIAN
                // ==========================================
                RaporSectionTitleTeacher("Aktivitas Harian (Zona)")
                AdvancedRaporCardTeacher(title = "Recall 24 Jam", data = data.recallMakanan, isRecall = true)
                AdvancedRaporCardTeacher(title = "Aktivitas Fisik", data = data.aktivitasFisik)
                AdvancedRaporCardTeacher(
                    title = "Minum TTD",
                    data = data.minumTtd,
                    customStatus = if (data.minumTtd?.sudahMinum == 1) "Sudah Minum TTD" else "Belum Minum TTD"
                )
                AdvancedRaporCardTeacher(title = "Personal Hygiene", data = data.personalHygiene, isHygiene = true)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun RaporSectionTitleTeacher(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
    )
}

// (KODE AdvancedRaporCardTeacher TETAP SAMA PERSIS SEPERTI AdvancedRaporCard DI ATAS)
@Composable
fun AdvancedRaporCardTeacher(
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
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.LightGray.copy(alpha = 0.5f),
                ambientColor = Color.Transparent
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isExpandable) { expanded = !expanded }
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {

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

            if (expanded && isDone) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                if (isRecall && data?.detailJawaban != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Detail Konsumsi:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                        val groupedData = data.detailJawaban.entries.groupBy { entry ->
                            entry.key.substringBefore("_")
                        }

                        val waktuOrder = listOf("Makan Pagi", "Selingan Pagi", "Makan Siang", "Selingan Siang", "Makan Malam", "Selingan Malam")
                        val sortedGroups = groupedData.entries.sortedBy { group ->
                            val index = waktuOrder.indexOf(group.key)
                            if (index == -1) 99 else index
                        }

                        sortedGroups.forEach { (waktu, items) ->
                            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                Text(text = waktu, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                                items.forEach { item ->
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