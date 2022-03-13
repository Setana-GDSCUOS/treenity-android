package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import javax.inject.Inject

class UserApiHelperImpl @Inject constructor(
    private val userApiService: UserApiService
): UserApiHelper {
    override suspend fun loginByFirebaseToken() = userApiService.loginByFirebaseToken()
    override suspend fun registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO: RegisterCurrentFirebaseUserRequestDTO) = userApiService.registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO)
}