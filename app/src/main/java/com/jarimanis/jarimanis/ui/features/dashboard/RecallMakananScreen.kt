package com.jarimanis.jarimanis.ui.features.dashboard

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

    val checkedItems = remember { mutableStateMapOf<String, Boolean>() }
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
            TopAppBar(title = { Text("Zona 1: Recall 24 Jam", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = pureWhiteCard))
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
                    Text("Pilih tanggal dan centang makanan yang kamu konsumsi.", color = Color.Gray, fontSize = 14.sp)
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
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = checkedItems[stateKey] == true, onCheckedChange = { checkedItems[stateKey] = it })
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = foodItem, fontSize = 14.sp, color = Color.DarkGray)
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
                            val skorTotal = checkedItems.values.count { it } * 5
                            val detailJawabanMap = checkedItems.filterValues { it }.keys.associateWith { 5 }

                            viewModel.submitRecallMakanan(
                                token = token,
                                tanggal = apiDateFormat.format(selectedDate), // Menggunakan tanggal pilihan
                                skorTotal = skorTotal,
                                detailJawaban = detailJawabanMap
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Simpan Aktivitas Makan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}