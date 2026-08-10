package com.jarimanis.jarimanis.ui.features.dashboard

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinumTtdScreen(
    navController: NavController,
    viewModel: ZonaViewModel,
    token: String
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // State untuk menyimpan pilihan (null berarti belum memilih)
    var isYesSelected by remember { mutableStateOf<Boolean?>(null) }

    // State untuk kalender
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }

    // Format tanggal untuk UI (Misal: 07 Agustus 2026)
    // Menggunakan forLanguageTag yang merupakan standar Java/Android modern
    val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", java.util.Locale.forLanguageTag("id-ID"))

    // Format tanggal untuk dikirim ke API / Database Laravel (YYYY-MM-DD)
    // Memaksa menggunakan format US agar sistem penanggalan selalu konsisten angka 0-9 untuk server
    val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

    // Dialog Pemilih Tanggal bawaan Android (Native)
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        // --- TAMBAHAN BARU: MENGUNCI TANGGAL MAKSIMAL ADALAH HARI INI ---
        datePicker.maxDate = System.currentTimeMillis()
    }

    val offWhiteBackground = Color(0xFFF8F9FA)
    val pureWhiteCard = Color(0xFFFFFFFF)

    // Memantau respon dari API
    LaunchedEffect(uiState) {
        uiState.successMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            navController.popBackStack() // Kembali ke Dashboard
        }
        uiState.errorMessage?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zona 3: Minum TTD", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pureWhiteCard)
            )
        },
        containerColor = offWhiteBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Konfirmasi apakah kamu sudah meminum Tablet Tambah Darah (TTD) sesuai jadwal.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                // Card Pop-out
                Card(
                    colors = CardDefaults.cardColors(containerColor = pureWhiteCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Apakah kamu sudah minum TTD?", fontWeight = FontWeight.SemiBold)

                        // Segmented Button Custom (Ya / Tidak)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Tombol "Ya"
                            OutlinedButton(
                                onClick = { isYesSelected = true },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isYesSelected == true) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isYesSelected == true) Color.White else MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Ya, Sudah", fontWeight = FontWeight.Bold)
                            }

                            // Tombol "Tidak"
                            OutlinedButton(
                                onClick = { isYesSelected = false },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isYesSelected == false) Color.Red else Color.Transparent,
                                    contentColor = if (isYesSelected == false) Color.White else Color.Red
                                ),
                                border = BorderStroke(1.dp, Color.Red)
                            ) {
                                Text("Belum", fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = offWhiteBackground)

                        // Pemilih Tanggal
                        Text("Tanggal Minum:", fontWeight = FontWeight.SemiBold)
                        OutlinedButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = offWhiteBackground,
                                contentColor = Color.DarkGray
                            ),
                            border = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(displayDateFormat.format(selectedDate))
                                Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Tombol Simpan
                Button(
                    onClick = {
                        if (isYesSelected == null) {
                            Toast.makeText(context, "Pilih Ya atau Belum terlebih dahulu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Eksekusi ke ViewModel
                        viewModel.submitMinumTtd(
                            token = token,
                            sudahMinum = isYesSelected!!,
                            tanggalMinum = apiDateFormat.format(selectedDate)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Simpan Data TTD", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}