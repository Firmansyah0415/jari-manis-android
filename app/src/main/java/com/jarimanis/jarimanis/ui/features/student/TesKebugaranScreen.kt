package com.jarimanis.jarimanis.ui.features.student

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jarimanis.jarimanis.utils.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TesKebugaranScreen(
    navController: NavController,
    viewModel: ZonaViewModel,
    token: String,
    tipeTes: String,
    gender: String // "L" atau "P"
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val tesDataState by viewModel.tesKebugaranData.collectAsState()

    var lariInput by remember { mutableStateOf("") }
    var pushUpInput by remember { mutableStateOf("") }
    var sitUpInput by remember { mutableStateOf("") }
    var pullUpInput by remember { mutableStateOf("") }

    // Shuttle run dipecah jadi 2 agar pengguna tidak bingung
    var shuttleMenitInput by remember { mutableStateOf("") }
    var shuttleDetikInput by remember { mutableStateOf("") }

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
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.maxDate = System.currentTimeMillis() }

    val judulLayar = if (tipeTes == "pre") "Pre-Test Kebugaran" else "Post-Test Kebugaran"

    // Teks dinamis berdasarkan gender
    val labelPullUp = if (gender == "P") "4. Chining (Gantung Siku Tekuk)" else "4. Pull-Up"
    val placeholderPullUp = if (gender == "P") "Berapa detik mampu menahan?" else "Berapa kali repetisi?"

    // Fetch data lama saat layar dibuka
    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            viewModel.fetchTesKebugaran(token, tipeTes)
        }
    }

    // Mengisi form otomatis jika data lama ditemukan
    LaunchedEffect(tesDataState) {
        if (tesDataState is Resource.Success) {
            val data = (tesDataState as Resource.Success).data.data
            if (data != null) {
                // Parsing Tanggal
                if (!data.tanggal.isNullOrEmpty()) {
                    try {
                        selectedDate = apiDateFormat.parse(data.tanggal) ?: calendar.time
                        calendar.time = selectedDate
                    } catch (e: Exception) {}
                }

                // Isi Form
                lariInput = if (data.lari12Menit != null && data.lari12Menit > 0f) {
                    if (data.lari12Menit % 1.0f == 0f) data.lari12Menit.toInt().toString() else data.lari12Menit.toString()
                } else ""

                pushUpInput = if (data.pushUp != null && data.pushUp > 0) data.pushUp.toString() else ""
                sitUpInput = if (data.sitUp != null && data.sitUp > 0) data.sitUp.toString() else ""
                pullUpInput = if (data.pullUpChining != null && data.pullUpChining > 0) data.pullUpChining.toString() else ""

                // Memecah total detik kembali menjadi Menit & Detik
                if (data.shuttleRun != null && data.shuttleRun != 999f && data.shuttleRun > 0f) {
                    val m = (data.shuttleRun / 60).toInt()
                    val d = data.shuttleRun % 60
                    shuttleMenitInput = if (m > 0) m.toString() else ""
                    shuttleDetikInput = if (d > 0f) {
                        if (d % 1.0f == 0f) d.toInt().toString() else d.toString()
                    } else ""
                }
            }
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
                title = { Text(judulLayar, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } }
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (tesDataState is Resource.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Masukkan hasil tes kebugaran jasmani Anda dengan jujur. Semua kolom wajib diisi.", color = Color.Gray, fontSize = 14.sp)

                // PEMILIH TANGGAL
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.DarkGray),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(displayDateFormat.format(selectedDate), fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                    }
                }

                // 1. LARI 12 MENIT DENGAN KONVERSI OTOMATIS
                Column {
                    OutlinedTextField(
                        value = lariInput, onValueChange = { lariInput = it },
                        label = { Text("1. Lari 12 Menit (Jarak dalam Meter)") },
                        placeholder = { Text("Misal: 1500") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )
                    val lariVal = lariInput.toFloatOrNull()
                    if (lariVal != null && lariVal > 0) {
                        Text("Terbaca: ${lariVal / 1000} Kilometer", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp), fontWeight = FontWeight.Bold)
                    }
                }

                // 2. PUSH UP
                OutlinedTextField(
                    value = pushUpInput, onValueChange = { pushUpInput = it },
                    label = { Text("2. Push-Up") }, placeholder = { Text("Berapa kali repetisi?") },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                // 3. SIT UP
                OutlinedTextField(
                    value = sitUpInput, onValueChange = { sitUpInput = it },
                    label = { Text("3. Sit-Up") }, placeholder = { Text("Berapa kali repetisi?") },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                // 4. PULL UP / CHINING (LABEL DINAMIS)
                OutlinedTextField(
                    value = pullUpInput, onValueChange = { pullUpInput = it },
                    label = { Text(labelPullUp) }, placeholder = { Text(placeholderPullUp) },
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                // 5. SHUTTLE RUN (DIPISAH MENIT DAN DETIK AGAR MUDAH)
                Column {
                    Text("5. Shuttle Run (Lari Angka 8)", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = shuttleMenitInput, onValueChange = { shuttleMenitInput = it },
                            label = { Text("Menit") }, placeholder = { Text("Misal: 1") },
                            modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                        )
                        OutlinedTextField(
                            value = shuttleDetikInput, onValueChange = { shuttleDetikInput = it },
                            label = { Text("Detik") }, placeholder = { Text("Misal: 18.5") },
                            modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val isFormValid = lariInput.isNotBlank() && pushUpInput.isNotBlank() && sitUpInput.isNotBlank() && pullUpInput.isNotBlank() && (shuttleMenitInput.isNotBlank() || shuttleDetikInput.isNotBlank())

                Button(
                    onClick = {
                        // Matematika penggabungan menit dan detik menjadi Total Detik untuk backend
                        val m = shuttleMenitInput.toFloatOrNull() ?: 0f
                        val d = shuttleDetikInput.toFloatOrNull() ?: 0f
                        val totalShuttleSeconds = (m * 60) + d

                        viewModel.submitTesKebugaran(
                            token = token,
                            tipeTes = tipeTes,
                            tanggal = apiDateFormat.format(selectedDate),
                            lari = lariInput.toFloatOrNull(),
                            push = pushUpInput.toIntOrNull(),
                            sit = sitUpInput.toIntOrNull(),
                            pull = pullUpInput.toIntOrNull(),
                            shuttle = if (totalShuttleSeconds > 0f) totalShuttleSeconds else 999f
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !uiState.isLoading && isFormValid
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Simpan $judulLayar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}