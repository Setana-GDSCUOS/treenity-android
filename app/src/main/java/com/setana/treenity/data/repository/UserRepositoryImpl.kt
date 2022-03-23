package com.setana.treenity.data.repository

import com.setana.treenity.TreenityApplication.Companion.DAILY_WALK_LOG
import com.setana.treenity.data.api.UserApiHelper
import com.setana.treenity.data.api.dto.GetUserItemResponseDTO
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import com.setana.treenity.data.api.dto.mypage.user.User
import com.setana.treenity.data.api.dto.mypage.walklog.WalkLog
import retrofit2.Call
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

    override suspend fun getWalkLogs(): HashMap<String, String> {

        return DAILY_WALK_LOG
    }



    override suspend fun getUserData(userId: String): Response<User> =
        remoteSource.getUserData(userId)

    override suspend fun getUserWalkLogs(userId: String): Response<List<WalkLog>> =
        remoteSource.getUserWalkLogs(userId)

    override suspend fun changeUserName(userId: String, username: String): Response<Void> =
        remoteSource.changeUserName(userId, username)

    override suspend fun getUserItems(userId:Long): Response<List<GetUserItemResponseDTO>> =
        remoteSource.getUserItems(userId)
}