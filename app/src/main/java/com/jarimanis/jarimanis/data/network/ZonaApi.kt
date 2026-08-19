package com.jarimanis.jarimanis.data.network

import com.jarimanis.jarimanis.data.model.RaporResponse
import com.jarimanis.jarimanis.data.model.TesKebugaranDetailResponse
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
        @Body request: PostTestRequest
    ): Response<ZonaResponse>

    @POST("/api/recall-makanan")
    suspend fun submitRecallMakanan(
        @Header("Authorization") token: String,
        @Body request: RecallMakananRequest
    ): Response<ZonaResponse>

    @GET("/api/recall-makanan")
    suspend fun getRecallMakanan(
        @Header("Authorization") token: String,
        @Query("tanggal") tanggal: String
    ): Response<RecallMakananDetailResponse>

    @POST("/api/aktivitas-fisik")
    suspend fun submitAktivitasFisik(
        @Header("Authorization") token: String,
        @Body request: AktivitasFisikRequest
    ): Response<ZonaResponse>

    @GET("/api/aktivitas-fisik")
    suspend fun getAktivitasFisik(
        @Header("Authorization") token: String,
        @Query("tanggal") tanggal: String
    ): Response<AktivitasFisikDetailResponse>

    @POST("/api/minum-ttd")
    suspend fun submitMinumTtd(
        @Header("Authorization") token: String,
        @Body request: MinumTtdRequest
    ): Response<ZonaResponse>

    @GET("/api/minum-ttd")
    suspend fun getMinumTtd(
        @Header("Authorization") token: String,
        @Query("tanggal") tanggal: String
    ): Response<MinumTtdDetailResponse>

    @POST("/api/personal-hygiene")
    suspend fun submitPersonalHygiene(
        @Header("Authorization") token: String,
        @Body request: PersonalHygieneRequest
    ): Response<ZonaResponse>

    @GET("/api/personal-hygiene")
    suspend fun getPersonalHygiene(
        @Header("Authorization") token: String,
        @Query("tanggal") tanggal: String
    ): Response<PersonalHygieneDetailResponse>

    @GET("/api/rapor")
    suspend fun getRapor(
        @Header("Authorization") token: String,
        @Query("tanggal") tanggal: String
    ):Response<RaporResponse>

    @POST("/api/tes-kebugaran")
    suspend fun storeTesKebugaran(
        @Header("Authorization") token: String,
        @Body request: TesKebugaranRequest
    ): Response<ZonaResponse>

    @GET("/api/tes-kebugaran/{tipe_tes}")
    suspend fun getTesKebugaran(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("tipe_tes") tipeTes: String
    ): Response<TesKebugaranDetailResponse>

    @GET("/api/leaderboard-fisik")
    suspend fun getLeaderboardAktivitasFisik(
        @Header("Authorization") token: String,
        @Query("lingkup") lingkup: String?=null,
        @Query("sekolah_id") sekolahId: Int? = null,
        @Query("kelas_id") kelasId: Int? = null
    ): Response<LeaderboardResponse>
}