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
import com.jarimanis.jarimanis.data.model.Kelas
import com.jarimanis.jarimanis.data.model.Sekolah
import com.jarimanis.jarimanis.ui.components.JariManisTextField
import com.jarimanis.jarimanis.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (role: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sekolahList by viewModel.sekolahList.collectAsState()
    val kelasList by viewModel.kelasList.collectAsState()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("siswa") }
    var gender by remember { mutableStateOf("L") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var expandedSekolah by remember { mutableStateOf(false) }
    var selectedSekolah by remember { mutableStateOf<Sekolah?>(null) }
    var expandedKelas by remember { mutableStateOf(false) }
    var selectedKelas by remember { mutableStateOf<Kelas?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchSekolah()
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val role = (uiState as AuthUiState.Success).role
            onRegisterSuccess(role)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding() // 1. URUTAN DIPERBAIKI: imePadding DULU
                .verticalScroll(rememberScrollState()) // 2. BARU verticalScroll
                .padding(24.dp), // 3. LALU padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Buat Akun",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Daftar untuk memulai petualangan",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

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
                        value = name,
                        onValueChange = { name = it },
                        label = "Nama Lengkap"
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    JariManisTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username"
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Jenis Kelamin",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = gender == "L", onClick = { gender = "L" })
                        Text("Laki-laki")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = gender == "P", onClick = { gender = "P" })
                        Text("Perempuan")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true, // Mencegah tulisan password bisa di-enter ke bawah
                        shape = Shapes.medium,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Tampilkan Password")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Konfirmasi Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true, // Mencegah konfirmasi password bisa di-enter ke bawah
                        shape = Shapes.medium,
                        isError = password != confirmPassword && confirmPassword.isNotEmpty(),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = "Tampilkan Password")
                            }
                        }
                    )
                    if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                        Text(
                            text = "Password tidak cocok!",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedSekolah,
                        onExpandedChange = { expandedSekolah = !expandedSekolah }
                    ) {
                        OutlinedTextField(
                            value = selectedSekolah?.nama ?: "Pilih Sekolah",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Asal Sekolah") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSekolah) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = Shapes.medium,
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSekolah,
                            onDismissRequest = { expandedSekolah = false }
                        ) {
                            sekolahList.forEach { sekolah ->
                                DropdownMenuItem(
                                    text = { Text("${sekolah.nama} - ${sekolah.daerah}") },
                                    onClick = {
                                        selectedSekolah = sekolah
                                        selectedKelas = null
                                        expandedSekolah = false
                                        viewModel.fetchKelas(sekolah.id)
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedKelas,
                        onExpandedChange = {
                            if (selectedSekolah != null) expandedKelas = !expandedKelas
                        }
                    ) {
                        OutlinedTextField(
                            value = selectedKelas?.nama_kelas ?: "Pilih Kelas",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kelas") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = Shapes.medium,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            enabled = selectedSekolah != null
                        )
                        ExposedDropdownMenu(
                            expanded = expandedKelas,
                            onDismissRequest = { expandedKelas = false }
                        ) {
                            kelasList.forEach { kelas ->
                                DropdownMenuItem(
                                    text = { Text(kelas.nama_kelas) },
                                    onClick = {
                                        selectedKelas = kelas
                                        expandedKelas = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedRole == "siswa", onClick = { selectedRole = "siswa" })
                            Text("Siswa", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedRole == "guru", onClick = { selectedRole = "guru" })
                            Text("Guru", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
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
                        onClick = {
                            viewModel.register(
                                name = name,
                                username = username,
                                password = password,
                                role = selectedRole,
                                gender = gender,
                                sekolahId = selectedSekolah?.id,
                                kelasId = selectedKelas?.id
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = Shapes.medium,
                        enabled = uiState !is AuthUiState.Loading && password.isNotEmpty() && password == confirmPassword,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Daftar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Text("Sudah punya akun? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Masuk di sini",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.resetState()
                        onNavigateToLogin()
                    }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}