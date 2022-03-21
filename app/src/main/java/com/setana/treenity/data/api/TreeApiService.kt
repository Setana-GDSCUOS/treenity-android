package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TreeApiService {
    @GET("trees")   // API 상세 경로, format : json
    suspend fun getAroundTrees(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("userId") userId:Long
    ): Response<List<GetAroundTreeResponseDTO>>

    @Headers("Content-Type: application/json")
    @POST("users/{id}/trees") // API 상세 경로, format : json
    suspend fun postTree(
        @Path("id") id:Long,
        @Body postTreeRequestDTO: PostTreeRequestDTO
    ): Response<Void>

    // 이제 treeId 로 자신 나무만
    @GET("trees/{id}")
    suspend fun getTreeInformation(
        @Path("id") id:Long,
    ): Response<GetTreeInformationResponseDTO>

    @Headers("Content-Type: application/json")
    @POST("users/{userId}/trees/{treeId}/interact")
    suspend fun waterTree(
        @Path("userId") userId: Long,
        @Path("treeId") treeId: Long,
        @Body treeRequestRefresh: WaterTreeRequestDTO
    ): Response<Void>

    @Headers("Content-Type: application/json")
    @PUT("users/{userId}/trees/{treeId}")
    suspend fun putTreeInfo(
        @Path("userId") userId: Long,
        @Path("treeId") treeId: Long,
        @Body putTreeInfoRequestDTO: PutTreeInfoRequestDTO
    ): Response<Void>
}