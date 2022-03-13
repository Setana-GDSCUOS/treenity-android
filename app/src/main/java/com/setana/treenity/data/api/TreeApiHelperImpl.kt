package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.PostTreeDTO
import com.setana.treenity.data.api.dto.PostTreeResponseDTO
import retrofit2.Response
import javax.inject.Inject

class TreeApiHelperImpl @Inject constructor(
    private val treeApiService: TreeApiService
): TreeApiHelper {
    override suspend fun getAroundTrees(lat: Double, lng: Double) = treeApiService.getAroundTrees(lat, lng)
    override suspend fun getAroundArTrees(lat: Double, lng: Double) = treeApiService.getAroundArTrees(lat, lng)
    override suspend fun postTree(postTreeDTO: PostTreeDTO): Response<PostTreeResponseDTO> = treeApiService.postTree(postTreeDTO)
    override suspend fun waterTree(treeId: Long) {
        TODO("Not yet implemented")
    }
}