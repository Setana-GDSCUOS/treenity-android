package com.setana.treenity.data.api.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PostTreeRequestDTO (
    @SerializedName("cloudAnchorId")
    val cloudAnchorId: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("treeName")
    val treeName : String,
    @SerializedName("userItemId")
    val userItemId: Long
    //val userName: String
) : Serializable {
    companion object {
        val EMPTY = PostTreeRequestDTO("",0.0,0.0,"",0)
        //val EMPTY = PostTreeDTO(0, 0.0, 0.0, "", 0.0, 0, "", "")
    }
}