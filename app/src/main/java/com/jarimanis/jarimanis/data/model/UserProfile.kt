package com.jarimanis.jarimanis.data.model

import com.google.gson.annotations.SerializedName

data class UserProfile(
    val id: Int,
    val role: String,
    val name: String,
    val username: String,
    val gender: String?, // Bisa null karena user lama mungkin belum punya
    @SerializedName("foto_profil") val fotoProfil: String?,
    val sekolah: Sekolah?, // Data relasi yang dikirim dari Laravel
    val kelas: Kelas?,      // Data relasi yang dikirim dari Laravel

    // TAMBAHKAN BARIS INI:
    @SerializedName("total_skor") val totalSkor: Int? = null,
    @SerializedName("total_hari_aktif") val totalHariAktif: Int? = 0,
    // --- TAMBAHAN BARU ---
    @SerializedName("is_post_test_done") val isPostTestDone: Boolean? = false
)