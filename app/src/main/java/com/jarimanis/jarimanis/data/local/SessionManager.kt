package com.jarimanis.jarimanis.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Ekstensi untuk membuat DataStore di Context Android
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
        // Tambahan kunci baru untuk Pre-Test
        val PRETEST_KEY = booleanPreferencesKey("is_pretest_done")
    }

    // Update fungsi ini agar menerima 3 parameter
    suspend fun saveSession(token: String, role: String, isPretestDone: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
            preferences[USER_ROLE_KEY] = role
            preferences[PRETEST_KEY] = isPretestDone // Simpan status pretest
        }
    }

    // Fungsi untuk mengambil token (digunakan oleh Retrofit nanti)
    val getToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    // Fungsi untuk mengambil role (digunakan oleh Navigasi Dinamis dan Splash Screen)
    val getRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]
    }

    // Fungsi baru untuk mengambil status Pre-Test (Default-nya false jika belum ada data)
    val getPretestStatus: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PRETEST_KEY] ?: false
    }

    // Tambahkan fungsi ini untuk meng-update status Pre-Test dari false menjadi true
    suspend fun updatePretestStatus(isDone: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRETEST_KEY] = isDone
        }
    }

    // Fungsi untuk Logout
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}