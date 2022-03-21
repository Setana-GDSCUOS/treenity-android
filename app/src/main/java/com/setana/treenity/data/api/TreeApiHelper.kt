package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.*
import retrofit2.Response

interface TreeApiHelper {
    suspend fun getAroundTrees(lat: Double, lng: Double, userId:Long): Response<List<GetAroundTreeResponseDTO>>
    suspend fun postTree(userId:Long, postTreeRequestDTO: PostTreeRequestDTO): Response<Void>
    suspend fun getTreeInformation(treeId: Long): Response<GetTreeInformationResponseDTO>
    suspend fun waterTree(userId: Long,treeId: Long,waterTreeRequestDTO: WaterTreeRequestDTO): Response<Void>
    suspend fun putTreeInfo(userId:Long, treeId:Long, putTreeInfoRequestDTO:PutTreeInfoRequestDTO): Response<Void>
}