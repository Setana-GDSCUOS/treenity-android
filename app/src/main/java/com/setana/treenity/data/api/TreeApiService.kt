package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TreeApiService {
    @GET("trees")
    suspend fun getAroundTrees(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("userId") userId:Long
    ): Response<List<GetAroundTreeResponseDTO>>

    @POST("trees")
    suspend fun postTree(@Body postTreeDTO: PostTreeDTO): Response<Void>

    // userId를 제시하고 나무 정보를 받아오는 것
    @GET("trees/{id}")
    suspend fun getTreeInformation(
        @Path("id") id:Long,
        @Query("id") treeId: Long,
        @Query("userId") userId:Long
    ): Response<GetTreeInformationDTO>

    @PUT("trees/{id}/interact")
    suspend fun waterTree(
        @Path("id") id: Long,
        @Body treeRefresh: WaterTreeDTO
    ): Response<Void>

    @PUT("users/{userId}/trees/{treeId}")
    suspend fun putTreeInfo(
        @Path("userId") userId: Long,
        @Path("treeId") treeId: Long,
        @Body putTreeInfoDTO: PutTreeInfoDTO
    ): Response<Void>
}