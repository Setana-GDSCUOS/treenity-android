package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface TreeApiService {
    @GET("trees")   // API 상세 경로, format : json
    suspend fun getAroundTrees(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("userId") userId:Long
    ): Response<List<GetAroundTreeResponseDTO>>


    @GET("trees")   // API 상세 경로, format : json
    suspend fun getAroundArTrees(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("userId") userId:Long
    ): Response<List<GetAroundArTreeResponseDTO>>

    @Headers("Content-Type: application/json")
    @POST("trees") // API 상세 경로, format : json
    suspend fun postTree(@Body postTreeDTO: PostTreeDTO): Response<Void>

    // userId를 제시하고 나무 정보를 받아오는 것
    @GET("trees/{id}")
    suspend fun getTreeInformation(
        @Path("id") id:Long,
        @Query("id") treeId: Long,
        @Query("userId") userId:Long
    ): Response<GetTreeInformationDTO>

    @Headers("Content-Type: application/json")
    @PUT("trees/{id}/interact")
    suspend fun waterTree(
        @Path("id") id: Long,
        @Body treeRefresh: WaterTreeDTO
    ): Response<Void>

    @Headers("Content-Type: application/json")
    @PUT("users/{userId}/trees/{treeId}")
    suspend fun putTreeInfo(
        @Path("userId") userId: Long,
        @Path("treeId") treeId: Long,
        @Body putTreeInfoDTO: PutTreeInfoDTO
    ): Response<Void>
}