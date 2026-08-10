package com.jarimanis.jarimanis.data.model

data class SiswaResponse(
    val message: String,
    val data: List<UserProfile> // Mendaur ulang model UserProfile! Sangat efisien!
)