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
    //override suspend fun getAroundArTrees(lat: Double, lng: Double, userId:Long): Response<List<GetAroundArTreeResponseDTO>> =
        //.getAroundArTrees(lat, lng, userId)
    override suspend fun postTree(userId: Long, postTreeRequestDTO: PostTreeRequestDTO): Response<Void> =
        remoteSource.postTree(userId,postTreeRequestDTO)
    override suspend fun getTreeInformation(treeId: Long): Response<GetTreeInformationResponseDTO> =
        remoteSource.getTreeInformation(treeId)
    override suspend fun waterTree(userId:Long,treeId: Long, waterTreeRequestDTO: WaterTreeRequestDTO): Response<Void> =
        remoteSource.waterTree(userId, treeId,waterTreeRequestDTO)
    override suspend fun putTreeInfo(userId: Long, treeId: Long, putTreeInfoRequestDTO:PutTreeInfoRequestDTO): Response<Void> =
        remoteSource.putTreeInfo(userId,treeId,putTreeInfoRequestDTO)
}