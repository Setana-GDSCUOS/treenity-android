package com.setana.treenity.data.repository

import com.setana.treenity.data.api.TreeApiHelper
import com.setana.treenity.data.api.dto.GetAroundArTreeResponseDTO
import com.setana.treenity.data.api.dto.GetAroundTreeResponseDTO
import com.setana.treenity.data.api.dto.PostTreeResponseDTO
import com.setana.treenity.data.api.dto.PostTreeDTO
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class TreeRepositoryImpl @Inject constructor(
    private val remoteSource: TreeApiHelper
) : TreeRepository {
    override suspend fun getAroundTrees(lat: Double, lng: Double): Response<List<GetAroundTreeResponseDTO>> =
        remoteSource.getAroundTrees(lat, lng)
    override suspend fun getAroundArTrees(lat: Double, lng: Double): Response<List<GetAroundArTreeResponseDTO>> =
        remoteSource.getAroundArTrees(lat, lng)
    override suspend fun postTree(postTreeDTO: PostTreeDTO): Call<PostTreeDTO> =
        remoteSource.postTree(postTreeDTO)
}