package com.setana.treenity.data.api.dto

import java.io.Serializable

data class GetAroundTreeResponseDTO(
    val treeId: Long,
    val latitude: Double,
    val longitude: Double,
    val treeName: String,
    val createdDate: String,
    val level: Int,
    val distance: Double,
    val userId: Long,
    val username: String
) : Serializable {
    companion object {
        val EMPTY = GetAroundTreeResponseDTO(0, 0.0, 0.0, "","",0, 0.0, 0, "")
    }
}