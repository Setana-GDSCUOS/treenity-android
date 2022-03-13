package com.setana.treenity.data.api.dto

import java.io.Serializable

data class PostTreeDTO (
    val userId: Long,
    val item: String,
    val userName: String,
    val treeName : String,
    val cloudAnchorID: String
) : Serializable {
    companion object {
        val EMPTY = PostTreeDTO(0,"","","","")
        //val EMPTY = PostTreeDTO(0, 0.0, 0.0, "", 0.0, 0, "", "")
    }
}