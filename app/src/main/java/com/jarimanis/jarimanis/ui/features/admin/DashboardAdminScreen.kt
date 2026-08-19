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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val userProfile by viewModel.userProfile.collectAsState()

    val calendar = Calendar.getInstance()
    val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Selamat Pagi"; in 12..14 -> "Selamat Siang"
        in 15..17 -> "Selamat Sore"; else -> "Selamat Malam"
    }
    val offWhiteBackground = Color(0xFFF8F9FA)

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            // Tarik data Dashboard secara global (tanpa filter sekolah)
            viewModel.fetchAdminDashboard(token, null, null)
            viewModel.fetchProfile(token)
        }
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
                onClick = {
                    Toast.makeText(context, "Memulai unduhan seluruh data...", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val baseUrl = ApiClient.BASE_URL.removeSuffix("/")
                            val request = Request.Builder()
                                .url("$baseUrl/api/admin/export-csv")
                                .addHeader("Authorization", if (token.startsWith("Bearer")) token else "Bearer $token")
                                .build()

                            val response = OkHttpClient().newCall(request).execute()
                            if (response.isSuccessful) {
                                val bytes = response.body?.bytes()
                                if (bytes != null) {
                                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Data_Penelitian_JariManis_${System.currentTimeMillis()}.csv")
                                    FileOutputStream(file).use { it.write(bytes) }
                                    withContext(Dispatchers.Main) { Toast.makeText(context, "SUKSES! File CSV tersimpan di Download", Toast.LENGTH_LONG).show() }
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) { Toast.makeText(context, "Gagal mengunduh", Toast.LENGTH_LONG).show() }
                        }
                    }
                },
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
                    val top3 = state.data.data.leaderboard.take(3) // Ambil 3 Teratas Saja

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

                    // Kita pinjam desain list biasa saja untuk teaser agar cepat
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

// (Biarkan fungsi StatCard seperti sedia kala)
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