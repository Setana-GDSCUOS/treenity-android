package com.setana.treenity.data.api.dto

import java.io.Serializable

data class GetUserItemResponseDTO(
    val count:Int,
    val createdDate:String,
    val imagePath: String,
    val itemId: Long,
    val itemName: String,
    val itemType: String,
    val userItemId: Long
) : Serializable{
    companion object {
        val EMPTY = GetUserItemResponseDTO(0,"","",0L,"","",0L)
    }
}
