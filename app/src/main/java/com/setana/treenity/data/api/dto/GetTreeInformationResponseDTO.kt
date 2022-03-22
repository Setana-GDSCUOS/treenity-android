package com.setana.treenity.data.api.dto

import com.google.gson.annotations.SerializedName
import com.setana.treenity.data.model.SeedItem
import com.setana.treenity.data.model.User
import java.io.Serializable

data class GetTreeInformationResponseDTO(
    @SerializedName("bookmark")
    val bookmark : Boolean,
    @SerializedName("bucket")
    val bucket: Int,
    @SerializedName("cloudAnchorId")
    val cloudAnchorId: String,
    @SerializedName("createdDate")
    val createdDate: String,
    @SerializedName("treeDescription")
    val treeDescription: String,
    @SerializedName("imagePath")
    val imagePath: String,
    @SerializedName("item")
    val item: SeedItem,
    @SerializedName("latitude")
    val latitude : Double,
    @SerializedName("level")
    val level : Int,
    @SerializedName("longitude")
    val longitude : Double,
    @SerializedName("treeId")
    val treeId : Long,
    @SerializedName("user")
    val user: User,
    @SerializedName("treeName")
    val treeName: String
) :Serializable {
    companion object{
        val EMPTY = GetTreeInformationResponseDTO(true,0,"","","","", SeedItem.EMPTY,0.0,0,0.0,0,User.EMPTY,"")
    }
}