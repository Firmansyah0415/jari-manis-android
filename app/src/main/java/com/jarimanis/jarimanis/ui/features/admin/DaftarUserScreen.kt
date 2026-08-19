package com.jarimanis.jarimanis.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jarimanis.jarimanis.data.model.UserProfile
import com.jarimanis.jarimanis.data.network.ApiClient
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarUserScreen(
    viewModel: AuthViewModel,
    token: String
) {
    val context = LocalContext.current
    val userListState by viewModel.adminUserList.collectAsState()
    val sekolahList by viewModel.sekolahList.collectAsState()
    val kelasList by viewModel.kelasList.collectAsState()

    // State Delete User
    val deleteUserState by viewModel.deleteUserState.collectAsState()
    var userToDelete by remember { mutableStateOf<UserProfile?>(null) }
    var adminPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val roles = listOf("siswa", "guru")

    var selectedSekolahId by remember { mutableStateOf<Int?>(null) }
    var selectedSekolahName by remember { mutableStateOf("Semua Sekolah") }
    var selectedKelasId by remember { mutableStateOf<Int?>(null) }
    var selectedKelasName by remember { mutableStateOf("Semua Kelas") }

    var expandedSekolah by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }

    val offWhiteBackground = Color(0xFFF8F9FA)
    val keyboardController = LocalSoftwareKeyboardController.current

    fun refreshData() {
        viewModel.fetchAdminUsers(token, role = roles[selectedTabIndex], sekolahId = selectedSekolahId, kelasId = selectedKelasId)
    }

    LaunchedEffect(selectedTabIndex) { refreshData() }

    // Pantau Status Penghapusan User
    LaunchedEffect(deleteUserState) {
        when (deleteUserState) {
            is Resource.Success -> {
                Toast.makeText(context, (deleteUserState as Resource.Success).data, Toast.LENGTH_LONG).show()
                viewModel.resetDeleteState()
                userToDelete = null // Tutup pop-up
                adminPasswordInput = "" // Kosongkan input
                refreshData() // Tarik ulang data agar user hilang dari list
            }
            is Resource.Error -> {
                Toast.makeText(context, (deleteUserState as Resource.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetDeleteState()
            }
            else -> {}
        }
    }

    // --- POP-UP (DIALOG) KONFIRMASI HAPUS USER ---
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                if (deleteUserState !is Resource.Loading) {
                    userToDelete = null
                    adminPasswordInput = ""
                }
            },
            title = { Text(text = "Hapus User?", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Column {
                    Text("Apakah Anda yakin ingin menghapus akun ")
                    Text("${userToDelete?.name}", fontWeight = FontWeight.Bold)
                    Text("Semua data riwayat Zona & Rapor miliknya akan terhapus permanen.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp, top = 4.dp))

                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = { adminPasswordInput = it },
                        label = { Text("Masukkan Password Admin Anda") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.deleteUser(token, userToDelete!!.id, adminPasswordInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = adminPasswordInput.isNotBlank() && deleteUserState !is Resource.Loading
                ) {
                    if (deleteUserState is Resource.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    else Text("Ya, Hapus Permanen")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { userToDelete = null; adminPasswordInput = "" },
                    enabled = deleteUserState !is Resource.Loading
                ) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Manajemen User", fontWeight = FontWeight.Bold, fontSize = 20.sp) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)) },
        containerColor = offWhiteBackground
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            item {
                SecondaryTabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.White) {
                    Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Daftar Siswa", fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Daftar Guru", fontWeight = FontWeight.Bold) })
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FilterAlt, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Filter Pencarian", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExposedDropdownMenuBox(expanded = expandedSekolah, onExpandedChange = { expandedSekolah = !expandedSekolah }, modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = selectedSekolahName, onValueChange = {}, readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSekolah) },
                                    modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp), singleLine = true
                                )
                                ExposedDropdownMenu(expanded = expandedSekolah, onDismissRequest = { expandedSekolah = false }) {
                                    DropdownMenuItem(text = { Text("Semua Sekolah", fontSize = 14.sp) }, onClick = {
                                        selectedSekolahId = null; selectedSekolahName = "Semua Sekolah"; selectedKelasId = null; selectedKelasName = "Semua Kelas"
                                        expandedSekolah = false; refreshData()
                                    })
                                    sekolahList.forEach { sekolah ->
                                        DropdownMenuItem(text = { Text(sekolah.nama, fontSize = 14.sp) }, onClick = {
                                            selectedSekolahId = sekolah.id; selectedSekolahName = sekolah.nama; selectedKelasId = null; selectedKelasName = "Semua Kelas"
                                            expandedSekolah = false; viewModel.fetchKelas(sekolah.id); refreshData()
                                        })
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(expanded = expandedKelas, onExpandedChange = { if (selectedSekolahId != null) expandedKelas = !expandedKelas }, modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = selectedKelasName, onValueChange = {}, readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                                    modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp), enabled = selectedSekolahId != null, singleLine = true
                                )
                                ExposedDropdownMenu(expanded = expandedKelas, onDismissRequest = { expandedKelas = false }) {
                                    DropdownMenuItem(text = { Text("Semua Kelas", fontSize = 14.sp) }, onClick = {
                                        selectedKelasId = null; selectedKelasName = "Semua Kelas"; expandedKelas = false; refreshData()
                                    })
                                    kelasList.forEach { kelas ->
                                        DropdownMenuItem(text = { Text(kelas.nama_kelas, fontSize = 14.sp) }, onClick = {
                                            selectedKelasId = kelas.id; selectedKelasName = kelas.nama_kelas; expandedKelas = false; refreshData()
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            when (val state = userListState) {
                is Resource.Loading -> { item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } } }
                is Resource.Error -> { item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text(state.message, color = Color.Red) } } }
                is Resource.Success -> {
                    val allUsers = state.data.data
                    val filteredUsers = if (searchQuery.isBlank()) allUsers else allUsers.filter { it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true) }

                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari nama atau username...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Cari") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = ""; keyboardController?.hide() }) { Icon(Icons.Filled.Clear, contentDescription = "Hapus") }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, unfocusedBorderColor = Color.LightGray),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        val roleName = roles[selectedTabIndex].replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() }
                        Text("Total $roleName: ${filteredUsers.size} Akun", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }

                    if (filteredUsers.isEmpty()) {
                        item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text(if (searchQuery.isNotBlank()) "User tidak ditemukan" else "Belum ada data", color = Color.Gray) } }
                    } else {
                        items(filteredUsers) { user ->
                            // --- LEMPAR FUNGSI CALLBACK KE ITEM CARD ---
                            UserItemCard(
                                user = user,
                                onDeleteClick = { userToDelete = user },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun UserItemCard(
    user: UserProfile,
    onDeleteClick: () -> Unit, // Tambahan fungsi buang
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                if (!user.fotoProfil.isNullOrEmpty()) {
                    AsyncImage(model = "${ApiClient.BASE_URL}profil/${user.fotoProfil}", contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "Username: @${user.username}", fontSize = 12.sp, color = Color.DarkGray)
                Text(text = "${user.sekolah?.nama ?: "-"} | Kelas: ${user.kelas?.nama_kelas ?: "-"}", fontSize = 11.sp, color = Color.Gray)
            }

            // --- TOMBOL SAMPAH (HAPUS) ---
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFFEBEE))
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Hapus User", tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}