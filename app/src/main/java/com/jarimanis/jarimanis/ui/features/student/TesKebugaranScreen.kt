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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TesKebugaranScreen(
    navController: NavController,
    viewModel: ZonaViewModel,
    token: String,
    tipeTes: String
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var lariInput by remember { mutableStateOf("") }
    var pushUpInput by remember { mutableStateOf("") }
    var sitUpInput by remember { mutableStateOf("") }
    var pullUpInput by remember { mutableStateOf("") }
    var shuttleInput by remember { mutableStateOf("") }

    // --- STATE TANGGAL BARU ---
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
    val isFormValid = lariInput.isNotBlank() && pushUpInput.isNotBlank() && sitUpInput.isNotBlank() && pullUpInput.isNotBlank() && shuttleInput.isNotBlank()

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
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Masukkan tanggal tes dan hasil tes kebugaran jasmani Anda dengan jujur. Semua kolom wajib diisi.", color = Color.Gray, fontSize = 14.sp)

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

            // INPUT FORM
            OutlinedTextField(value = lariInput, onValueChange = { lariInput = it }, label = { Text("1. Lari 12 Menit (Meter)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = pushUpInput, onValueChange = { pushUpInput = it }, label = { Text("2. Push-Up (Kali)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = sitUpInput, onValueChange = { sitUpInput = it }, label = { Text("3. Sit-Up (Kali)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = pullUpInput, onValueChange = { pullUpInput = it }, label = { Text("4. Pull-Up / Chining") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = shuttleInput, onValueChange = { shuttleInput = it }, label = { Text("5. Shuttle Run (Detik)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.submitTesKebugaran(
                        token = token,
                        tipeTes = tipeTes,
                        tanggal = apiDateFormat.format(selectedDate),
                        lari = lariInput.toFloatOrNull(),
                        push = pushUpInput.toIntOrNull(),
                        sit = sitUpInput.toIntOrNull(),
                        pull = pullUpInput.toIntOrNull(),
                        shuttle = shuttleInput.toFloatOrNull()
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !uiState.isLoading && isFormValid // <--- VALIDASI: Harus isi semua baru bisa diklik
            ) {
                if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Simpan $judulLayar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}