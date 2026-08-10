package com.jarimanis.jarimanis.data.model
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val message: String,
    val token: String,
    val user: UserProfile,

    // Tambahan untuk menangkap status pre-test dari API Laravel
    @SerializedName("is_pretest_done")
    val isPretestDone: Boolean? = false
)