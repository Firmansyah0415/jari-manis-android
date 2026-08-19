package com.jarimanis.jarimanis.ui.features.student

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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

// Struktur Data untuk Kategori Makan
data class MealCategory(
    val title: String,
    val items: List<String>
)

val recallMeals = listOf(
    MealCategory("Makan Pagi", listOf("Makanan Pokok (misal: nasi, roti)", "Lauk Hewani (misal: ikan goreng)", "Lauk Nabati (misal: tempe)", "Sayuran (misal: tumis kangkung)", "Buah-Buahan (misal: jeruk)", "Minuman (misal: air putih)")),
    MealCategory("Selingan Pagi", listOf("Snack (misal: biskuit, kue)")),
    MealCategory("Makan Siang", listOf("Makanan Pokok (misal: nasi, roti)", "Lauk Hewani (misal: ikan goreng)", "Lauk Nabati (misal: tempe)", "Sayuran (misal: tumis kangkung)", "Buah-Buahan (misal: jeruk)", "Minuman (misal: air putih)")),
    MealCategory("Selingan Sore", listOf("Snack (misal: biskuit, kue)")),
    MealCategory("Makan Malam", listOf("Makanan Pokok (misal: nasi, roti)", "Lauk Hewani (misal: ikan goreng)", "Lauk Nabati (misal: tempe)", "Sayuran (misal: tumis kangkung)", "Buah-Buahan (misal: jeruk)", "Minuman (misal: air putih)"))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecallMakananScreen(
    navController: NavController,
    viewModel: ZonaViewModel,
    token: String
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // STATE BARU: Menyimpan Teks jawaban (bukan true/false lagi)
    // Jika key ada di map, berarti dicentang. Value-nya adalah teks yang diketik siswa.
    val checkedItems = remember { mutableStateMapOf<String, String>() }
    var expandedCategory by remember { mutableStateOf<String?>(null) }

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
            TopAppBar(
                title = { Text("Zona 1: Recall 24 Jam", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pureWhiteCard),
                // --- 1. TAMBAHKAN TOMBOL KEMBALI DI SINI ---
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
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
                    Text("Pilih tanggal dan centang makanan yang kamu konsumsi, lalu ketik nama makanannya.", color = Color.Gray, fontSize = 14.sp)
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

                items(recallMeals) { category ->
                    val isExpanded = expandedCategory == category.title
                    Card(
                        onClick = { expandedCategory = if (isExpanded) null else category.title },
                        colors = CardDefaults.cardColors(containerColor = pureWhiteCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = category.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Expand", tint = Color.Gray)
                            }
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Divider(color = offWhiteBackground, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    category.items.forEach { foodItem ->
                                        val stateKey = "${category.title}_$foodItem"
                                        val isChecked = checkedItems.containsKey(stateKey)

                                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                            // BARIS CHECKBOX
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { checked ->
                                                        if (checked) {
                                                            checkedItems[stateKey] = "" // Tambahkan dengan teks kosong
                                                        } else {
                                                            checkedItems.remove(stateKey) // Hapus jika batal centang
                                                        }
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = foodItem, fontSize = 14.sp, color = Color.DarkGray)
                                            }

                                            // KOTAK TEKS (Hanya Muncul Jika Dicentang)
                                            AnimatedVisibility(
                                                visible = isChecked,
                                                enter = expandVertically(),
                                                exit = shrinkVertically()
                                            ) {
                                                OutlinedTextField(
                                                    value = checkedItems[stateKey] ?: "",
                                                    onValueChange = { newValue ->
                                                        checkedItems[stateKey] = newValue // Update teks ketikan
                                                    },
                                                    modifier = Modifier.fillMaxWidth().padding(start = 48.dp, end = 8.dp, bottom = 8.dp),
                                                    singleLine = true,
                                                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                        unfocusedBorderColor = Color.LightGray
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val validItems = checkedItems.filterValues { it.isNotBlank() }
                            val skorTotal = validItems.size * 5

                            viewModel.submitRecallMakanan(
                                token = token,
                                tanggal = apiDateFormat.format(selectedDate),
                                skorTotal = skorTotal,
                                detailJawaban = validItems
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        // --- KODE PERBAIKAN DI SINI ---
                        // Tombol nyala JIKA tidak loading AND minimal ada 1 kotak dicentang AND semua yang dicentang teksnya sudah diketik
                        enabled = !uiState.isLoading && checkedItems.isNotEmpty() && checkedItems.values.all { it.isNotBlank() }
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Simpan Aktivitas Makan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}