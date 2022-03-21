package com.setana.treenity.data.api.dto

import java.io.Serializable

data class GetAroundTreeResponseDTO(
    val bookmark: Boolean,
    val cloudAnchorId: String,
    val createdDate: String,
    val distance: Double,
    val latitude: Double,
    val level: Int,
    val longitude: Double,
    val treeId: Long,
    val treeName: String,
    val user: GetAroundTreeResponseUserDTO
) : Serializable

data class GetAroundTreeResponseUserDTO(
    val userId: Long,
    val username: String
) : Serializable
