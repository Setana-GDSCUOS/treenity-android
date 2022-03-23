package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.GetUserItemResponseDTO
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import com.setana.treenity.data.api.dto.mypage.user.User
import com.setana.treenity.data.api.dto.mypage.walklog.WalkLog
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class UserApiHelperImpl @Inject constructor(
    private val userApiService: UserApiService
): UserApiHelper {
    override suspend fun loginByFirebaseToken() = userApiService.loginByFirebaseToken()
    override suspend fun registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO: RegisterCurrentFirebaseUserRequestDTO) = userApiService.registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO)
    override suspend fun updateUserWalkLogs(userId:String, updateUserWalkLogsRequestDTO: UpdateUserWalkLogsRequestDTO) = userApiService.updateUserWalkLogs(userId, updateUserWalkLogsRequestDTO)


    override suspend fun getUserData(userId: String): Response<User> = userApiService.getUserData(userId)
    override suspend fun getUserWalkLogs(userId: String): Response<List<WalkLog>> = userApiService.getUserWalkLogs(userId)
    override suspend fun changeUserName(userId: String, username: String): Response<Void> = userApiService.changeUserName(userId, username)

    override suspend fun getUserItems(userId: Long): Response<List<GetUserItemResponseDTO>> = userApiService.getUserItems(userId)

}