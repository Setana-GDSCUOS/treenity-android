package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.GetTreeInformationDTO
import com.setana.treenity.data.api.dto.PostTreeDTO
import com.setana.treenity.data.api.dto.PutTreeInfoDTO
import com.setana.treenity.data.api.dto.WaterTreeDTO
import retrofit2.Response
import javax.inject.Inject

class TreeApiHelperImpl @Inject constructor(
    private val treeApiService: TreeApiService
): TreeApiHelper {
    override suspend fun getAroundTrees(lat: Double, lng: Double, userId:Long) = treeApiService.getAroundTrees(lat, lng, userId)
    override suspend fun postTree(postTreeDTO: PostTreeDTO): Response<Void> = treeApiService.postTree(postTreeDTO)
    override suspend fun getTreeInformation(id:Long ,treeId: Long, userId:Long): Response<GetTreeInformationDTO> = treeApiService.getTreeInformation(id,treeId, userId)
    override suspend fun waterTree(id:Long ,waterTreeDTO: WaterTreeDTO): Response<Void> = treeApiService.waterTree(id,waterTreeDTO)
    override suspend fun putTreeInfo(userId: Long, treeId: Long, putTreeInfoDTO: PutTreeInfoDTO) = treeApiService.putTreeInfo(userId,treeId,putTreeInfoDTO)
}