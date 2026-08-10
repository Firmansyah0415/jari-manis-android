package com.jarimanis.jarimanis.ui.features.profil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
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

    // State dari ViewModel untuk Dropdown
    val sekolahList by viewModel.sekolahList.collectAsState()
    val kelasList by viewModel.kelasList.collectAsState()

    // State untuk form
    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // State Baru: Gender, Sekolah, Kelas
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

    // Memuat daftar sekolah saat layar dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchSekolah()
    }

    // Jika user sudah punya sekolah sebelumnya, langsung muat kelasnya
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
                title = { Text("Edit Profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali") }
                }
            )
        }
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
            // --- UPLOAD FOTO ---
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(model = selectedImageUri, contentDescription = "Foto Baru", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (!userProfile?.fotoProfil.isNullOrEmpty()) {
                    AsyncImage(model = "${ApiClient.BASE_URL}storage/profil/${userProfile?.fotoProfil}", contentDescription = "Foto Lama", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Avatar", modifier = Modifier.size(100.dp), tint = Color.Gray)
                }
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = "Ganti Foto",
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.BottomEnd).background(Color.Black.copy(alpha = 0.6f), CircleShape).padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- INPUT NAMA ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
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
                                // Reset kelas saat sekolah diubah
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
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = selectedSekolahId != null // Nonaktifkan jika sekolah belum dipilih
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
                label = { Text("Password Baru") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, "Tampilkan Password") }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password Baru") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = password != confirmPassword && confirmPassword.isNotEmpty(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(image, "Tampilkan Password") }
                }
            )
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
                        password = password,
                        gender = gender,                   // KIRIM GENDER
                        sekolahId = selectedSekolahId,     // KIRIM SEKOLAH
                        kelasId = selectedKelasId,         // KIRIM KELAS
                        fotoUri = selectedImageUri,
                        context = context
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = uiState !is AuthUiState.Loading && (password.isEmpty() || password == confirmPassword)
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}