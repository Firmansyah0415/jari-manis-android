package com.jarimanis.jarimanis.ui.features.teacher

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
            // Fetch ulang saat guru mengganti tanggal
            if (token.isNotEmpty()) {
                viewModel.fetchDetailRaporSiswa(token, siswaId, apiDateFormat.format(selectedDate))
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = System.currentTimeMillis() }

    // Fetch pertama kali halaman dibuka (default hari ini)
    LaunchedEffect(siswaId) {
        if (token.isNotEmpty()) viewModel.fetchDetailRaporSiswa(token, siswaId, apiDateFormat.format(selectedDate))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Rapor Siswa", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Kembali") }
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

                // ==========================================
                // HEADER PROFIL SISWA
                // ==========================================
                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (user != null && !user.fotoProfil.isNullOrEmpty()) {
                        AsyncImage(
                            model = "${ApiClient.BASE_URL}storage/profil/${user.fotoProfil}",
                            contentDescription = "Foto Siswa",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Filled.AccountCircle, "Avatar", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = user?.name ?: "Nama Tidak Diketahui", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Kelas: ${user?.kelas?.nama_kelas ?: "-"}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                // ==========================================
                // FILTER TANGGAL RAPOR
                // ==========================================
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

                // Menghitung berapa misi harian yang sudah selesai
                var selesaiHarian = 0
                if (data.recallMakanan != null) selesaiHarian++
                if (data.aktivitasFisik != null) selesaiHarian++
                if (data.minumTtd != null) selesaiHarian++
                if (data.personalHygiene != null) selesaiHarian++

                val progress = selesaiHarian / 4f

                // ==========================================
                // KARTU PROGRESS HARIAN SISWA
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
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

                // ==========================================
                // DAFTAR ZONA & NILAI (Desain mirip RaporScreen)
                // ==========================================

                RaporItemCard(
                    title = "Pre-Test (Kuesioner Awal)",
                    isDone = data.preTest != null,
                    subLabel = if (data.preTest != null) "Skor: ${data.preTest.skor}" else "Belum Dikerjakan"
                )

                RaporItemCard(
                    title = "Recall 24 Jam",
                    isDone = data.recallMakanan != null,
                    subLabel = if (data.recallMakanan != null) "Skor: ${data.recallMakanan.skorTotal} | Kategori: ${data.recallMakanan.kategori}" else "Belum Dikerjakan"
                )

                RaporItemCard(
                    title = "Aktivitas Fisik",
                    isDone = data.aktivitasFisik != null,
                    subLabel = if (data.aktivitasFisik != null) "Durasi: ${data.aktivitasFisik.durasiMenit} Min | Skor: ${data.aktivitasFisik.skor} (${data.aktivitasFisik.kategori})" else "Belum Dikerjakan"
                )

                val statusTtd = if (data.minumTtd?.sudahMinum == 1) "Sudah Minum" else "Belum Minum"
                RaporItemCard(
                    title = "Minum TTD",
                    isDone = data.minumTtd != null,
                    subLabel = if (data.minumTtd != null) "Skor: ${data.minumTtd.skor} | Keterangan: $statusTtd" else "Belum Dikerjakan"
                )

                RaporItemCard(
                    title = "Personal Hygiene",
                    isDone = data.personalHygiene != null,
                    subLabel = if (data.personalHygiene != null) "Skor: ${data.personalHygiene.skorTotal} | Kategori: ${data.personalHygiene.kategori}" else "Belum Dikerjakan"
                )

                RaporItemCard(
                    title = "Post-Test (Evaluasi Akhir)",
                    isDone = data.postTest != null,
                    subLabel = if (data.postTest != null) "Skor: ${data.postTest.skor}" else "Belum Memenuhi Syarat / Belum Dikerjakan"
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// Komponen Card Item yang disempurnakan (Sama dengan RaporScreen)
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