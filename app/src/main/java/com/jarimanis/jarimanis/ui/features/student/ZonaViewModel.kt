package com.jarimanis.jarimanis.ui.features.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarimanis.jarimanis.data.network.*
import com.jarimanis.jarimanis.data.repository.ZonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.jarimanis.jarimanis.utils.Resource // Untuk memakai Resource.Loading

// State untuk memantau status pengiriman API
data class ZonaUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class ZonaViewModel(private val repository: ZonaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ZonaUiState())
    val uiState: StateFlow<ZonaUiState> = _uiState.asStateFlow()

    // Fungsi untuk mereset state (penting saat pindah halaman agar pesan sukses tidak muncul terus)
    fun resetState() {
        _uiState.value = ZonaUiState()
    }

    // ==========================================
    // 1. FUNGSI PRE-TEST
    // ==========================================
    fun submitPreTest(token: String, skor: Int) {
        viewModelScope.launch {
            _uiState.value = ZonaUiState(isLoading = true)
            try {
                val request = PreTestRequest(skor = skor)
                val response = repository.submitPreTest(token, request)

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ZonaUiState(successMessage = response.body()!!.message)
                } else {
                    _uiState.value = ZonaUiState(errorMessage = "Gagal: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = ZonaUiState(errorMessage = "Error Jaringan: ${e.message}")
            }
        }
    }

    fun submitPostTest(token: String, skor: Int) {
        viewModelScope.launch {
            _uiState.value = ZonaUiState(isLoading = true)
            try {
                val response = repository.submitPostTest(token, PostTestRequest(skor))
                if (response.isSuccessful) _uiState.value = ZonaUiState(successMessage = response.body()?.message)
                else _uiState.value = ZonaUiState(errorMessage = "Gagal: ${response.message()}")
            } catch (e: Exception) {
                _uiState.value = ZonaUiState(errorMessage = "Error Jaringan: ${e.message}")
            }
        }
    }

    // ==========================================
    // 2. FUNGSI RECALL 24 JAM
    // ==========================================
    fun submitRecallMakanan(token: String, tanggal: String, skorTotal: Int, detailJawaban: Map<String, Int>?) {
        viewModelScope.launch {
            _uiState.value = ZonaUiState(isLoading = true)
            try {
                val request = RecallMakananRequest(tanggal, skorTotal, detailJawaban)
                val response = repository.submitRecallMakanan(token, request)

                if (response.isSuccessful) _uiState.value = ZonaUiState(successMessage = response.body()?.message)
                else _uiState.value = ZonaUiState(errorMessage = "Gagal: ${response.message()}")
            } catch (e: Exception) {
                _uiState.value = ZonaUiState(errorMessage = "Error Jaringan: ${e.message}")
            }
        }
    }

    // ==========================================
    // 3. FUNGSI AKTIVITAS FISIK
    // ==========================================
    fun submitAktivitasFisik(token: String, tanggal: String, durasiMenit: Int) {
        viewModelScope.launch {
            _uiState.value = ZonaUiState(isLoading = true)
            try {
                val request = AktivitasFisikRequest(tanggal, durasiMenit)
                val response = repository.submitAktivitasFisik(token, request)

                if (response.isSuccessful) _uiState.value = ZonaUiState(successMessage = response.body()?.message)
                else _uiState.value = ZonaUiState(errorMessage = "Gagal: ${response.message()}")
            } catch (e: Exception) {
                _uiState.value = ZonaUiState(errorMessage = "Error Jaringan: ${e.message}")
            }
        }
    }

    // ==========================================
    // 4. FUNGSI MINUM TTD
    // ==========================================
    fun submitMinumTtd(token: String, sudahMinum: Boolean, tanggalMinum: String?) {
        viewModelScope.launch {
            _uiState.value = ZonaUiState(isLoading = true)
            try {
                val request = MinumTtdRequest(sudahMinum, tanggalMinum)
                val response = repository.submitMinumTtd(token, request)

                if (response.isSuccessful) _uiState.value = ZonaUiState(successMessage = response.body()?.message)
                else _uiState.value = ZonaUiState(errorMessage = "Gagal: ${response.message()}")
            } catch (e: Exception) {
                _uiState.value = ZonaUiState(errorMessage = "Error Jaringan: ${e.message}")
            }
        }
    }

    // ==========================================
    // 5. FUNGSI PERSONAL HYGIENE
    // ==========================================
    fun submitPersonalHygiene(token: String, request: PersonalHygieneRequest) {
        // Untuk form yang panjang seperti ini, kita langsung menerima Data Class dari UI
        // agar parameter fungsi ViewModel ini tidak terlalu panjang.
        viewModelScope.launch {
            _uiState.value = ZonaUiState(isLoading = true)
            try {
                val response = repository.submitPersonalHygiene(token, request)

                if (response.isSuccessful) _uiState.value = ZonaUiState(successMessage = response.body()?.message)
                else _uiState.value = ZonaUiState(errorMessage = "Gagal: ${response.message()}")
            } catch (e: Exception) {
                _uiState.value = ZonaUiState(errorMessage = "Error Jaringan: ${e.message}")
            }
        }
    }

    // 1. State untuk menyimpan data rapor
    private val _raporState = MutableStateFlow<Resource<com.jarimanis.jarimanis.data.model.RaporResponse>?>(null)
    val raporState: StateFlow<Resource<com.jarimanis.jarimanis.data.model.RaporResponse>?> = _raporState.asStateFlow()

    // 2. Fungsi untuk mengambil data rapor dari backend dengan filter tanggal
    fun fetchRapor(token: String, tanggal: String) {
        _raporState.value = Resource.Loading
        viewModelScope.launch {
            _raporState.value = repository.getRapor(token, tanggal) // <--- SISIPKAN TANGGAL DI SINI
        }
    }
}