package com.jarimanis.jarimanis.data.model

import com.google.gson.annotations.SerializedName

data class RaporResponse(
    val message: String,
    val data: RaporData
)

data class RaporData(
    val user: UserProfile?,
    @SerializedName("tanggal_filter") val tanggalFilter: String?,
    @SerializedName("pre_test") val preTest: RaporItem?,
    @SerializedName("post_test") val postTest: RaporItem?,
    @SerializedName("recall_makanan") val recallMakanan: RaporItem?,
    @SerializedName("aktivitas_fisik") val aktivitasFisik: RaporItem?,
    @SerializedName("minum_ttd") val minumTtd: RaporItem?,
    @SerializedName("personal_hygiene") val personalHygiene: RaporItem?,
    @SerializedName("pre_test_kebugaran") val preTestKebugaran: RaporItem? = null,
    @SerializedName("post_test_kebugaran") val postTestKebugaran: RaporItem? = null
)

data class RaporItem(
    val id: Int,
    val skor: Int? = null,
    @SerializedName("skor_total") val skorTotal: Int? = null,
    @SerializedName("durasi_menit") val durasiMenit: Int? = null,

    // --- PERBAIKAN: Diubah menjadi Boolean? ---
    @SerializedName("sudah_minum") val sudahMinum: Boolean? = null,
    val kategori: String? = null,

    // --- WADAH BARU UNTUK AKTIVITAS FISIK ---
    @SerializedName("nama_aktivitas") val namaAktivitas: String? = null,

    // --- WADAH BARU UNTUK RECALL 24 JAM ---
    // Laravel akan mengirim array JSON, Gson Android akan mengubahnya jadi Map
    @SerializedName("detail_jawaban") val detailJawaban: Map<String, String>? = null,

    // --- PERBAIKAN WADAH UNTUK 10 SKENARIO PERSONAL HYGIENE: Diubah menjadi Boolean? ---
    @SerializedName("mandi_2x_sehari") val mandi2xSehari: Boolean? = null,
    @SerializedName("pakai_sabun") val pakaiSabun: Boolean? = null,
    @SerializedName("sikat_gigi_pagi") val sikatGigiPagi: Boolean? = null,
    @SerializedName("sikat_gigi_malam") val sikatGigiMalam: Boolean? = null,
    @SerializedName("cuci_tangan_sebelum_makan") val cuciTanganSebelumMakan: Boolean? = null,
    @SerializedName("cuci_tangan_setelah_bab") val cuciTanganSetelahBab: Boolean? = null,
    @SerializedName("pakai_alas_kaki") val pakaiAlasKaki: Boolean? = null,
    @SerializedName("pakai_pakaian_bersih") val pakaiPakaianBersih: Boolean? = null,
    @SerializedName("handuk_pribadi_bersih") val handukPribadiBersih: Boolean? = null,
    @SerializedName("cuci_tangan_luar_rumah") val cuciTanganLuarRumah: Boolean? = null
)