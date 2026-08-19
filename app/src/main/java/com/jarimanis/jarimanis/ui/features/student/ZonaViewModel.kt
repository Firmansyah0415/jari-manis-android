package com.jarimanis.jarimanis.ui.features.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarimanis.jarimanis.data.model.TesKebugaranDetailResponse
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
    fun submitRecallMakanan(token: String, tanggal: String, skorTotal: Int, detailJawaban: Map<String, String>) {
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

    // State untuk mengambil data lama Recall Makanan
    private val _recallMakananData = MutableStateFlow<Resource<RecallMakananDetailResponse>?>(null)
    val recallMakananData: StateFlow<Resource<RecallMakananDetailResponse>?> = _recallMakananData.asStateFlow()

    fun fetchRecallMakanan(token: String, tanggal: String) {
        _recallMakananData.value = Resource.Loading
        viewModelScope.launch {
            _recallMakananData.value = repository.getRecallMakanan(token, tanggal)
        }
    }

    fun clearRecallMakananData() {
        _recallMakananData.value = null
    }

    // ==========================================
    // 3. FUNGSI AKTIVITAS FISIK
    // ==========================================
    fun submitAktivitasFisik(token: String, tanggal: String, namaAktivitas: String, durasiMenit: Int) {
        viewModelScope.launch {
            _uiState.value = ZonaUiState(isLoading = true)
            try {
                val request = AktivitasFisikRequest(tanggal, namaAktivitas, durasiMenit)
                val response = repository.submitAktivitasFisik(token, request)

                if (response.isSuccessful) {
                    _uiState.value = ZonaUiState(successMessage = response.body()?.message)
                } else {
                    _uiState.value = ZonaUiState(errorMessage = "Gagal: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = ZonaUiState(errorMessage = "Error Jaringan: ${e.message}")
            }
        }
    }

    // State untuk mengambil data lama Aktivitas Fisik
    private val _aktivitasFisikData = MutableStateFlow<Resource<com.jarimanis.jarimanis.data.network.AktivitasFisikDetailResponse>?>(null)
    val aktivitasFisikData: StateFlow<Resource<com.jarimanis.jarimanis.data.network.AktivitasFisikDetailResponse>?> = _aktivitasFisikData.asStateFlow()

    fun fetchAktivitasFisik(token: String, tanggal: String) {
        _aktivitasFisikData.value = Resource.Loading
        viewModelScope.launch {
            _aktivitasFisikData.value = repository.getAktivitasFisik(token, tanggal)
        }
    }

    fun clearAktivitasFisikData() {
        _aktivitasFisikData.value = null
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

    // --- STATE ZONA 3 (TTD) ---
    private val _minumTtdData = MutableStateFlow<Resource<com.jarimanis.jarimanis.data.network.MinumTtdDetailResponse>?>(null)
    val minumTtdData: StateFlow<Resource<com.jarimanis.jarimanis.data.network.MinumTtdDetailResponse>?> = _minumTtdData.asStateFlow()

    fun fetchMinumTtd(token: String, tanggal: String) {
        _minumTtdData.value = Resource.Loading
        viewModelScope.launch { _minumTtdData.value = repository.getMinumTtd(token, tanggal) }
    }
    fun clearMinumTtdData() { _minumTtdData.value = null }

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

    // --- STATE ZONA 4 (HYGIENE) ---
    private val _personalHygieneData = MutableStateFlow<Resource<com.jarimanis.jarimanis.data.network.PersonalHygieneDetailResponse>?>(null)
    val personalHygieneData: StateFlow<Resource<com.jarimanis.jarimanis.data.network.PersonalHygieneDetailResponse>?> = _personalHygieneData.asStateFlow()

    fun fetchPersonalHygiene(token: String, tanggal: String) {
        _personalHygieneData.value = Resource.Loading
        viewModelScope.launch { _personalHygieneData.value = repository.getPersonalHygiene(token, tanggal) }
    }
    fun clearPersonalHygieneData() { _personalHygieneData.value = null }

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

    fun submitTesKebugaran(
        token: String,
        tipeTes: String,
        tanggal: String, // <--- Sudah benar ada tanggal
        lari: Float?,
        push: Int?,
        sit: Int?,
        pull: Int?,
        shuttle: Float?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            try {
                // Pastikan 'tanggal' dimasukkan sebagai parameter kedua saat membuat object request
                val request = TesKebugaranRequest(tipeTes, tanggal, lari, push, sit, pull, shuttle)

                val response = repository.submitTesKebugaran(token, request)

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = response.body()?.message ?: "Berhasil")
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Gagal: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error Jaringan: ${e.message}")
            }
        }
    }

    // State untuk mengambil data lama
    private val _tesKebugaranData = MutableStateFlow<Resource<TesKebugaranDetailResponse>?>(null)
    val tesKebugaranData: StateFlow<Resource<TesKebugaranDetailResponse>?> = _tesKebugaranData.asStateFlow()

    fun fetchTesKebugaran(token: String, tipeTes: String) {
        _tesKebugaranData.value = Resource.Loading
        viewModelScope.launch {
            _tesKebugaranData.value = repository.getTesKebugaran(token, tipeTes)
        }
    }

    // --- TAMBAHKAN FUNGSI INI ---
    fun clearTesKebugaranData() {
        _tesKebugaranData.value = null
    }

    // ==========================================
    // STATE & FUNGSI LEADERBOARD AKTIVITAS FISIK
    // ==========================================
    private val _leaderboardState = MutableStateFlow<Resource<LeaderboardResponse>?>(null)
    val leaderboardState: StateFlow<Resource<LeaderboardResponse>?> = _leaderboardState.asStateFlow()

    // Tambahkan parameter lingkup dengan default value "sekolah"
    fun fetchLeaderboardFisik(token: String, lingkup: String? = null, sekolahId: Int? = null, kelasId: Int? = null) {
        _leaderboardState.value = Resource.Loading
        viewModelScope.launch {
            _leaderboardState.value = repository.getLeaderboardAktivitasFisik(token, lingkup, sekolahId, kelasId)
        }
    }
}