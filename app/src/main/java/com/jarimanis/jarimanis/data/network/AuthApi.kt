package com.jarimanis.jarimanis.data.network

import com.jarimanis.jarimanis.data.model.AdminDashboardResponse
import com.jarimanis.jarimanis.data.model.KelasResponse
import com.jarimanis.jarimanis.data.model.LoginRequest
import com.jarimanis.jarimanis.data.model.LoginResponse
import com.jarimanis.jarimanis.data.model.LogoutResponse
import com.jarimanis.jarimanis.data.model.RaporResponse
import com.jarimanis.jarimanis.data.model.RegisterRequest
import com.jarimanis.jarimanis.data.model.RegisterResponse
import com.jarimanis.jarimanis.data.model.SekolahResponse
import com.jarimanis.jarimanis.data.model.SiswaResponse
import com.jarimanis.jarimanis.data.model.UpdateProfileResponse
import com.jarimanis.jarimanis.data.model.UserListResponse
import com.jarimanis.jarimanis.data.model.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("/api/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    // Menambahkan Header "Authorization: Bearer {token}" agar Laravel Sanctum mengenali user
    @POST("/api/logout")
    suspend fun logoutUser(@Header("Authorization") token: String): Response<LogoutResponse>

    // TAMBAHKAN @Headers DI SINI JUGA
    @GET("/api/sekolah")
    suspend fun getSekolah(): Response<SekolahResponse>

    // TAMBAHKAN @Headers DI SINI JUGA
    @GET("/api/kelas/{sekolah_id}")
    suspend fun getKelas(@Path("sekolah_id") sekolahId: Int): Response<KelasResponse>

    // Tambahkan di dalam interface AuthApi
    @GET("/api/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserProfile>

    // Jangan lupa import okhttp3.MultipartBody dan okhttp3.RequestBody di atas file
    @Multipart
    @POST("/api/profil/update")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part("name") name: okhttp3.RequestBody?,
        @Part("username") username: okhttp3.RequestBody?,
        @Part("password") password: okhttp3.RequestBody?,
        @Part("gender") gender: okhttp3.RequestBody?,
        @Part("sekolah_id") sekolahId: okhttp3.RequestBody?,
        @Part("kelas_id") kelasId: okhttp3.RequestBody?,
        @Part fotoProfil: okhttp3.MultipartBody.Part?
    ): Response<UpdateProfileResponse>

    @GET("/api/guru/leaderboard")
    suspend fun getSiswaList(
        @Header("Authorization") token: String
    ): Response<SiswaResponse>

    @GET("/api/guru/siswa/{id}/rapor")
    suspend fun getDetailSiswaRapor(
        @Header("Authorization") token: String,
        @Path("id") siswaId: Int,
        @Query("tanggal") tanggal: String
    ): Response<RaporResponse>

    @GET("/api/admin/dashboard")
    suspend fun getAdminDashboard(
        @Header("Authorization") token: String,
        @Query("sekolah_id") sekolahId: Int? = null,
        @Query("kelas_id") kelasId: Int? = null
    ): Response<AdminDashboardResponse>

    @GET("/api/admin/users")
    suspend fun getAdminUsers(
        @Header("Authorization") token: String,
        @Query("role") role: String? = null,
        @Query("sekolah_id") sekolahId: Int? = null,
        @Query("kelas_id") kelasId: Int? = null
    ): Response<UserListResponse>
}