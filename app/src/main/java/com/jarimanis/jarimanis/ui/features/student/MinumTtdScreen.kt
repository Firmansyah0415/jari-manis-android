package com.jarimanis.jarimanis.ui.features.student

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
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
import com.jarimanis.jarimanis.utils.AlarmHelper
import com.jarimanis.jarimanis.utils.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinumTtdScreen(navController: NavController, viewModel: ZonaViewModel, token: String) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val ttdDataState by viewModel.minumTtdData.collectAsState()

    var isYesSelected by remember { mutableStateOf<Boolean?>(null) }
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // FITUR DINAMIS TOMBOL
    val isDataExists = (ttdDataState as? Resource.Success)?.data?.data != null
    val buttonText = if (isDataExists) "Perbarui Data TTD" else "Simpan Data TTD"

    // FETCH & AUTO-FILL
    LaunchedEffect(selectedDate) {
        if (token.isNotEmpty()) viewModel.fetchMinumTtd(token, apiDateFormat.format(selectedDate))
    }
    DisposableEffect(Unit) { onDispose { viewModel.clearMinumTtdData() } }
    LaunchedEffect(ttdDataState) {
        if (ttdDataState is Resource.Success) {
            val data = (ttdDataState as Resource.Success).data.data
            isYesSelected = data?.sudahMinum
        } else if (ttdDataState is Resource.Error) {
            isYesSelected = null
        }
    }

    val alarmState = remember { AlarmHelper.getAlarmState(context) }
    var reminderEnabled by remember { mutableStateOf(alarmState.isActive) }
    var isDaily by remember { mutableStateOf(alarmState.isDaily) }
    var selectedHour by remember { mutableIntStateOf(alarmState.hour) }
    var selectedMinute by remember { mutableIntStateOf(alarmState.minute) }

    fun applyAlarm() {
        if (reminderEnabled) {
            AlarmHelper.setAlarm(context, isDaily, Calendar.MONDAY, selectedHour, selectedMinute)
            Toast.makeText(context, "Pengingat aktif jam ${String.format("%02d:%02d", selectedHour, selectedMinute)}", Toast.LENGTH_SHORT).show()
        } else {
            AlarmHelper.cancelAlarm(context)
            Toast.makeText(context, "Pengingat dinonaktifkan", Toast.LENGTH_SHORT).show()
        }
    }

    val datePickerDialog = DatePickerDialog(context, { _, y, m, d -> calendar.set(y, m, d); selectedDate = calendar.time }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = System.currentTimeMillis() }
    val timePickerDialog = TimePickerDialog(context, { _, h, m -> selectedHour = h; selectedMinute = m; if (reminderEnabled) applyAlarm() }, selectedHour, selectedMinute, true)

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) Toast.makeText(context, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(uiState) {
        uiState.successMessage?.let { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show(); viewModel.resetState(); navController.popBackStack() }
        uiState.errorMessage?.let { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show(); viewModel.resetState() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Zona 3: Minum TTD", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)) },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (ttdDataState is Resource.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Konfirmasi apakah kamu sudah meminum Tablet Tambah Darah (TTD) sesuai jadwal.", color = Color.Gray, fontSize = 14.sp)

                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Sudah minum TTD hari ini?", fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { isYesSelected = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(containerColor = if(isYesSelected == true) MaterialTheme.colorScheme.primary else Color.Transparent, contentColor = if(isYesSelected == true) Color.White else MaterialTheme.colorScheme.primary), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) { Text("Ya, Sudah") }
                                OutlinedButton(onClick = { isYesSelected = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(containerColor = if(isYesSelected == false) Color.Red else Color.Transparent, contentColor = if(isYesSelected == false) Color.White else Color.Red), border = BorderStroke(1.dp, Color.Red)) { Text("Belum") }
                            }
                            HorizontalDivider(Modifier, thickness = 1.dp, color = Color(0xFFF8F9FA))
                            Text("Tanggal Minum:", fontWeight = FontWeight.SemiBold)
                            OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(displayDateFormat.format(selectedDate))
                                    Icon(Icons.Default.DateRange, null)
                                }
                            }
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Aktifkan Pengingat TTD", fontWeight = FontWeight.SemiBold)
                                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it; applyAlarm() })
                            }
                            if (reminderEnabled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = isDaily, onClick = { isDaily = true; applyAlarm() })
                                    Text("Harian")
                                    Spacer(modifier = Modifier.width(16.dp))
                                    RadioButton(selected = !isDaily, onClick = { isDaily = false; applyAlarm() })
                                    Text("Mingguan")
                                }
                                OutlinedButton(onClick = { timePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Default.Schedule, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pilih Jam: ${String.format("%02d:%02d", selectedHour, selectedMinute)}")
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isYesSelected == null) { Toast.makeText(context, "Pilih status minum terlebih dahulu!", Toast.LENGTH_SHORT).show(); return@Button }
                            viewModel.submitMinumTtd(token, isYesSelected!!, apiDateFormat.format(selectedDate))
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !uiState.isLoading
                    ) { Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}