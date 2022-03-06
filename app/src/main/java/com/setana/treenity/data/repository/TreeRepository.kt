package com.setana.treenity.data.repository

import com.setana.treenity.data.api.dto.GetAroundTreeResponseDTO
import retrofit2.Response

interface TreeRepository {
    suspend fun getAroundTrees(lat: Double, lng: Double): Response<List<GetAroundTreeResponseDTO>>
}