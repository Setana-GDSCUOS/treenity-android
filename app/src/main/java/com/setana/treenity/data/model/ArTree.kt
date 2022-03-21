package com.setana.treenity.data.model

import com.setana.treenity.data.api.dto.GetAroundTreeResponseDTO
import java.io.Serializable
import java.util.*

data class ArTree(
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
        val EMPTY = ArTree(0, 0.0, 0.0, "","",0, 0.0, 0, "")
    }
}
