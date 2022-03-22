package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.mypage.tree.MyTreeResponse
import com.setana.treenity.data.api.dto.mypage.user.User
import com.setana.treenity.data.api.dto.mypage.walklog.WalkLog
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface MyPageApiService {

    // User 부분
//    @GET("users/{uid}")
//    suspend fun getUserData(
//        @Path(value = "uid", encoded = true) userId: Long
//    ): Response<User>

    // MyPageActivity
//    @GET("users/{uid}/walk-logs")
//    suspend fun getUserWalkLogs(
//        @Path(value = "uid", encoded = true) userId: Long
//    ) : Response<List<WalkLog>>

//    @GET("users/{uid}/trees")
//    suspend fun getUserTrees(
//        @Path(value = "uid", encoded = true) userId: Long
//    ): Response<MyTreeResponse>

//    // TreeListActivity
//    @GET("users/{uid}/trees")
//    fun getTreeData(
//        @Path(value = "uid", encoded = true) userId: String
//    ) : Call<MyTreeResponse>

//    // StoreActivity TODO: 상점 페이지 pr 날릴 때 StoreApiService 로 구현할 것
//    @GET("items")
//    suspend fun getStoreData() : Response<StoreResponse>
//
//    // 상점에서 아이템 구매했을 때 POST
//    @Headers("Content-Type: application/json")
//    @POST("users/{uid}/items")
//    fun pushTreeItem(
//        @Path(value = "uid", encoded = true) userId: String,
//        @Body postItem: StoreItem
//    ) : Call<StoreItem>

//    // 환경설정 페이지에서 이름 바꿨을 때 PUT
//    @Headers("Content-Type: application/json")
//    @PUT("users/{uid}")
//    fun changeName(
//        @Path(value = "uid", encoded = true) userId: String,
//        @Body user: User
//    ) : Call<User>
}