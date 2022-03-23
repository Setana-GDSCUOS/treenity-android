package com.setana.treenity.data.api

import androidx.lifecycle.MutableLiveData
import com.setana.treenity.data.api.dto.*
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class TreeApiHelperImpl @Inject constructor(
    private val treeApiService: TreeApiService
): TreeApiHelper {
    override suspend fun getAroundTrees(lat: Double, lng: Double, userId:Long) = treeApiService.getAroundTrees(lat, lng, userId)

//     override suspend fun getAroundArTrees(lat: Double, lng: Double, userId:Long) = treeApiService.getAroundArTrees(lat, lng, userId)
//     override suspend fun postTree(postTreeDTO: PostTreeDTO): Response<Void> = treeApiService.postTree(postTreeDTO)
//     override suspend fun getTreeInformation(id:Long ,treeId: Long, userId:Long): Response<GetTreeInformationDTO> = treeApiService.getTreeInformation(id,treeId, userId)
//     override suspend fun waterTree(id:Long ,waterTreeDTO: WaterTreeDTO): Response<Void> = treeApiService.waterTree(id,waterTreeDTO)
//     override suspend fun putTreeInfo(userId: Long, treeId: Long, putTreeInfoDTO: PutTreeInfoDTO) = treeApiService.putTreeInfo(userId,treeId,putTreeInfoDTO)

    override suspend fun getUserTrees(userId: String): Response<List<MyTreeItem>> = treeApiService.getUserTrees(userId)
    override suspend fun getTreeData(userId: String): Response<List<MyTreeItem>> = treeApiService.getTreeData(userId)

    override suspend fun postTree(userId: Long, postTreeRequestDTO: PostTreeRequestDTO): Response<Void> = treeApiService.postTree(userId,postTreeRequestDTO)
    override suspend fun getTreeInformation(treeId: Long): Response<GetTreeInformationResponseDTO> = treeApiService.getTreeInformation(treeId)
    override suspend fun waterTree(userId: Long,treeId: Long,waterTreeRequestDTO: WaterTreeRequestDTO): Response<Void> = treeApiService.waterTree(userId,treeId,waterTreeRequestDTO)
    override suspend fun putTreeInfo(userId: Long, treeId: Long, putTreeInfoRequestDTO: PutTreeInfoRequestDTO) = treeApiService.putTreeInfo(userId,treeId,putTreeInfoRequestDTO)
}