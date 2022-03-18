package com.setana.treenity.data.repository

import com.setana.treenity.data.api.dto.*
import retrofit2.Call
import retrofit2.Response

interface TreeRepository {
    suspend fun getAroundTrees(lat: Double, lng: Double): Response<List<GetAroundTreeResponseDTO>>
    suspend fun getAroundArTrees(lat: Double, lng: Double): Response<List<GetAroundArTreeResponseDTO>>
    suspend fun postTree(postTreeDTO: PostTreeDTO) : Response<Void>
}