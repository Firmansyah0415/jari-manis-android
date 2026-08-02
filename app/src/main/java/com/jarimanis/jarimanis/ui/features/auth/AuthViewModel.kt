package com.jarimanis.jarimanis.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarimanis.jarimanis.data.local.SessionManager
import com.jarimanis.jarimanis.data.model.LoginRequest
import com.jarimanis.jarimanis.data.model.RegisterRequest
import com.jarimanis.jarimanis.data.repository.AuthRepository
import com.jarimanis.jarimanis.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

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

                    sessionManager.saveSession(token, role)
                    _uiState.value = AuthUiState.Success(role, "Login berhasil")
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is Resource.Loading -> { } // Sudah ditangani sebelum try
            }
        }
    }

    fun register(name: String, username: String, password: String, role: String) {
        if (name.isBlank() || username.isBlank() || password.isBlank() || role.isBlank()) {
            _uiState.value = AuthUiState.Error("Semua kolom harus diisi")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val request = RegisterRequest(name, username, password, role)
            when (val result = repository.register(request)) {
                is Resource.Success -> {
                    val token = result.data.token
                    val userRole = result.data.user.role

                    // Langsung otomatis login setelah registrasi berhasil
                    sessionManager.saveSession(token, userRole)
                    _uiState.value = AuthUiState.Success(userRole, "Registrasi berhasil")
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is Resource.Loading -> { }
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

            // 3. Hapus memori sesi di HP dan kembalikan state ke Idle
            sessionManager.clearSession()
            _uiState.value = AuthUiState.Idle
        }
    }
}