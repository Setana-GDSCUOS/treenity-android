package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.GetAroundTreeResponseDTO
import retrofit2.Response

interface TreeApiHelper {
    suspend fun getAroundTrees(lat: Double, lng: Double): Response<List<GetAroundTreeResponseDTO>>
}