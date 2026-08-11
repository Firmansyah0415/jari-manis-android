package com.jarimanis.jarimanis.ui.features.admin

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

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarimanis.jarimanis.data.model.UserProfile
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.utils.Resource
import java.util.Calendar

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
    val sekolahList by viewModel.sekolahList.collectAsState()
    val kelasList by viewModel.kelasList.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var selectedSekolahId by remember { mutableStateOf<Int?>(null) }
    var selectedSekolahName by remember { mutableStateOf("Semua Sekolah") }
    var selectedKelasId by remember { mutableStateOf<Int?>(null) }
    var selectedKelasName by remember { mutableStateOf("Semua Kelas") }
    var expandedSekolah by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) }

    // --- LOGIKA WAKTU UNTUK GREETING ---
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Selamat Pagi"
        in 12..14 -> "Selamat Siang"
        in 15..17 -> "Selamat Sore"
        else -> "Selamat Malam"
    }

    // TAMBAHKAN VARIABEL WARNA INI
    val offWhiteBackground = Color(0xFFF8F9FA)

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            viewModel.fetchSekolah()
            viewModel.fetchAdminDashboard(token)
            viewModel.fetchProfile(token)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dashboard Admin", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Halo ${userProfile?.name ?: "Admin"}, $greeting", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                // Menggunakan warna offWhite persis seperti dashboard siswa
                colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)
            )
        },
        containerColor = offWhiteBackground,
        // --- TAMBAHKAN TOMBOL DOWNLOAD DI SINI ---
//        floatingActionButton = {
//            // --- 1. SIAPKAN PELUNCUR IZIN UNTUK ANDROID LAMA ---
//            val permissionLauncher = rememberLauncherForActivityResult(
//                contract = ActivityResultContracts.RequestPermission(),
//                onResult = { isGranted ->
//                    if (!isGranted) {
//                        Toast.makeText(context, "Izin penyimpanan ditolak. Tidak bisa mengunduh file.", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(context, "Izin diberikan! Silakan klik tombol Export CSV sekali lagi.", Toast.LENGTH_LONG).show()
//                    }
//                }
//            )
//
//            ExtendedFloatingActionButton(
//                onClick = {
//                    // --- 2. CEK IZIN (HANYA UNTUK ANDROID 9 KE BAWAH) ---
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//                            // Jika belum ada izin, minta sekarang lalu hentikan proses klik ini sementara
//                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                            return@ExtendedFloatingActionButton
//                        }
//                    }
//
//                    // --- 3. PROSES DOWNLOAD ---
//                    try {
//                        val baseUrl = ApiClient.BASE_URL.removeSuffix("/")
//                        val url = "$baseUrl/api/admin/export-csv" +
//                                (if (selectedSekolahId != null) "?sekolah_id=$selectedSekolahId" else "") +
//                                (if (selectedKelasId != null) (if (selectedSekolahId != null) "&" else "?") + "kelas_id=$selectedKelasId" else "")
//
//                        val request = DownloadManager.Request(Uri.parse(url))
//                            .addRequestHeader("Authorization", if (token.startsWith("Bearer")) token else "Bearer $token")
//                            .setMimeType("text/csv")
//                            .setTitle("Data_Penelitian_JariManis.csv")
//                            .setDescription("Mengunduh data Excel penelitian...")
//                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Data_Penelitian_JariManis_${System.currentTimeMillis()}.csv")
//                            .setAllowedOverMetered(true)
//                            .setAllowedOverRoaming(true)
//
//                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                        downloadManager.enqueue(request)
//
//                        Toast.makeText(context, "Sedang mengunduh... Cek folder Download di File Manager HP Anda.", Toast.LENGTH_LONG).show()
//                    } catch (e: Exception) {
//                        Toast.makeText(context, "Gagal memulai unduhan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//                    }
//                },
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = Color.White
//            ) {
//                Icon(Icons.Filled.Download, contentDescription = "Export")
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Export CSV")
//            }
//        }
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    Toast.makeText(context, "Memulai unduhan...", Toast.LENGTH_SHORT).show()

                    // Kita gunakan Coroutine Background (Dispatchers.IO) agar UI tidak freeze (macet)
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val baseUrl = ApiClient.BASE_URL.removeSuffix("/")
                            val url = "$baseUrl/api/admin/export-csv" +
                                    (if (selectedSekolahId != null) "?sekolah_id=$selectedSekolahId" else "") +
                                    (if (selectedKelasId != null) (if (selectedSekolahId != null) "&" else "?") + "kelas_id=$selectedKelasId" else "")

                            // Membangun request menggunakan OkHttp (Mesin yang sama yang dipakai Retrofit)
                            val request = Request.Builder()
                                .url(url)
                                .addHeader("Authorization", if (token.startsWith("Bearer")) token else "Bearer $token")
                                .build()

                            val client = OkHttpClient()
                            val response = client.newCall(request).execute()

                            if (response.isSuccessful) {
                                // Ambil data file-nya
                                val bytes = response.body?.bytes()
                                if (bytes != null) {
                                    // Siapkan folder dan nama file
                                    val fileName = "Data_Penelitian_JariManis_${System.currentTimeMillis()}.csv"
                                    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                    val file = File(downloadDir, fileName)

                                    // Tulis/Simpan data ke dalam folder Download HP
                                    FileOutputStream(file).use { output ->
                                        output.write(bytes)
                                    }

                                    // Beritahu pengguna di Main Thread
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "SUKSES! File CSV berhasil tersimpan di folder Download", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Gagal mengunduh data. Error: ${response.code}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Terjadi kesalahan: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Download, contentDescription = "Export")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export CSV")
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // === FILTER DROPDOWN MATERIAL 3 ===
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), // Jarak diubah sedikit agar rapi
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FilterAlt, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Filter Leaderboard", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = expandedSekolah,
                                onExpandedChange = { expandedSekolah = !expandedSekolah },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedSekolahName,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSekolah) },
                                    modifier = Modifier.menuAnchor(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(expanded = expandedSekolah, onDismissRequest = { expandedSekolah = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Semua Sekolah", fontSize = 14.sp) },
                                        onClick = {
                                            selectedSekolahId = null
                                            selectedSekolahName = "Semua Sekolah"
                                            selectedKelasId = null
                                            selectedKelasName = "Semua Kelas"
                                            expandedSekolah = false
                                            viewModel.fetchAdminDashboard(token, null, null)
                                        }
                                    )
                                    sekolahList.forEach { sekolah ->
                                        DropdownMenuItem(
                                            text = { Text(sekolah.nama, fontSize = 14.sp) },
                                            onClick = {
                                                selectedSekolahId = sekolah.id
                                                selectedSekolahName = sekolah.nama
                                                selectedKelasId = null
                                                selectedKelasName = "Semua Kelas"
                                                expandedSekolah = false
                                                viewModel.fetchKelas(sekolah.id)
                                                viewModel.fetchAdminDashboard(token, sekolah.id, null)
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = expandedKelas,
                                onExpandedChange = { if (selectedSekolahId != null) expandedKelas = !expandedKelas },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedKelasName,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                                    modifier = Modifier.menuAnchor(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                    enabled = selectedSekolahId != null,
                                    singleLine = true
                                )
                                ExposedDropdownMenu(expanded = expandedKelas, onDismissRequest = { expandedKelas = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Semua Kelas", fontSize = 14.sp) },
                                        onClick = {
                                            selectedKelasId = null
                                            selectedKelasName = "Semua Kelas"
                                            expandedKelas = false
                                            viewModel.fetchAdminDashboard(token, selectedSekolahId, null)
                                        }
                                    )
                                    kelasList.forEach { kelas ->
                                        DropdownMenuItem(
                                            text = { Text(kelas.nama_kelas, fontSize = 14.sp) },
                                            onClick = {
                                                selectedKelasId = kelas.id
                                                selectedKelasName = kelas.nama_kelas
                                                expandedKelas = false
                                                viewModel.fetchAdminDashboard(token, selectedSekolahId, kelas.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // === KONTEN DATA ===
            when (val state = adminState) {
                is Resource.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text(state.message, color = Color.Red)
                        }
                    }
                }
                is Resource.Success -> {
                    val data = state.data.data

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatCard("Total\nSiswa", "${data.statistik.totalSiswa}", Color(0xFF2196F3), Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(16.dp))
                            StatCard("Rata-Rata\nSkor", "${data.statistik.rataRataSkor}", Color(0xFF4CAF50), Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Global Leaderboard", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(data.leaderboard) { index, siswa ->
                        LeaderboardItemCard(index = index + 1, siswa = siswa, onClick = { onSiswaClick(siswa.id) }, modifier = Modifier.padding(horizontal = 16.dp))
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
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 24.sp, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, color = Color.DarkGray, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
fun LeaderboardItemCard(index: Int, siswa: UserProfile, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (bgColor, textColor, iconColor, circleColor, circleText) = when (index) {
        1 -> listOf(Color(0xFFFFF8E1), Color(0xFFF57F17), Color(0xFFFFD700), Color(0xFFFFD700), Color.White)
        2 -> listOf(Color(0xFFF5F5F5), Color(0xFF616161), Color(0xFFC0C0C0), Color(0xFFC0C0C0), Color.White)
        3 -> listOf(Color(0xFFEFEBE9), Color(0xFF5D4037), Color(0xFFCD7F32), Color(0xFFCD7F32), Color.White)
        else -> listOf(Color.White, Color.DarkGray, Color.Transparent, Color.White, Color.Gray)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(if (index <= 3) 4.dp else 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(circleColor), contentAlignment = Alignment.Center) {
                Text("#$index", color = circleText, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(siswa.name, fontWeight = FontWeight.Bold, color = textColor)
                Text("${siswa.sekolah?.nama ?: "-"} | Kelas: ${siswa.kelas?.nama_kelas ?: "-"}", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (index <= 3) Icon(Icons.Filled.EmojiEvents, contentDescription = "Juara", tint = iconColor, modifier = Modifier.size(20.dp))
                Text("${siswa.totalSkor} Pts", fontWeight = FontWeight.Black, color = textColor)
            }
        }
    }
}