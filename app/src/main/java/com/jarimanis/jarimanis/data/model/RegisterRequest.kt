package com.jarimanis.jarimanis.data.model

data class RegisterRequest(
    val name: String,
    val username: String,
    val password: String,
    val role: String
)
