package com.setana.treenity.data.repository

import com.setana.treenity.data.api.UserApiHelper
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import retrofit2.Response
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val remoteSource: UserApiHelper
) : UserRepository {
    override suspend fun loginByFirebaseToken() = remoteSource.loginByFirebaseToken()
    override suspend fun registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO: RegisterCurrentFirebaseUserRequestDTO) =
        remoteSource.registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO)
    override suspend fun updateUserWalkLogs(
        userId: String,
        updateUserWalkLogsRequestDTO: UpdateUserWalkLogsRequestDTO
    ): Response<Void> = remoteSource.updateUserWalkLogs(userId, updateUserWalkLogsRequestDTO)
}