package com.jarimanis.jarimanis.data.model

data class RegisterResponse(
    val message: String,
    val token: String,
    val user: UserProfile
)
