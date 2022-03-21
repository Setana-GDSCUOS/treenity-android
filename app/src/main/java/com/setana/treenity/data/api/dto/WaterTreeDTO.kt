package com.setana.treenity.data.api.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WaterTreeDTO (
    @SerializedName("treeId")
    val treeId :Long,
    @SerializedName("cloudAnchorId")
    val cloudAnchorId : String
        ):Serializable
{
    companion object{
        val EMPTY = WaterTreeDTO(0,"")
    }
}