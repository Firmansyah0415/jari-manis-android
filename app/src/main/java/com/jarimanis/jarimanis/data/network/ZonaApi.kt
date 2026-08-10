package com.jarimanis.jarimanis.data.network

import com.jarimanis.jarimanis.data.model.RaporResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ZonaApi {

    @POST("/api/pre-test")
    suspend fun submitPreTest(
        @Header("Authorization") token: String,
        @Body request: PreTestRequest
    ): Response<ZonaResponse>

    @POST("/api/post-test")
    suspend fun submitPostTest(
        @Header("Authorization") token: String,
        @Body request: PreTestRequest
    ): Response<ZonaResponse>

    @POST("/api/recall-makanan")
    suspend fun submitRecallMakanan(
        @Header("Authorization") token: String,
        @Body request: RecallMakananRequest
    ): Response<ZonaResponse>

    @POST("/api/aktivitas-fisik")
    suspend fun submitAktivitasFisik(
        @Header("Authorization") token: String,
        @Body request: AktivitasFisikRequest
    ): Response<ZonaResponse>

    @POST("/api/minum-ttd")
    suspend fun submitMinumTtd(
        @Header("Authorization") token: String,
        @Body request: MinumTtdRequest
    ): Response<ZonaResponse>

    @POST("/api/personal-hygiene")
    suspend fun submitPersonalHygiene(
        @Header("Authorization") token: String,
        @Body request: PersonalHygieneRequest
    ): Response<ZonaResponse>

    // Tambahkan di dalam interface ZonaApi
    @GET("/api/rapor")
    suspend fun getRapor(
        @Header("Authorization") token: String,
        @Query("tanggal") tanggal: String
    ):Response<RaporResponse>
}