package com.setana.treenity.data.repository

import com.setana.treenity.data.api.dto.LoginByFirebaseTokenResponseDTO
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import retrofit2.Response

interface UserRepository {
    suspend fun loginByFirebaseToken(): Response<LoginByFirebaseTokenResponseDTO>
    suspend fun registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO: RegisterCurrentFirebaseUserRequestDTO): Response<Void>
}