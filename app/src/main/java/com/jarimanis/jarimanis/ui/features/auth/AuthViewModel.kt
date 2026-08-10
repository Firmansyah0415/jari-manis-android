package com.jarimanis.jarimanis.ui.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarimanis.jarimanis.data.local.SessionManager
import com.jarimanis.jarimanis.data.model.LoginRequest
import com.jarimanis.jarimanis.data.model.RaporResponse
import com.jarimanis.jarimanis.data.model.RegisterRequest
import com.jarimanis.jarimanis.data.model.UserProfile
import com.jarimanis.jarimanis.data.repository.AuthRepository
import com.jarimanis.jarimanis.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

// State untuk UI (bisa dipakai untuk Login maupun Register)
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val role: String, val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // 1. TAMBAHKAN STATE UNTUK SEKOLAH & KELAS (Taruh di bawah uiState)
    private val _sekolahList = MutableStateFlow<List<com.jarimanis.jarimanis.data.model.Sekolah>>(emptyList())
    val sekolahList: StateFlow<List<com.jarimanis.jarimanis.data.model.Sekolah>> = _sekolahList.asStateFlow()

    private val _kelasList = MutableStateFlow<List<com.jarimanis.jarimanis.data.model.Kelas>>(emptyList())
    val kelasList: StateFlow<List<com.jarimanis.jarimanis.data.model.Kelas>> = _kelasList.asStateFlow()

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Username dan Password tidak boleh kosong")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val request = LoginRequest(username, password)
            when (val result = repository.login(request)) {
                is Resource.Success -> {
                    val token = result.data.token
                    val role = result.data.user.role
                    // Menarik data status pre-test (jika null, default ke false)
                    val isPretestDone = result.data.isPretestDone ?: false

                    // Simpan ke SessionManager dengan 3 parameter baru
                    sessionManager.saveSession(token, role, isPretestDone)
                    _uiState.value = AuthUiState.Success(role, "Login berhasil")
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is Resource.Loading -> { }
            }
        }
    }

    // 2. FUNGSI UNTUK MENGAMBIL DATA DARI REPOSITORY
    fun fetchSekolah() {
        Log.d("API_SEKOLAH", "1. Fungsi fetchSekolah() dipanggil oleh UI!") // Pelacak 1

        viewModelScope.launch {
            Log.d("API_SEKOLAH", "2. Mulai menghubungi Repository...") // Pelacak 2

            when (val result = repository.getSekolah()) {
                is Resource.Success -> {
                    _sekolahList.value = result.data.data
                    Log.d("API_SEKOLAH", "3. SUKSES: Berhasil mengambil ${result.data.data.size} sekolah")
                }
                is Resource.Error -> {
                    Log.e("API_SEKOLAH", "3. GAGAL: ${result.message}")
                }
                else -> {
                    Log.e("API_SEKOLAH", "3. ERROR LAINNYA: $result")
                }
            }
        }
    }

    fun fetchKelas(sekolahId: Int) {
        viewModelScope.launch {
            when (val result = repository.getKelas(sekolahId)) {
                is Resource.Success -> { _kelasList.value = result.data.data }
                else -> { }
            }
        }
    }

    fun register(
        name: String,
        username: String,
        password: String,
        role: String,
        gender: String,
        sekolahId: Int? = null, // Parameter baru
        kelasId: Int? = null    // Parameter baru
    ) {
        if (name.isBlank() || username.isBlank() || password.isBlank() || role.isBlank()) {
            _uiState.value = AuthUiState.Error("Semua kolom harus diisi")
            return
        }

        // Tambahkan validasi: Jika Siswa atau Guru, wajib pilih Sekolah & Kelas
        if ((role == "siswa" || role == "guru") && (sekolahId == null || kelasId == null)) {
            _uiState.value = AuthUiState.Error("Sekolah dan Kelas wajib dipilih!")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            // Masukkan sekolahId dan kelasId ke dalam request
            val request = RegisterRequest(name, username, password, role, gender, sekolahId, kelasId)
            when (val result = repository.register(request)) {
                is Resource.Success -> {
                    val token = result.data.token
                    val userRole = result.data.user.role

                    // Langsung otomatis login setelah registrasi berhasil.
                    // Karena user baru, isPretestDone DIPASTIKAN false.
                    sessionManager.saveSession(token, userRole, false)
                    _uiState.value = AuthUiState.Success(userRole, "Registrasi berhasil")
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is Resource.Loading -> { }
            }
        }
    }

    // State untuk menyimpan data Profil User (Sekarang rapi dan konsisten)
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Fungsi untuk memanggil API Profil
    fun fetchProfile(token: String) {
        viewModelScope.launch {
            try {
                val response = repository.getProfile(token)
                if (response.isSuccessful && response.body() != null) {
                    _userProfile.value = response.body()
                } else {
                    // Tangani jika server merespons tapi dengan status error (misal: 401 atau 500)
                    _uiState.value = AuthUiState.Error("Gagal memuat profil: ${response.message()}")
                }
            } catch (e: Exception) {
                // Tangani error jaringan di sini (misal: tidak ada internet atau server mati)
                _uiState.value = AuthUiState.Error("Koneksi bermasalah: Pastikan internet Anda aktif.")
            }
        }
    }

    fun updateProfile(
        token: String,
        name: String,
        password: String?,
        gender: String,
        sekolahId: Int?,
        kelasId: Int?,
        fotoUri: android.net.Uri?,
        context: android.content.Context
    ) {
        if (name.isBlank()) {
            _uiState.value = AuthUiState.Error("Nama tidak boleh kosong")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val passwordBody = if (!password.isNullOrBlank()) password.toRequestBody("text/plain".toMediaTypeOrNull()) else null

            // Konversi 3 data baru ke RequestBody
            val genderBody = gender.toRequestBody("text/plain".toMediaTypeOrNull())
            val sekolahBody = sekolahId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val kelasBody = kelasId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            var fotoPart: okhttp3.MultipartBody.Part? = null
            if (fotoUri != null) {
                val file = com.jarimanis.jarimanis.utils.uriToFile(context, fotoUri)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    fotoPart = okhttp3.MultipartBody.Part.createFormData("foto_profil", file.name, requestFile)
                }
            }

            // Panggil Repository dengan urutan yang sesuai
            when (val result = repository.updateProfile(token, nameBody, passwordBody, genderBody, sekolahBody, kelasBody, fotoPart)) {
                is Resource.Success -> {
                    _userProfile.value = result.data.user
                    _uiState.value = AuthUiState.Success(result.data.user.role, "Profil berhasil diperbarui")
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is Resource.Loading -> { }
            }
        }
    }

    // --- STATE UNTUK DASHBOARD GURU ---
    private val _siswaList = MutableStateFlow<List<UserProfile>>(emptyList())
    val siswaList: StateFlow<List<UserProfile>> = _siswaList.asStateFlow()

    private val _isSiswaLoading = MutableStateFlow(false)
    val isSiswaLoading: StateFlow<Boolean> = _isSiswaLoading.asStateFlow()

    fun fetchSiswaList(token: String) {
        _isSiswaLoading.value = true
        viewModelScope.launch {
            when (val result = repository.getSiswaList(token)) {
                is Resource.Success -> {
                    _siswaList.value = result.data.data
                    _isSiswaLoading.value = false
                }
                is Resource.Error -> {
                    _isSiswaLoading.value = false
                }
                else -> { _isSiswaLoading.value = false }
            }
        }
    }

    private val _detailRaporSiswa = MutableStateFlow<RaporResponse?>(null)
    val detailRaporSiswa: StateFlow<RaporResponse?> = _detailRaporSiswa.asStateFlow()

    fun fetchDetailRaporSiswa(token: String, siswaId: Int, tanggal: String) {
        viewModelScope.launch {
            _detailRaporSiswa.value = null // Reset state sbelum fetch
            when (val result = repository.getDetailSiswaRapor(token, siswaId, tanggal)) {
                is Resource.Success -> _detailRaporSiswa.value = result.data
                is Resource.Error -> { /* Tangani error jika perlu */ }
                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // 1. Ambil token saat ini dari DataStore
            val token = sessionManager.getToken.firstOrNull()

            // 2. Jika token ada, tembak API Logout di Laravel
            if (token != null) {
                repository.logout(token)
            }

            // 3. Hapus memori sesi (termasuk role & pretest status) dan kembalikan state ke Idle
            sessionManager.clearSession()
            _uiState.value = AuthUiState.Idle
        }
    }
}