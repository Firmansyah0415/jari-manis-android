package com.jarimanis.jarimanis.data.network

import com.google.gson.annotations.SerializedName

// ==========================================
// MODEL REQUEST (Data yang dikirim ke API)
// ==========================================

data class PreTestRequest(
    val skor: Int
)

data class PostTestRequest(
    val skor: Int
)

data class RecallMakananRequest(
    val tanggal: String,
    @SerializedName("skor_total") val skorTotal: Int,
    @SerializedName("detail_jawaban") val detailJawaban: Map<String, Int>? = null
)

data class AktivitasFisikRequest(
    val tanggal: String,
    @SerializedName("durasi_menit") val durasiMenit: Int
)

data class MinumTtdRequest(
    @SerializedName("sudah_minum") val sudahMinum: Boolean,
    @SerializedName("tanggal_minum") val tanggalMinum: String? = null
)

data class PersonalHygieneRequest(
    val tanggal: String,
    @SerializedName("mandi_2x_sehari") val mandi2xSehari: Boolean,
    @SerializedName("pakai_sabun") val pakaiSabun: Boolean,
    @SerializedName("sikat_gigi_pagi") val sikatGigiPagi: Boolean,
    @SerializedName("sikat_gigi_malam") val sikatGigiMalam: Boolean,
    @SerializedName("cuci_tangan_sebelum_makan") val cuciTanganSebelumMakan: Boolean,
    @SerializedName("cuci_tangan_setelah_bab") val cuciTanganSetelahBab: Boolean,
    @SerializedName("pakai_alas_kaki") val pakaiAlasKaki: Boolean,
    @SerializedName("pakai_pakaian_bersih") val pakaiPakaianBersih: Boolean,
    @SerializedName("handuk_pribadi_bersih") val handukPribadiBersih: Boolean,
    @SerializedName("cuci_tangan_luar_rumah") val cuciTanganLuarRumah: Boolean
)

// ==========================================
// MODEL RESPONSE (Data balasan dari API)
// ==========================================

/**
 * Kita hanya mengambil 'message' (Pesan Sukses) dari API.
 * Objek 'data' tidak perlu kita petakan (mapping) karena di sisi Android,
 * kita hanya butuh informasi bahwa data berhasil dikirim untuk melanjutkan navigasi.
 */
data class ZonaResponse(
    val message: String
)