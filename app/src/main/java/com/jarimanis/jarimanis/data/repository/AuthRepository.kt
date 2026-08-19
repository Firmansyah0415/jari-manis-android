package com.jarimanis.jarimanis.data.repository

import com.jarimanis.jarimanis.data.model.DeleteUserRequest
import com.jarimanis.jarimanis.data.model.KelasResponse
import com.jarimanis.jarimanis.data.model.LoginRequest
import com.jarimanis.jarimanis.data.model.LoginResponse
import com.jarimanis.jarimanis.data.model.RaporResponse
import com.jarimanis.jarimanis.data.model.RegisterRequest
import com.jarimanis.jarimanis.data.model.RegisterResponse
import com.jarimanis.jarimanis.data.model.SekolahResponse
import com.jarimanis.jarimanis.data.network.AuthApi
import com.jarimanis.jarimanis.utils.Resource

class AuthRepository(private val api: AuthApi) {

    private fun formatToken(token: String): String {
        return if (token.startsWith("Bearer ")) token else "Bearer $token"
    }

    // Tambahkan fungsi pembongkar JSON Error dari Laravel
    private fun parseErrorMessage(errorBody: okhttp3.ResponseBody?): String {
        return try {
            val jsonObject = org.json.JSONObject(errorBody?.string() ?: "")
            jsonObject.getString("message")
        } catch (e: Exception) {
            "Terjadi kesalahan pada server"
        }
    }

    suspend fun login(request: LoginRequest): Resource<LoginResponse> {
        return try {
            val response = api.loginUser(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                // Gunakan fungsi pembongkar di sini
                Resource.Error(parseErrorMessage(response.errorBody()))
            }
        } catch (e: java.io.IOException) {
            // Error jika Wi-Fi mati / Tidak ada kuota
            Resource.Error("Koneksi internet bermasalah. Silakan periksa jaringan Anda.")
        } catch (e: Exception) {
            Resource.Error("Sistem sibuk: ${e.localizedMessage}")
        }
    }

    suspend fun register(request: RegisterRequest): Resource<RegisterResponse> {
        return try {
            val response = api.registerUser(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                // Gunakan fungsi pembongkar di sini
                Resource.Error(parseErrorMessage(response.errorBody()))
            }
        } catch (e: java.io.IOException) {
            Resource.Error("Koneksi internet bermasalah. Silakan periksa jaringan Anda.")
        } catch (e: Exception) {
            Resource.Error("Sistem sibuk: ${e.localizedMessage}")
        }
    }

    suspend fun logout(token: String): Resource<String> {
        return try {
            // Format token untuk Sanctum: "Bearer eyJhbG..."
            val formattedToken = formatToken(token)
            val response = api.logoutUser(formattedToken)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.message ?: "Logout sukses")
            } else {
                Resource.Error("Logout gagal: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Kesalahan server: ${e.localizedMessage}")
        }
    }

    // Tambahkan 2 fungsi ini di dalam class AuthRepository:

    suspend fun getSekolah(): Resource<SekolahResponse> {
        return try {
            val response = api.getSekolah()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Gagal mengambil data sekolah")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Koneksi bermasalah")
        }
    }

    suspend fun getKelas(sekolahId: Int): Resource<KelasResponse> {
        return try {
            val response = api.getKelas(sekolahId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Gagal mengambil data kelas")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Koneksi bermasalah")
        }
    }

    // Tambahkan di dalam AuthRepository
    suspend fun getProfile(token: String) = api.getProfile("Bearer $token")

    suspend fun updateProfile(
        token: String,
        name: okhttp3.RequestBody?,
        username: okhttp3.RequestBody?,
        password: okhttp3.RequestBody?,
        gender: okhttp3.RequestBody?,
        sekolahId: okhttp3.RequestBody?,
        kelasId: okhttp3.RequestBody?,
        fotoProfil: okhttp3.MultipartBody.Part?
    ): Resource<com.jarimanis.jarimanis.data.model.UpdateProfileResponse> {
        return try {
            val response = api.updateProfile(formatToken(token), name, username, password, gender, sekolahId, kelasId, fotoProfil)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Gagal memperbarui profil")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Koneksi bermasalah")
        }
    }

    suspend fun getSiswaList(token: String): Resource<com.jarimanis.jarimanis.data.model.SiswaResponse> {
        return try {
            val response = api.getSiswaList(formatToken(token))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Gagal memuat data siswa")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Koneksi bermasalah")
        }
    }

    suspend fun getDetailSiswaRapor(token: String, siswaId: Int, tanggal: String): Resource<RaporResponse> {
        return try {
            val response = api.getDetailSiswaRapor(formatToken(token), siswaId, tanggal)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Gagal memuat rapor siswa")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Koneksi bermasalah")
        }
    }

    suspend fun getAdminDashboard(token: String, sekolahId: Int? = null, kelasId: Int? = null): Resource<com.jarimanis.jarimanis.data.model.AdminDashboardResponse> {
        return try {
            val response = api.getAdminDashboard(formatToken(token), sekolahId, kelasId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Gagal memuat data admin")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Koneksi bermasalah: ${e.localizedMessage}")
        }
    }

    suspend fun getAdminUsers(token: String, role: String? = null, sekolahId: Int? = null, kelasId: Int? = null): Resource<com.jarimanis.jarimanis.data.model.UserListResponse> {
        return try {
            val response = api.getAdminUsers(formatToken(token), role, sekolahId, kelasId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Gagal memuat daftar user")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Koneksi bermasalah: ${e.localizedMessage}")
        }
    }

    suspend fun deleteUser(token: String, userId: Int, adminPassword: String): Resource<String> {
        return try {
            val response = api.deleteUser(formatToken(token), userId,
                DeleteUserRequest(adminPassword)
            )
            if (response.isSuccessful) {
                Resource.Success(response.body()?.message ?: "Berhasil dihapus")
            } else {
                Resource.Error(parseErrorMessage(response.errorBody()))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan server")
        }
    }
}