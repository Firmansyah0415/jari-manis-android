package com.jarimanis.jarimanis.ui.features.dashboard

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTestScreen(
    navController: NavController,
    viewModel: ZonaViewModel,
    token: String,
    onSuccessSubmit: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val answers = remember { mutableStateMapOf<Int, Int>() }

    LaunchedEffect(uiState) {
        uiState.successMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onSuccessSubmit()
        }
        uiState.errorMessage?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Evaluasi Akhir (Post-Test)", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)) },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { Text("Jawab pertanyaan evaluasi ini setelah kamu menerapkan Jari Manis selama 5 hari.", color = Color.Gray, fontSize = 14.sp) }

                itemsIndexed(preTestQuestions) { questionIndex, question ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = question.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                            question.options.forEachIndexed { optionIndex, optionText ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().selectable(selected = (answers[questionIndex] == optionIndex), onClick = { answers[questionIndex] = optionIndex }, role = Role.RadioButton).padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = (answers[questionIndex] == optionIndex), onClick = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = optionText, fontSize = 14.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (answers.size < preTestQuestions.size) {
                                Toast.makeText(context, "Harap jawab semua pertanyaan!", Toast.LENGTH_SHORT).show()
                            } else {
                                var correctCount = 0
                                preTestQuestions.forEachIndexed { index, question -> if (answers[index] == question.correctIndex) correctCount++ }
                                val finalScore = (correctCount.toDouble() / preTestQuestions.size * 100).toInt()
                                viewModel.submitPostTest(token, finalScore) // <--- PANGGIL POST TEST
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Selesaikan Misi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}