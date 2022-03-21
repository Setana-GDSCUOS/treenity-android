package com.setana.treenity.data.repository

import com.setana.treenity.data.api.dto.LoginByFirebaseTokenResponseDTO
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import com.setana.treenity.data.api.dto.mypage.user.User
import com.setana.treenity.data.api.dto.mypage.walklog.WalkLog
import retrofit2.Call
import retrofit2.Response

interface UserRepository {
    suspend fun loginByFirebaseToken(): Response<LoginByFirebaseTokenResponseDTO>
    suspend fun registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO: RegisterCurrentFirebaseUserRequestDTO): Response<Void>
    suspend fun updateUserWalkLogs(userId: String, updateUserWalkLogsRequestDTO: UpdateUserWalkLogsRequestDTO): Response<Void>
    suspend fun getWalkLogs(): HashMap<String, String>

    suspend fun getUserData(userId:String): Response<User>
    suspend fun getUserWalkLogs(userId:String): Response<List<WalkLog>>
    suspend fun changeUserName(userId:String, user:User): Response<User>
}