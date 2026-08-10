package com.jarimanis.jarimanis.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // PENTING:
    // Gunakan "http://10.0.2.2:8000" jika Anda menjalankan aplikasi di Emulator Android Studio.
    // Jika Anda memakai HP asli via kabel data, ganti dengan IP Address Wi-Fi laptop Anda (misal: "http://192.168.1.10:8000").

    // Ngriok
//     private const val BASE_URL = "https://diego-beaky-unappeasably.ngrok-free.dev"

    // Lokal tanpa Ngrok
     const val BASE_URL = "http://172.24.93.129:8000/"

    // Interceptor untuk memantau lalu lintas jaringan di Logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Konfigurasi HTTP Client
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Inisialisasi Retrofit
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // Parsing otomatis JSON ke Data Class
            .build()
    }

    // Fungsi untuk memudahkan pemanggilan AuthApi
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    // TAMBAHKAN INI:
    val zonaApi: ZonaApi by lazy {
        retrofit.create(ZonaApi::class.java)
    }
}