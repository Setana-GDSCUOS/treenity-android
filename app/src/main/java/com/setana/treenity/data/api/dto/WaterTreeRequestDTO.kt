package com.setana.treenity.data.api.dto

import java.io.Serializable

data class WaterTreeRequestDTO (
    val cloudAnchorId : String
        ):Serializable
{
    companion object{
        val EMPTY = WaterTreeRequestDTO("")
    }
}