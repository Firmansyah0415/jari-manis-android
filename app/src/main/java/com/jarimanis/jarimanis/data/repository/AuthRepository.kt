package com.jarimanis.jarimanis.data.repository

import com.jarimanis.jarimanis.data.model.LoginRequest
import com.jarimanis.jarimanis.data.model.LoginResponse
import com.jarimanis.jarimanis.data.model.RegisterRequest
import com.jarimanis.jarimanis.data.model.RegisterResponse
import com.jarimanis.jarimanis.data.network.AuthApi
import com.jarimanis.jarimanis.utils.Resource

class AuthRepository(private val api: AuthApi) {

    suspend fun login(request: LoginRequest): Resource<LoginResponse> {
        return try {
            val response = api.loginUser(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Login gagal: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Tidak dapat terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun register(request: RegisterRequest): Resource<RegisterResponse> {
        return try {
            val response = api.registerUser(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Registrasi gagal: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Tidak dapat terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun logout(token: String): Resource<String> {
        return try {
            // Format token untuk Sanctum: "Bearer eyJhbG..."
            val formattedToken = "Bearer $token"
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
}