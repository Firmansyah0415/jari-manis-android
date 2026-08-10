package com.jarimanis.jarimanis.data.model

data class SekolahResponse(
    val message: String,
    val data: List<Sekolah>
)

data class Sekolah(
    val id: Int,
    val nama: String,
    val daerah: String
)

data class KelasResponse(
    val message: String,
    val data: List<Kelas>
)

data class Kelas(
    val id: Int,
    val nama_kelas: String
)