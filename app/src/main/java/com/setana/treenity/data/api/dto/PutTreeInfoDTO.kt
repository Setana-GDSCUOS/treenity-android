package com.setana.treenity.data.api.dto

import java.io.Serializable

data class PutTreeInfoDTO(
    var bookmark:Boolean?,
    var treeDescription: String?,
    var treeName: String?
): Serializable
{
    companion object{
        val EMPTY = PutTreeInfoDTO(true,"","")
    }
}