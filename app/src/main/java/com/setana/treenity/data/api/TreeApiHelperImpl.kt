package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.*
import retrofit2.Response
import javax.inject.Inject

class TreeApiHelperImpl @Inject constructor(
    private val treeApiService: TreeApiService
): TreeApiHelper {
    override suspend fun getAroundTrees(lat: Double, lng: Double, userId:Long) = treeApiService.getAroundTrees(lat, lng, userId)
    //override suspend fun getAroundArTrees(lat: Double, lng: Double, userId:Long) = treeApiService.getAroundArTrees(lat, lng, userId)
    override suspend fun postTree(userId: Long, postTreeRequestDTO: PostTreeRequestDTO): Response<Void> = treeApiService.postTree(userId,postTreeRequestDTO)
    override suspend fun getTreeInformation(treeId: Long): Response<GetTreeInformationResponseDTO> = treeApiService.getTreeInformation(treeId)
    override suspend fun waterTree(userId: Long,treeId: Long,waterTreeRequestDTO: WaterTreeRequestDTO): Response<Void> = treeApiService.waterTree(userId,treeId,waterTreeRequestDTO)
    override suspend fun putTreeInfo(userId: Long, treeId: Long, putTreeInfoRequestDTO: PutTreeInfoRequestDTO) = treeApiService.putTreeInfo(userId,treeId,putTreeInfoRequestDTO)
}