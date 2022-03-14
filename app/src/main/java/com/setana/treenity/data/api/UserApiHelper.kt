package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.LoginByFirebaseTokenResponseDTO
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import retrofit2.Response

interface UserApiHelper {
    suspend fun loginByFirebaseToken(): Response<LoginByFirebaseTokenResponseDTO>
    suspend fun registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO: RegisterCurrentFirebaseUserRequestDTO): Response<Void>
    suspend fun updateUserWalkLogs(
        userId: String,
        updateUserWalkLogsRequestDTO: UpdateUserWalkLogsRequestDTO
    ): Response<Void>
}