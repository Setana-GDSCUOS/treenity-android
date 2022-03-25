package com.setana.treenity.data.api.dto

import com.setana.treenity.data.model.SeedItem
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
    val user: GetAroundTreeResponseUserDTO,
    //val item: SeedItem
) : Serializable

data class GetAroundTreeResponseUserDTO(
    val userId: Long,
    val username: String
) : Serializable
