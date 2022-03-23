package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.*
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import retrofit2.Response

interface TreeApiHelper {
    suspend fun getAroundTrees(lat: Double, lng: Double, userId:Long): Response<List<GetAroundTreeResponseDTO>>

//     suspend fun getAroundArTrees(lat: Double, lng: Double, userId:Long): Response<List<GetAroundArTreeResponseDTO>>
//     suspend fun postTree(postTreeDTO: PostTreeDTO): Response<Void>
//     suspend fun getTreeInformation(id:Long, treeId: Long, userId:Long): Response<GetTreeInformationDTO>
//     suspend fun waterTree(id:Long, waterTreeDTO: WaterTreeDTO): Response<Void>
//     suspend fun putTreeInfo(userId:Long,treeId:Long,putTreeInfoDTO:PutTreeInfoDTO): Response<Void>

    suspend fun getUserTrees(userId: Long): Response<ArrayList<MyTreeItem>>
    suspend fun getTreeData(userId: Long): Response<List<MyTreeItem>>

    suspend fun postTree(userId:Long, postTreeRequestDTO: PostTreeRequestDTO): Response<Void>
    suspend fun getTreeInformation(treeId: Long): Response<GetTreeInformationResponseDTO>
    suspend fun waterTree(userId: Long,treeId: Long,waterTreeRequestDTO: WaterTreeRequestDTO): Response<Void>
    suspend fun putTreeInfo(userId:Long, treeId:Long, putTreeInfoRequestDTO:PutTreeInfoRequestDTO): Response<Void>
    suspend fun postTreeBookmark(userId: Long, treeId: Long): Response<Void>
    suspend fun deleteTreeBookmark(userId: Long, treeId: Long): Response<Void>
}