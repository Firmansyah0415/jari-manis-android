package com.jarimanis.jarimanis.ui.features.student // Sesuaikan dengan package Anda

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Model Data Statis untuk Pertanyaan
data class Question(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

val preTestQuestions = listOf(
    Question(
        text = "1. Anemia adalah kondisi ketika tubuh mengalami kekurangan...",
        options = listOf(
            "Vitamin C",
            "Sel darah merah atau hemoglobin",
            "Air dalam tubuh",
            "Lemak"
        ),
        correctIndex = 1
    ),
    Question(
        text = "2. Salah satu tanda atau gejala anemia yang umum terjadi adalah...",
        options = listOf(
            "Mudah lelah dan lemas",
            "Nafsu makan meningkat",
            "Kulit semakin cerah",
            "Tidur semakin sedikit"
        ),
        correctIndex = 0
    ),
    Question(
        text = "3. TTD (Tablet Tambah Darah) terutama mengandung zat besi yang berfungsi untuk...",
        options = listOf(
            "Membantu pembentukan hemoglobin",
            "Meningkatkan kadar gula darah",
            "Menurunkan berat badan",
            "Mengurangi kebutuhan cairan"
        ),
        correctIndex = 0
    ),
    Question(
        text = "4. Agar penyerapan zat besi dari TTD lebih baik, sebaiknya TTD dikonsumsi bersama...",
        options = listOf(
            "Teh",
            "Kopi",
            "Air putih atau minuman yang mengandung vitamin C",
            "Minuman bersoda"
        ),
        correctIndex = 2
    ),
    Question(
        text = "5. Contoh makanan yang merupakan sumber zat besi adalah...",
        options = listOf(
            "Daging, hati, ikan, dan kacang-kacangan",
            "Permen dan cokelat",
            "Minuman bersoda",
            "Kerupuk saja"
        ),
        correctIndex = 0
    ),
    Question(
        text = "6. Prinsip utama gizi seimbang adalah...",
        options = listOf(
            "Menghindari semua jenis makanan",
            "Mengonsumsi satu jenis makanan setiap hari",
            "Mengonsumsi makanan beragam sesuai kebutuhan tubuh",
            "Hanya mengonsumsi makanan yang rendah lemak"
        ),
        correctIndex = 2
    ),
    Question(
        text = "7. Contoh aktivitas fisik yang baik dilakukan secara rutin adalah...",
        options = listOf(
            "Duduk sepanjang hari",
            "Jalan kaki, bersepeda, atau olahraga",
            "Bermain gawai sepanjang hari",
            "Tidur sepanjang waktu luang"
        ),
        correctIndex = 1
    ),
    Question(
        text = "8. Contoh makanan yang merupakan sumber zat besi adalah...",
        options = listOf(
            "Daging, hati, ikan, dan kacang-kacangan",
            "Permen dan cokelat",
            "Minuman bersoda",
            "Kerupuk saja"
        ),
        correctIndex = 0
    ),
    Question(
        text = "9. Salah satu contoh personal hygiene yang baik adalah...",
        options = listOf(
            "Jarang mencuci tangan",
            "Menggunakan handuk bersama-sama",
            "Mandi dan menjaga kebersihan tubuh secara teratur",
            "Tidak mengganti pakaian setelah berkeringat"
        ),
        correctIndex = 2
    ),
    Question(
        text = "10. Mencuci tangan dengan sabun terutama penting dilakukan...",
        options = listOf(
            "Hanya ketika tangan terlihat kotor",
            "Sebelum makan dan setelah menggunakan toilet",
            "Hanya sebelum tidur",
            "Sekali dalam sehari saja"
        ),
        correctIndex = 1
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreTestScreen(
    navController: NavController,
    viewModel: ZonaViewModel,
    token: String,
    onSuccessSubmit: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // State untuk menyimpan jawaban pengguna (index pertanyaan -> index jawaban)
    val answers = remember { mutableStateMapOf<Int, Int>() }

    // Warna Kustom untuk Desain
    val offWhiteBackground = Color(0xFFF8F9FA)
    val pureWhiteCard = Color(0xFFFFFFFF)

    // 2. Ubah LaunchedEffect untuk memanggil onSuccessSubmit
    LaunchedEffect(uiState) {
        uiState.successMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.resetState()

            // Panggil aksi sukses (akan dieksekusi di AppNavigation)
            onSuccessSubmit()
        }
        uiState.errorMessage?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kuesioner Awal (Pre-Test)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = pureWhiteCard
                )
            )
        },
        containerColor = offWhiteBackground // Latar belakang abu-abu sangat terang
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Banner
                item {
                    Text(
                        text = "Silakan jawab pertanyaan berikut sebelum mengakses menu utama Jari Manis.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Render setiap pertanyaan ke dalam Card putih dengan efek pop-out
                itemsIndexed(preTestQuestions) { questionIndex, question ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = pureWhiteCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // Efek Pop-out
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = question.text,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Render Pilihan Ganda (Radio Buttons)
                            question.options.forEachIndexed { optionIndex, optionText ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = (answers[questionIndex] == optionIndex),
                                            onClick = { answers[questionIndex] = optionIndex },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (answers[questionIndex] == optionIndex),
                                        onClick = null // Di-handle oleh selectable di Row
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = optionText,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Tombol Submit
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (answers.size < preTestQuestions.size) {
                                Toast.makeText(context, "Harap jawab semua pertanyaan!", Toast.LENGTH_SHORT).show()
                            } else {
                                // Hitung Skor Logika
                                var correctCount = 0
                                preTestQuestions.forEachIndexed { index, question ->
                                    if (answers[index] == question.correctIndex) correctCount++
                                }
                                val finalScore = (correctCount.toDouble() / preTestQuestions.size * 100).toInt()

                                // Kirim ke API via ViewModel
                                viewModel.submitPreTest(token, finalScore)
                            }
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
                            Text("Simpan & Lanjutkan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}