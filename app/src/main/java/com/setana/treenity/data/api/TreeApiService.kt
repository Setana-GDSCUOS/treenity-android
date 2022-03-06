package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.GetAroundTreeResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TreeApiService {
    @GET("trees")   // API 상세 경로, format : json
    suspend fun getAroundTrees(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double
    ): Response<List<GetAroundTreeResponseDTO>>
}