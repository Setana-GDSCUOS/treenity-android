package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.*
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface TreeApiService {
    @GET("trees")
    suspend fun getAroundTrees(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("userId") userId:Long
    ): Response<List<GetAroundTreeResponseDTO>>

    @POST("users/{id}/trees") // API 상세 경로, format : json
    suspend fun postTree(
        @Path("id") id:Long,
        @Body postTreeRequestDTO: PostTreeRequestDTO
    ): Response<GetTreeInformationResponseDTO>

    // 이제 treeId 로 자신 나무만
    @GET("trees/{id}")
    suspend fun getTreeInformation(
        @Path("id") id:Long,
    ): Response<GetTreeInformationResponseDTO>

    @POST("users/{userId}/trees/{treeId}/interact")
    suspend fun waterTree(
        @Path("userId") userId: Long,
        @Path("treeId") treeId: Long,
        @Body treeRequestRefresh: WaterTreeRequestDTO
    ): Response<Void>

    @PUT("users/{userId}/trees/{treeId}")
    suspend fun putTreeInfo(
        @Path("userId") userId: Long,
        @Path("treeId") treeId: Long,
        @Body putTreeInfoRequestDTO: PutTreeInfoRequestDTO
    ): Response<Void>

    @GET("users/{uid}/trees")
    suspend fun getUserTrees(
        @Path(value = "uid", encoded = true) userId: String
    ): Response<List<MyTreeItem>>

    // TreeListActivity : 받아오는 정보 더 다양
    @GET("users/{uid}/trees")
    suspend fun getTreeData(
        @Path(value = "uid", encoded = true) userId: String
    ) : Response<List<MyTreeItem>>

    @POST("users/{userId}/trees/{treeId}/bookmark")
    suspend fun postTreeBookmark(
        @Path("userId") userId: Long,
        @Path("treeId") treeId: Long,
    ): Response<Void>

    @DELETE("users/{userId}/trees/{treeId}/bookmark")
    suspend fun deleteTreeBookmark(
        @Path("userId") userId: Long,
        @Path("treeId") treeId: Long,
    ): Response<Void>
}