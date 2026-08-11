package com.jarimanis.jarimanis.data.model

import com.google.gson.annotations.SerializedName

data class AdminDashboardResponse(
    val message: String,
    val data: AdminDashboardData
)

data class AdminDashboardData(
    val statistik: AdminStatistik,
    val leaderboard: List<UserProfile> // Memanfaatkan model UserProfile yang sudah ada
)

data class AdminStatistik(
    @SerializedName("total_siswa") val totalSiswa: Int,
    @SerializedName("total_guru") val totalGuru: Int,
    @SerializedName("rata_rata_skor") val rataRataSkor: Double
)

data class UserListResponse(
    val message: String,
    val data: List<UserProfile>
)