package com.setana.treenity.data.api.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GetAroundArTreeResponseDTO(
    @SerializedName("treeId")
    val treeId: Long,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    // val treeName: String,
    @SerializedName("createdDate")
    val createdDate: String,
    // val level: Int,
    @SerializedName("distance")
    val distance: Double,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("username")
    val username: String,
    @SerializedName("cloudAnchorId")
    val cloudAnchorId: String,
    @SerializedName("treeName")
    val treeName: String,
    @SerializedName("level")
    val level: Int
) : Serializable {
    companion object {
        val EMPTY = GetAroundArTreeResponseDTO(0, 0.0, 0.0, "", 0.0, 0, "", "","",0)
    }
}