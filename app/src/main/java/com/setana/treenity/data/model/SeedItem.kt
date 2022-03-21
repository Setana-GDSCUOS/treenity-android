package com.setana.treenity.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SeedItem (
    @SerializedName("cost")
    val cost: Int,
    @SerializedName("description")
    val description: String,
    @SerializedName("imagePath")
    val imagePath: String,
    @SerializedName("itemId")
    val itemId : Long,
    @SerializedName("itemType")
    val itemType: String,
    @SerializedName("name")
    val name : String
    ): Serializable{
    companion object {
        val EMPTY = SeedItem(0,"","",0,"","")
    }
}