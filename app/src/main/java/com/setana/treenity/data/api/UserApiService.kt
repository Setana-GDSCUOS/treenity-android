package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.LoginByFirebaseTokenResponseDTO
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApiService {
    @GET("auth")
    suspend fun loginByFirebaseToken(): Response<LoginByFirebaseTokenResponseDTO>

    @POST("auth")
    suspend fun registerCurrentFirebaseUser(
        @Body registerCurrentFirebaseUserRequestDTO: RegisterCurrentFirebaseUserRequestDTO
    ): Response<Void>
}