package com.jarimanis.jarimanis.ui.features.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jarimanis.jarimanis.data.model.UserProfile
import com.jarimanis.jarimanis.data.network.ApiClient
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardGuruScreen(
    viewModel: AuthViewModel,
    token: String,
    onSiswaClick: (Int) -> Unit
) {
    val siswaList by viewModel.siswaList.collectAsState()
    val isLoading by viewModel.isSiswaLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    val offWhiteBackground = Color(0xFFF8F9FA)

    // Filter siswa berdasarkan pencarian nama
    val filteredSiswaList = remember(siswaList, searchQuery) {
        if (searchQuery.isBlank()) siswaList
        else siswaList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val calendar = Calendar.getInstance()
    val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Selamat Pagi"
        in 12..14 -> "Selamat Siang"
        in 15..17 -> "Selamat Sore"
        else -> "Selamat Malam"
    }

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            viewModel.fetchSiswaList(token)
            viewModel.fetchProfile(token)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Direktori Siswa", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Halo ${userProfile?.name ?: "Guru"}, $greeting", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = offWhiteBackground)
            )
        },
        containerColor = offWhiteBackground
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    viewModel.fetchSiswaList(token)
                    delay(1000.milliseconds)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- PENCARIAN (SEARCH BAR) ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama siswa...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Cari") },
                    // Menambahkan Tombol Silang (X) Jika Teks Tidak Kosong
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = "" // Hapus isi teks
                                    keyboardController?.hide() // Turunkan keyboard
                                }
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = "Hapus Pencarian")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )

                if (isLoading && !isRefreshing) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredSiswaList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (searchQuery.isNotBlank()) "Siswa tidak ditemukan." else "Belum ada data siswa.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Total: ${filteredSiswaList.size} Siswa", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        items(filteredSiswaList) { siswa ->
                            ItemSiswaCardUtama(siswa = siswa, onClick = { onSiswaClick(siswa.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemSiswaCardUtama(siswa: UserProfile, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                if (!siswa.fotoProfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = "${ApiClient.BASE_URL}profil/${siswa.fotoProfil}",
                        contentDescription = "Foto Siswa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.AccountCircle, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = siswa.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "Kelas: ${siswa.kelas?.nama_kelas ?: "-"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}