package com.jarimanis.jarimanis.data.repository

import com.jarimanis.jarimanis.data.model.RaporResponse
import com.jarimanis.jarimanis.data.network.AktivitasFisikRequest
import com.jarimanis.jarimanis.data.network.LeaderboardResponse
import com.jarimanis.jarimanis.data.network.MinumTtdRequest
import com.jarimanis.jarimanis.data.network.PersonalHygieneRequest
import com.jarimanis.jarimanis.data.network.PostTestRequest
import com.jarimanis.jarimanis.data.network.PreTestRequest
import com.jarimanis.jarimanis.data.network.RecallMakananRequest
import com.jarimanis.jarimanis.data.network.TesKebugaranRequest
import com.jarimanis.jarimanis.data.network.ZonaApi
import com.jarimanis.jarimanis.utils.Resource

class ZonaRepository(private val api: ZonaApi) {

    private fun formatToken(token: String): String {
        return if (token.startsWith("Bearer ")) token else "Bearer $token"
    }

    suspend fun submitPreTest(token: String, request: PreTestRequest) =
        api.submitPreTest(formatToken(token), request)

    suspend fun submitPostTest(token: String, request: PostTestRequest) =
        api.submitPostTest(formatToken(token), request)

    suspend fun submitRecallMakanan(token: String, request: RecallMakananRequest) =
        api.submitRecallMakanan(formatToken(token), request)

    suspend fun submitAktivitasFisik(token: String, request: AktivitasFisikRequest) =
        api.submitAktivitasFisik(formatToken(token), request)

    suspend fun submitMinumTtd(token: String, request: MinumTtdRequest) =
        api.submitMinumTtd(formatToken(token), request)

    suspend fun submitPersonalHygiene(token: String, request: PersonalHygieneRequest) =
        api.submitPersonalHygiene(formatToken(token), request)

    // Tambahkan di dalam class ZonaRepository
    suspend fun getRapor(token: String, tanggal: String): Resource<RaporResponse> {
        return try {
            // Memanggil API dengan menyertakan token dan filter tanggal
            val response = api.getRapor(formatToken(token), tanggal)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Gagal mengambil data rapor")
            }
        } catch (e: Exception) {
            // Tetap mempertahankan e.localizedMessage bawaan Anda agar pesan error lebih detail
            Resource.Error(e.message ?: "Koneksi bermasalah: ${e.localizedMessage}")
        }
    }

    // ==========================================
    // FUNGSI TES KEBUGARAN
    // ==========================================
    suspend fun submitTesKebugaran(token: String, request: TesKebugaranRequest) =
        api.storeTesKebugaran(formatToken(token), request)

    // Tambahkan parameter lingkup
    suspend fun getLeaderboardAktivitasFisik(token: String, lingkup: String? = null, sekolahId: Int? = null, kelasId: Int? = null): Resource<LeaderboardResponse> {
        return try {
            val response = api.getLeaderboardAktivitasFisik(formatToken(token), lingkup, sekolahId, kelasId) // <--- SISIPKAN DI SINI
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Gagal mengambil data leaderboard")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Koneksi bermasalah: ${e.localizedMessage}")
        }
    }

    suspend fun getTesKebugaran(token: String, tipeTes: String): Resource<com.jarimanis.jarimanis.data.model.TesKebugaranDetailResponse> {
        return try {
            val response = api.getTesKebugaran(formatToken(token), tipeTes)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Gagal mengambil data")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Koneksi bermasalah: ${e.localizedMessage}")
        }
    }
}