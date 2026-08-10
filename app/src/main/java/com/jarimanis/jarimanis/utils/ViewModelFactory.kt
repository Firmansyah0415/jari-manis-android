package com.jarimanis.jarimanis.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jarimanis.jarimanis.data.local.SessionManager
import com.jarimanis.jarimanis.data.repository.AuthRepository
import com.jarimanis.jarimanis.data.repository.ZonaRepository
import com.jarimanis.jarimanis.ui.features.auth.AuthViewModel
import com.jarimanis.jarimanis.ui.features.dashboard.ZonaViewModel

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val zonaRepository: ZonaRepository // Tambahan parameter baru
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository, sessionManager) as T
        }
        if (modelClass.isAssignableFrom(ZonaViewModel::class.java)) {
            return ZonaViewModel(zonaRepository) as T // Tambahan pembuatan ZonaViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}