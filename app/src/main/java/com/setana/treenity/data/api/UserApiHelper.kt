package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.GetUserItemResponseDTO
import com.setana.treenity.data.api.dto.LoginByFirebaseTokenResponseDTO
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import com.setana.treenity.data.api.dto.mypage.user.User
import com.setana.treenity.data.api.dto.mypage.walklog.WalkLog
import retrofit2.Call
import retrofit2.Response

interface UserApiHelper {
    suspend fun loginByFirebaseToken(): Response<LoginByFirebaseTokenResponseDTO>
    suspend fun registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO: RegisterCurrentFirebaseUserRequestDTO): Response<Void>
    suspend fun updateUserWalkLogs(
        userId: String,
        updateUserWalkLogsRequestDTO: UpdateUserWalkLogsRequestDTO
    ): Response<Void>

    suspend fun getUserData(userId: String): Response<User>
    suspend fun getUserWalkLogs(userId: String): Response<List<WalkLog>>
    suspend fun changeUserName(userId: String, username: String): Response<Void>
    suspend fun getUserItems(userId:Long):Response<List<GetUserItemResponseDTO>>
}