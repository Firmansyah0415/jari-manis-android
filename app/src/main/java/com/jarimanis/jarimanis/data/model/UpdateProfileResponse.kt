package com.jarimanis.jarimanis.data.model

data class UpdateProfileResponse(
    val message: String,
    val user: UserProfile // Menggunakan model UserProfile yang sudah kita buat sebelumnya
)