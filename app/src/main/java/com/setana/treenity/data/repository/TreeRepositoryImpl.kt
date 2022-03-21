package com.setana.treenity.data.repository

import com.setana.treenity.data.api.TreeApiHelper
import com.setana.treenity.data.api.dto.*
import retrofit2.Response
import javax.inject.Inject

class TreeRepositoryImpl @Inject constructor(
    private val remoteSource: TreeApiHelper
) : TreeRepository {
    override suspend fun getAroundTrees(lat: Double, lng: Double, userId:Long): Response<List<GetAroundTreeResponseDTO>> =
        remoteSource.getAroundTrees(lat, lng, userId)
    override suspend fun postTree(postTreeDTO: PostTreeDTO): Response<Void> =
        remoteSource.postTree(postTreeDTO)
    override suspend fun getTreeInformation(id:Long,treeId: Long, userId:Long): Response<GetTreeInformationDTO> =
        remoteSource.getTreeInformation(id,treeId, userId)
    override suspend fun waterTree(id:Long, waterTreeDTO: WaterTreeDTO): Response<Void> =
        remoteSource.waterTree(id,waterTreeDTO)
    override suspend fun putTreeInfo(userId: Long, treeId: Long, putTreeInfoDTO:PutTreeInfoDTO): Response<Void> =
        remoteSource.putTreeInfo(userId,treeId,putTreeInfoDTO)
}