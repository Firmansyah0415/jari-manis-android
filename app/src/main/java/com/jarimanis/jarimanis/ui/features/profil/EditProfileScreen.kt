package com.jarimanis.jarimanis.ui.features.profil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jarimanis.jarimanis.data.network.ApiClient
import com.jarimanis.jarimanis.ui.features.auth.AuthUiState
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: AuthViewModel,
    token: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val sekolahList by viewModel.sekolahList.collectAsState()
    val kelasList by viewModel.kelasList.collectAsState()

    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var username by remember { mutableStateOf(userProfile?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var gender by remember { mutableStateOf(userProfile?.gender ?: "L") }
    var selectedSekolahId by remember { mutableStateOf(userProfile?.sekolah?.id) }
    var selectedSekolahName by remember { mutableStateOf(userProfile?.sekolah?.nama ?: "") }
    var selectedKelasId by remember { mutableStateOf(userProfile?.kelas?.id) }
    var selectedKelasName by remember { mutableStateOf(userProfile?.kelas?.nama_kelas ?: "") }

    var expandedSekolah by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    LaunchedEffect(Unit) {
        viewModel.fetchSekolah()
    }

    LaunchedEffect(selectedSekolahId) {
        selectedSekolahId?.let { viewModel.fetchKelas(it) }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            viewModel.resetState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- UPLOAD FOTO (DIROMBAK) ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0)), // Warna abu-abu yang lebih lembut
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = "Foto Baru", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (!userProfile?.fotoProfil.isNullOrEmpty()) {
                        AsyncImage(
                            model = "${ApiClient.BASE_URL}profil/${userProfile?.fotoProfil}",
                            contentDescription = "Foto Lama",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop)
                    } else {
                        // Icon diganti menjadi Person yang lebih bersih
                        Icon(Icons.Filled.Person, contentDescription = "Avatar", modifier = Modifier.size(80.dp), tint = Color.LightGray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Tombol ganti foto dipisah dari gambar
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ubah Foto Profil")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- INPUT NAMA ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- INPUT USERNAME ---
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- INPUT GENDER ---
            Text("Jenis Kelamin", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = gender == "L", onClick = { gender = "L" })
                Text(text = "Laki-laki", modifier = Modifier.clickable { gender = "L" })
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = gender == "P", onClick = { gender = "P" })
                Text(text = "Perempuan", modifier = Modifier.clickable { gender = "P" })
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- DROPDOWN SEKOLAH ---
            ExposedDropdownMenuBox(
                expanded = expandedSekolah,
                onExpandedChange = { expandedSekolah = !expandedSekolah },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSekolahName.ifEmpty { "Pilih Sekolah" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sekolah") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSekolah) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedSekolah,
                    onDismissRequest = { expandedSekolah = false }
                ) {
                    sekolahList.forEach { sekolah ->
                        DropdownMenuItem(
                            text = { Text(sekolah.nama) },
                            onClick = {
                                selectedSekolahId = sekolah.id
                                selectedSekolahName = sekolah.nama
                                expandedSekolah = false
                                selectedKelasId = null
                                selectedKelasName = ""
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- DROPDOWN KELAS ---
            ExposedDropdownMenuBox(
                expanded = expandedKelas,
                onExpandedChange = { if (selectedSekolahId != null) expandedKelas = !expandedKelas },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedKelasName.ifEmpty { "Pilih Kelas" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kelas") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = selectedSekolahId != null
                )
                ExposedDropdownMenu(
                    expanded = expandedKelas,
                    onDismissRequest = { expandedKelas = false }
                ) {
                    kelasList.forEach { kelas ->
                        DropdownMenuItem(
                            text = { Text(kelas.nama_kelas) },
                            onClick = {
                                selectedKelasId = kelas.id
                                selectedKelasName = kelas.nama_kelas
                                expandedKelas = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- INPUT PASSWORD ---
            Text("Ganti Password (Kosongkan jika tidak ingin diubah)", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password Baru (Min. 8 Karakter)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Menghilangkan garis ejaan
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, "Tampilkan Password") }
                }
            )
            if (password.isNotEmpty() && password.length < 8) {
                Text("Password kurang panjang", color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password Baru") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = password != confirmPassword && confirmPassword.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Menghilangkan garis ejaan
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(image, "Tampilkan Password") }
                }
            )
            if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                Text("Password tidak cocok!", color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- ERROR MESSAGE ---
            if (uiState is AuthUiState.Error) {
                Text((uiState as AuthUiState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 12.dp))
            }

            // --- TOMBOL SIMPAN ---
            Button(
                onClick = {
                    viewModel.updateProfile(
                        token = token,
                        name = name,
                        username = username,
                        password = password,
                        gender = gender,
                        sekolahId = selectedSekolahId,
                        kelasId = selectedKelasId,
                        fotoUri = selectedImageUri,
                        context = context
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is AuthUiState.Loading && (password.isEmpty() || (password.length >= 8 && password == confirmPassword))
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}