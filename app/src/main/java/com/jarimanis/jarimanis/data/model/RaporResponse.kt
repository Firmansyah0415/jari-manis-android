package com.jarimanis.jarimanis.data.model

import com.google.gson.annotations.SerializedName

data class RaporResponse(
    val message: String,
    val data: RaporData
)

data class RaporData(
    val user: UserProfile?,
    @SerializedName("tanggal_filter") val tanggalFilter: String?, // <--- TAMBAHAN UNTUK FILTER TANGGAL
    @SerializedName("pre_test") val preTest: RaporItem?,
    @SerializedName("post_test") val postTest: RaporItem?, // <--- TAMBAHAN UNTUK POST-TEST
    @SerializedName("recall_makanan") val recallMakanan: RaporItem?,
    @SerializedName("aktivitas_fisik") val aktivitasFisik: RaporItem?,
    @SerializedName("minum_ttd") val minumTtd: RaporItem?,
    @SerializedName("personal_hygiene") val personalHygiene: RaporItem?
)

// Struktur serbaguna yang sudah diperluas untuk menangkap semua field dari Laravel
data class RaporItem(
    val id: Int,
    val skor: Int? = null, // Untuk Pre-Test, Post-Test, Aktivitas Fisik, & TTD
    @SerializedName("skor_total") val skorTotal: Int? = null, // Untuk Recall & Hygiene
    @SerializedName("durasi_menit") val durasiMenit: Int? = null, // Untuk Aktivitas Fisik

    // Catatan: Saya ubah ke Boolean? karena tipe datanya di Laravel adalah boolean (true/false)
    @SerializedName("sudah_minum") val sudahMinum: Int? = null,

    val kategori: String? = null // Untuk Recall, Aktivitas Fisik, & Hygiene
)