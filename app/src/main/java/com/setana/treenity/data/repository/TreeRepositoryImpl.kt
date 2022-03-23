package com.setana.treenity.data.repository

import com.setana.treenity.data.api.TreeApiHelper
import com.setana.treenity.data.api.dto.*
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem

import retrofit2.Response
import javax.inject.Inject

class TreeRepositoryImpl @Inject constructor(
    private val remoteSource: TreeApiHelper
) : TreeRepository {
    override suspend fun getAroundTrees(
        lat: Double,
        lng: Double,
        userId: Long
    ): Response<List<GetAroundTreeResponseDTO>> =
        remoteSource.getAroundTrees(lat, lng, userId)

//     override suspend fun getAroundArTrees(lat: Double, lng: Double, userId:Long): Response<List<GetAroundArTreeResponseDTO>> =
//         remoteSource.getAroundArTrees(lat, lng, userId)
//     override suspend fun postTree(postTreeDTO: PostTreeDTO): Response<Void> =
//         remoteSource.postTree(postTreeDTO)
//     override suspend fun getTreeInformation(id:Long,treeId: Long, userId:Long): Response<GetTreeInformationDTO> =
//         remoteSource.getTreeInformation(id,treeId, userId)
//     override suspend fun waterTree(id:Long, waterTreeDTO: WaterTreeDTO): Response<Void> =
//         remoteSource.waterTree(id,waterTreeDTO)
//     override suspend fun putTreeInfo(userId: Long, treeId: Long, putTreeInfoDTO:PutTreeInfoDTO): Response<Void> =
//         remoteSource.putTreeInfo(userId,treeId,putTreeInfoDTO)


    override suspend fun getUserTrees(userId: Long): Response<ArrayList<MyTreeItem>> =
        remoteSource.getUserTrees(userId)

    override suspend fun getTreeData(userId: Long): Response<List<MyTreeItem>> =
        remoteSource.getTreeData(userId)

    override suspend fun postTree(
        userId: Long,
        postTreeRequestDTO: PostTreeRequestDTO
    ): Response<Void> =
        remoteSource.postTree(userId, postTreeRequestDTO)

    override suspend fun getTreeInformation(treeId: Long): Response<GetTreeInformationResponseDTO> =
        remoteSource.getTreeInformation(treeId)

    override suspend fun waterTree(
        userId: Long,
        treeId: Long,
        waterTreeRequestDTO: WaterTreeRequestDTO
    ): Response<Void> =
        remoteSource.waterTree(userId, treeId, waterTreeRequestDTO)

    override suspend fun putTreeInfo(
        userId: Long,
        treeId: Long,
        putTreeInfoRequestDTO: PutTreeInfoRequestDTO
    ): Response<Void> =
        remoteSource.putTreeInfo(userId, treeId, putTreeInfoRequestDTO)

    override suspend fun postTreeBookmark(userId: Long, treeId: Long): Response<Void> =
        remoteSource.postTreeBookmark(userId, treeId)

    override suspend fun deleteTreeBookmark(userId: Long, treeId: Long): Response<Void> =
        remoteSource.deleteTreeBookmark(userId, treeId)
}