package com.jarimanis.jarimanis.ui.features.student

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jarimanis.jarimanis.utils.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AktivitasFisikScreen(
    navController: NavController,
    viewModel: ZonaViewModel,
    token: String
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // --- STATE PENARIK DATA ---
    val aktivitasDataState by viewModel.aktivitasFisikData.collectAsState()

    // --- STATE FORM ---
    var namaAktivitas by remember { mutableStateOf("") }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

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
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = System.currentTimeMillis() }

    val offWhiteBackground = Color(0xFFF8F9FA)
    val pureWhiteCard = Color(0xFFFFFFFF)

    // FITUR 3: Mengecek apakah ada data sebelumnya untuk mengubah teks tombol
    val isDataExists = (aktivitasDataState as? Resource.Success)?.data?.data != null
    val buttonText = if (isDataExists) "Perbarui Aktivitas Fisik" else "Simpan Aktivitas Fisik"

    // 1. Fetch data saat layar dibuka ATAU SAAT TANGGAL DIGANTI
    LaunchedEffect(selectedDate) {
        if (token.isNotEmpty()) {
            viewModel.fetchAktivitasFisik(token, apiDateFormat.format(selectedDate))
        }
    }

    // 2. Bersihkan ingatan saat layar ditutup (Back)
    DisposableEffect(Unit) {
        onDispose { viewModel.clearAktivitasFisikData() }
    }

    // 3. Otomatis mengisi form jika ada data dari backend
    LaunchedEffect(aktivitasDataState) {
        if (aktivitasDataState is Resource.Success) {
            val data = (aktivitasDataState as Resource.Success).data.data
            if (data != null) {
                namaAktivitas = data.namaAktivitas
                sliderPosition = data.durasiMenit.toFloat()
            } else {
                // Kosongkan form jika tanggal ini belum pernah diisi
                namaAktivitas = ""
                sliderPosition = 0f
            }
        } else if (aktivitasDataState is Resource.Error) {
            namaAktivitas = ""
            sliderPosition = 0f
        }
    }

    LaunchedEffect(uiState) {
        uiState.successMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            navController.popBackStack()
        }
        uiState.errorMessage?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zona 2: Aktivitas Fisik", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pureWhiteCard),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = offWhiteBackground
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (aktivitasDataState is Resource.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                // PERBAIKAN KEYBOARD: Menambahkan imePadding dan verticalScroll agar bisa ditarik
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Catat jenis olahraga dan geser durasi waktumu.", color = Color.Gray, fontSize = 14.sp)

                    // --- PEMILIH TANGGAL ---
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = pureWhiteCard, contentColor = Color.DarkGray),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(displayDateFormat.format(selectedDate), fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                        }
                    }

                    // --- INPUT NAMA AKTIVITAS ---
                    OutlinedTextField(
                        value = namaAktivitas,
                        onValueChange = { namaAktivitas = it },
                        label = { Text("Jenis Aktivitas Fisik / Olahraga") },
                        placeholder = { Text("Misal: Bermain Futsal, Jogging") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = pureWhiteCard,
                            unfocusedContainerColor = pureWhiteCard,
                        )
                    )

                    // --- SLIDER DURASI ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = pureWhiteCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${sliderPosition.roundToInt()} Menit", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Slider(value = sliderPosition, onValueChange = { sliderPosition = it }, valueRange = 0f..120f, steps = 23, modifier = Modifier.fillMaxWidth())
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("0 Min", fontSize = 12.sp, color = Color.Gray)
                                Text("120+ Min", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- TOMBOL SIMPAN ---
                    Button(
                        onClick = {
                            viewModel.submitAktivitasFisik(
                                token = token,
                                tanggal = apiDateFormat.format(selectedDate),
                                namaAktivitas = namaAktivitas.trim(), // Data Teks
                                durasiMenit = sliderPosition.roundToInt() // Data Angka
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !uiState.isLoading && namaAktivitas.isNotBlank() && sliderPosition.roundToInt() > 0
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}