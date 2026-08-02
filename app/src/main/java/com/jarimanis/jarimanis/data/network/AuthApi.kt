package com.jarimanis.jarimanis.data.network

import com.jarimanis.jarimanis.data.model.LoginRequest
import com.jarimanis.jarimanis.data.model.LoginResponse
import com.jarimanis.jarimanis.data.model.LogoutResponse
import com.jarimanis.jarimanis.data.model.RegisterRequest
import com.jarimanis.jarimanis.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    // Menambahkan Header "Authorization: Bearer {token}" agar Laravel Sanctum mengenali user
    @POST("/api/logout")
    suspend fun logoutUser(@Header("Authorization") token: String): Response<LogoutResponse>
}