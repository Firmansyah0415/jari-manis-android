package com.jarimanis.jarimanis.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val name: String,
    val username: String,
    val password: String,
    val role: String,
    val gender: String,
    @SerializedName("sekolah_id") val sekolahId: Int? = null, // Tambahan
    @SerializedName("kelas_id") val kelasId: Int? = null      // Tambahan
)