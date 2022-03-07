package com.setana.treenity.data.api.dto

import java.io.Serializable

data class GetAroundArTreeResponseDTO(
    val treeId: Long,
    val latitude: Double,
    val longitude: Double,
    // val treeName: String,
    val createdDate: String,
    // val level: Int,
    val distance: Double,
    val userId: Long,
    val username: String,
    val cloudAnchorID: String
) : Serializable {
    companion object {
        val EMPTY = GetAroundArTreeResponseDTO(0, 0.0, 0.0, "", 0.0, 0, "", "")
    }
}