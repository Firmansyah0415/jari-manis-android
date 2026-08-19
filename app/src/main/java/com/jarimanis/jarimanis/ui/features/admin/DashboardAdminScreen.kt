package com.jarimanis.jarimanis.ui.features.admin

import android.app.DatePickerDialog
import android.os.Environment
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Download
import com.jarimanis.jarimanis.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.utils.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdminScreen(
    viewModel: AuthViewModel,
    token: String,
    onSiswaClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val adminState by viewModel.adminDashboardState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    // --- STATE UNTUK DIALOG EXPORT CSV ---
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedExportType by remember { mutableStateOf("induk") }

    // Format Tanggal
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = System.currentTimeMillis() }

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Selamat Pagi"; in 12..14 -> "Selamat Siang"
        in 15..17 -> "Selamat Sore"; else -> "Selamat Malam"
    }
    val offWhiteBackground = Color(0xFFF8F9FA)

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            viewModel.fetchAdminDashboard(token, null, null)
            viewModel.fetchProfile(token)
        }
    }

    // --- DIALOG EXPORT DATA ---
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data Penelitian", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pilih jenis data yang ingin Anda unduh (CSV):", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                    val exportOptions = listOf(
                        "induk" to "1. Data Induk & Total Poin (Overview)",
                        "kebugaran" to "2. Data Pre/Post Test & Kebugaran",
                        "harian" to "3. Log Harian: Aktivitas, TTD, Hygiene",
                        "recall" to "4. Log Harian: Recall Makanan 24 Jam"
                    )

                    Column(Modifier.selectableGroup()) {
                        exportOptions.forEach { (typeKey, typeLabel) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (typeKey == selectedExportType),
                                        onClick = { selectedExportType = typeKey },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = (typeKey == selectedExportType), onClick = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = typeLabel, fontSize = 13.sp, fontWeight = if (typeKey == selectedExportType) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    // Tampilkan Pemilih Tanggal HANYA JIKA memilih Log Harian atau Recall
                    if (selectedExportType == "harian" || selectedExportType == "recall") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Pilih Tanggal Pengisian:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth().height(45.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(displayDateFormat.format(selectedDate), color = Color.DarkGray)
                                Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        Toast.makeText(context, "Memulai unduhan data CSV...", Toast.LENGTH_SHORT).show()

                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val baseUrl = ApiClient.BASE_URL.removeSuffix("/")
                                // Susun URL dinamis berdasarkan pilihan admin
                                var url = "$baseUrl/api/admin/export-csv?tipe=$selectedExportType"
                                if (selectedExportType == "harian" || selectedExportType == "recall") {
                                    url += "&tanggal=${apiDateFormat.format(selectedDate)}"
                                }

                                val request = Request.Builder()
                                    .url(url)
                                    .addHeader("Authorization", if (token.startsWith("Bearer")) token else "Bearer $token")
                                    .build()

                                val response = OkHttpClient().newCall(request).execute()
                                if (response.isSuccessful) {
                                    val bytes = response.body?.bytes()
                                    if (bytes != null) {
                                        val dynamicFileName = "JariManis_${selectedExportType.uppercase()}_${System.currentTimeMillis()}.csv"
                                        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), dynamicFileName)
                                        FileOutputStream(file).use { it.write(bytes) }
                                        withContext(Dispatchers.Main) { Toast.makeText(context, "SUKSES! File $dynamicFileName tersimpan di folder Download", Toast.LENGTH_LONG).show() }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) { Toast.makeText(context, "Gagal mengunduh: Server Error", Toast.LENGTH_LONG).show() }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) { Toast.makeText(context, "Gagal mengunduh: ${e.localizedMessage}", Toast.LENGTH_LONG).show() }
                            }
                        }
                    }
                ) {
                    Text("Unduh Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Batal", color = Color.Gray) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Beranda Analitik", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Halo ${userProfile?.name ?: "Admin"}, $greeting", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)
            )
        },
        containerColor = offWhiteBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showExportDialog = true }, // Munculkan Dialog
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Download, contentDescription = "Export")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Data CSV")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp)) {
            when (val state = adminState) {
                is Resource.Loading -> item { Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                is Resource.Error -> item { Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) { Text(state.message, color = Color.Red) } }
                is Resource.Success -> {
                    val stats = state.data.data.statistik
                    val top3 = state.data.data.leaderboard.take(3)

                    item {
                        Text("Ringkasan Aplikasi (Global)", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatCard("Total Siswa", "${stats.totalSiswa}", Color(0xFF2196F3), Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(12.dp))
                            StatCard("Total Guru", "${stats.totalGuru}", Color(0xFFFF9800), Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(12.dp))
                            StatCard("Rata-Rata Skor", "${stats.rataRataSkor}", Color(0xFF4CAF50), Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("🏆 3 Besar Tertinggi", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    itemsIndexed(top3) { index, siswa ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("#${index + 1}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = if(index==0) Color(0xFFFFD700) else Color.Gray)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(siswa.name, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Text(siswa.sekolah?.nama ?: "-", fontSize = 12.sp, color = Color.Gray)
                                }
                                Text("${siswa.totalSkor} Pts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Kunjungi menu 'Peringkat' untuk melihat daftar lengkap dan menggunakan filter area.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, color = Color.DarkGray, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}