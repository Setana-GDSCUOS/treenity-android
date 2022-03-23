package com.setana.treenity.data.repository

import androidx.lifecycle.MutableLiveData
import com.setana.treenity.data.api.dto.*
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import retrofit2.Call
import retrofit2.Response

interface TreeRepository {
    suspend fun getAroundTrees(lat: Double, lng: Double, userId:Long): Response<List<GetAroundTreeResponseDTO>>

//     suspend fun getAroundArTrees(lat: Double, lng: Double,userId:Long): Response<List<GetAroundArTreeResponseDTO>>
//     suspend fun postTree(postTreeDTO: PostTreeDTO) : Response<Void>
//     suspend fun getTreeInformation(id:Long,treeId:Long,userId:Long): Response<GetTreeInformationDTO>
//     suspend fun waterTree(id:Long, waterTreeDTO: WaterTreeDTO): Response<Void>
//     suspend fun putTreeInfo(userId:Long,treeId:Long,putTreeInfoDTO:PutTreeInfoDTO):Response<Void>

    suspend fun getUserTrees(userId:String): Response<List<MyTreeItem>> // MyPageActivity
    suspend fun getTreeData(userId:String): Response<List<MyTreeItem>> // TreeListActivity

    //suspend fun getAroundArTrees(lat: Double, lng: Double,userId:Long): Response<List<GetAroundArTreeResponseDTO>>
    suspend fun postTree(userId: Long, postTreeRequestDTO: PostTreeRequestDTO) : Response<Void>
    suspend fun getTreeInformation(treeId:Long): Response<GetTreeInformationResponseDTO>
    suspend fun waterTree(userId:Long,treeId: Long, waterTreeRequestDTO: WaterTreeRequestDTO): Response<Void>
    suspend fun putTreeInfo(userId:Long, treeId:Long, putTreeInfoRequestDTO:PutTreeInfoRequestDTO):Response<Void>

}