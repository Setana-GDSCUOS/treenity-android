package com.setana.treenity.data.api.dto

import java.io.Serializable

data class PostTreeDTO (
    val longitude: Double,
    val latitude: Double,
    val cloudAnchorID: String,
    //val userId: Long,
    val userItemId: Long,
    //val userName: String
    // val treeName : String,
) : Serializable {
    companion object {
        val EMPTY = PostTreeDTO(0.0,0.0,"",0)
        //val EMPTY = PostTreeDTO(0, 0.0, 0.0, "", 0.0, 0, "", "")
    }
}