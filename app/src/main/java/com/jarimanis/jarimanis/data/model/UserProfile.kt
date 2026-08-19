package com.jarimanis.jarimanis.data.model

import com.google.gson.annotations.SerializedName

data class UserProfile(
    val id: Int,
    val role: String,
    val name: String,
    val username: String,
    val gender: String?,
    @SerializedName("foto_profil") val fotoProfil: String?,
    val sekolah: Sekolah?,
    val kelas: Kelas?,

    @SerializedName("total_skor") val totalSkor: Int? = null,
    @SerializedName("total_hari_aktif") val totalHariAktif: Int? = 0,
    @SerializedName("is_post_test_done") val isPostTestDone: Boolean? = false,

    // --- TAMBAHAN BARU ---
    @SerializedName("is_pre_test_kebugaran_done") val isPreTestKebugaranDone: Boolean? = false,
    @SerializedName("is_post_test_kebugaran_done") val isPostTestKebugaranDone: Boolean? = false
)