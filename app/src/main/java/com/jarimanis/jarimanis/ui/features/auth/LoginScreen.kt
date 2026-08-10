package com.jarimanis.jarimanis.ui.features.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarimanis.jarimanis.ui.components.JariManisTextField
import com.jarimanis.jarimanis.ui.theme.Shapes

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (role: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // State untuk Icon Mata (Password)
    var passwordVisible by remember { mutableStateOf(false) }

    // Efek Navigasi jika sukses
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val role = (uiState as AuthUiState.Success).role
            onLoginSuccess(role)
        }
    }

    // Surface menggunakan warna background off-white
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding() // 1. URUTAN PENTING: imePadding dipanggil SEBELUM verticalScroll
                .verticalScroll(rememberScrollState()) // 2. Bikin layar bisa di-scroll
                .padding(24.dp), // 3. Jarak aman di pinggir
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Jari Manis",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Masuk ke Akun Anda",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Card putih yang menonjol
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.large,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JariManisTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // INPUT PASSWORD (DENGAN MATA & SINGLE LINE)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true, // Mencegah dienter / turun ke baris baru
                        shape = Shapes.medium,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Tampilkan Password")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState is AuthUiState.Error) {
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.login(username, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = Shapes.medium,
                        enabled = uiState !is AuthUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Teks untuk pindah ke halaman Register
            Row {
                Text("Belum punya akun? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Daftar di sini",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.resetState()
                        onNavigateToRegister()
                    }
                )
            }
        }
    }
}