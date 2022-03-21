package com.setana.treenity.data.repository

import com.setana.treenity.data.api.dto.*
import retrofit2.Call
import retrofit2.Response

interface TreeRepository {
    suspend fun getAroundTrees(lat: Double, lng: Double, userId:Long): Response<List<GetAroundTreeResponseDTO>>
    suspend fun getAroundArTrees(lat: Double, lng: Double,userId:Long): Response<List<GetAroundArTreeResponseDTO>>
    suspend fun postTree(postTreeDTO: PostTreeDTO) : Response<Void>
    suspend fun getTreeInformation(id:Long,treeId:Long,userId:Long): Response<GetTreeInformationDTO>
    suspend fun waterTree(id:Long, waterTreeDTO: WaterTreeDTO): Response<Void>
    suspend fun putTreeInfo(userId:Long,treeId:Long,putTreeInfoDTO:PutTreeInfoDTO):Response<Void>
}