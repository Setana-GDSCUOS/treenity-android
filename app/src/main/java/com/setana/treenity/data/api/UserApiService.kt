package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.LoginByFirebaseTokenResponseDTO
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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
}