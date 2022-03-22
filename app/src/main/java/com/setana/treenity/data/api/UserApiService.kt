package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.LoginByFirebaseTokenResponseDTO
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeResponse
import com.setana.treenity.data.api.dto.mypage.user.User
import com.setana.treenity.data.api.dto.mypage.walklog.WalkLog
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    @GET("auth")
    suspend fun loginByFirebaseToken(): Response<LoginByFirebaseTokenResponseDTO>

    @POST("auth")
    suspend fun registerCurrentFirebaseUser(
        @Body registerCurrentFirebaseUserRequestDTO: RegisterCurrentFirebaseUserRequestDTO
    ): Response<Void>

    @POST("/users/{uid}/walk-logs")
    suspend fun updateUserWalkLogs(
        @Path(value = "uid", encoded = true) userId: String,
        @Body updateUserWalkLogsRequestDTO: UpdateUserWalkLogsRequestDTO
    ): Response<Void>

    @GET("users/{uid}")
    suspend fun getUserData(
        @Path(value = "uid", encoded = true) userId: String
    ): Response<User>

    @GET("users/{uid}/walk-logs")
    suspend fun getUserWalkLogs(
        @Path(value = "uid", encoded = true) userId: String
    ) : Response<List<WalkLog>>


    @Headers("Content-Type: application/json")
    @PUT("users/{uid}")
    suspend fun changeUserName(
        @Path(value = "uid", encoded = true) userId: String,
        @Body username: String
    ) : Response<Void>

}