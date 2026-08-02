package com.jarimanis.jarimanis.data.model

data class LoginResponse(
    val message: String,
    val token: String,
    val user: UserProfile
)