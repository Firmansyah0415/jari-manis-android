package com.jarimanis.jarimanis.ui.features.student

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.jarimanis.jarimanis.data.network.PersonalHygieneRequest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HygieneItem(val key: String, val label: String)

val hygieneChecklist = listOf(
    HygieneItem("mandi", "Mandi minimal 2 kali sehari"),
    HygieneItem("sabun", "Menggunakan sabun saat mandi"),
    HygieneItem("gigi_pagi", "Menyikat gigi setelah sarapan"),
    HygieneItem("gigi_malam", "Menyikat gigi sebelum tidur"),
    HygieneItem("tangan_makan", "Mencuci tangan pakai sabun sebelum makan"),
    HygieneItem("tangan_bab", "Mencuci tangan pakai sabun setelah BAB/BAK"),
    HygieneItem("alas_kaki", "Menggunakan alas kaki saat keluar rumah"),
    HygieneItem("pakaian", "Mengganti pakaian setiap hari dengan yang bersih"),
    HygieneItem("handuk", "Menggunakan handuk pribadi yang bersih"),
    HygieneItem("tangan_luar", "Mencuci tangan setelah bepergian dari luar")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalHygieneScreen(
    navController: NavController,
    viewModel: ZonaViewModel,
    token: String
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val checkedItems = remember { mutableStateMapOf<String, Boolean>() }

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
            TopAppBar(title = { Text("Zona 4: Personal Hygiene", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = pureWhiteCard))
        },
        containerColor = offWhiteBackground
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Pilih tanggal dan centang aktivitas kebersihan dirimu.", color = Color.Gray, fontSize = 14.sp)
                }

                // --- PEMILIH TANGGAL ---
                item {
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = pureWhiteCard, contentColor = Color.DarkGray),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(displayDateFormat.format(selectedDate))
                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = pureWhiteCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            hygieneChecklist.forEach { item ->
                                val isChecked = checkedItems[item.key] ?: false
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { checkedItems[item.key] = !isChecked }.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = isChecked, onCheckedChange = { checked -> checkedItems[item.key] = checked })
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = item.label, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
                                }
                                if (item != hygieneChecklist.last()) {
                                    Divider(color = offWhiteBackground, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val request = PersonalHygieneRequest(
                                tanggal = apiDateFormat.format(selectedDate), // Menggunakan tanggal pilihan
                                mandi2xSehari = checkedItems["mandi"] ?: false,
                                pakaiSabun = checkedItems["sabun"] ?: false,
                                sikatGigiPagi = checkedItems["gigi_pagi"] ?: false,
                                sikatGigiMalam = checkedItems["gigi_malam"] ?: false,
                                cuciTanganSebelumMakan = checkedItems["tangan_makan"] ?: false,
                                cuciTanganSetelahBab = checkedItems["tangan_bab"] ?: false,
                                pakaiAlasKaki = checkedItems["alas_kaki"] ?: false,
                                pakaiPakaianBersih = checkedItems["pakaian"] ?: false,
                                handukPribadiBersih = checkedItems["handuk"] ?: false,
                                cuciTanganLuarRumah = checkedItems["tangan_luar"] ?: false
                            )
                            viewModel.submitPersonalHygiene(token, request)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Simpan Personal Hygiene", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}