package com.jarimanis.jarimanis.data.model

import com.google.gson.annotations.SerializedName

data class TesKebugaranDetailResponse(
    val message: String,
    val data: TesKebugaranData? // Bisa null jika siswa belum pernah mengisi
)

data class TesKebugaranData(
    val tanggal: String?,
    @SerializedName("lari_12_menit") val lari12Menit: Float?,
    @SerializedName("push_up") val pushUp: Int?,
    @SerializedName("sit_up") val sitUp: Int?,
    @SerializedName("pull_up_chining") val pullUpChining: Int?,
    @SerializedName("shuttle_run") val shuttleRun: Float?
)