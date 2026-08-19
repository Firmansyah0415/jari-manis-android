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
    @SerializedName("detail_jawaban") val detailJawaban: Map<String, String>? = null
)

data class AktivitasFisikRequest(
    val tanggal: String,
    @SerializedName("nama_aktivitas") val namaAktivitas: String,
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

data class TesKebugaranRequest(
    @SerializedName("tipe_tes") val tipeTes: String,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("lari_12_menit") val lari12Menit: Float?,
    @SerializedName("push_up") val pushUp: Int?,
    @SerializedName("sit_up") val sitUp: Int?,
    @SerializedName("pull_up_chining") val pullUpChining: Int?,
    @SerializedName("shuttle_run") val shuttleRun: Float?
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

// ==========================================
// MODEL LEADERBOARD (Baru)
// ==========================================
data class LeaderboardResponse(
    val message: String,
    val data: List<LeaderboardItem>
)

data class LeaderboardItem(
    val peringkat: Int,
    val id: Int,
    val nama: String,
    val kelas: String,
    @SerializedName("foto_profil") val fotoProfil: String?,
    @SerializedName("total_skor") val totalSkor: Int,
    @SerializedName("total_menit") val totalMenit: Int
)