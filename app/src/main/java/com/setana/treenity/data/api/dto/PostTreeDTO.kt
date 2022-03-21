package com.setana.treenity.data.api.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PostTreeDTO (
    @SerializedName("cloudAnchorId")
    val cloudAnchorId: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("treeName")
    val treeName : String,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("userItemId")
    val userItemId: Long
    //val userName: String
) : Serializable {
    companion object {
        val EMPTY = PostTreeDTO("",0.0,0.0,"",0,0)
        //val EMPTY = PostTreeDTO(0, 0.0, 0.0, "", 0.0, 0, "", "")
    }
}