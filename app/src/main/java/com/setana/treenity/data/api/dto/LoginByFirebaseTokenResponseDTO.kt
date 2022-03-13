package com.setana.treenity.data.api.dto

import java.io.Serializable

data class LoginByFirebaseTokenResponseDTO(
    val userId: Long,
    val uid: String,
    val email: String,
    val username: String
): Serializable
