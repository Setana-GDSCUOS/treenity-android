package com.setana.treenity.data.api.dto

import java.io.Serializable

data class PutTreeInfoRequestDTO(
    var bookmark:Boolean?,
    var treeDescription: String?,
    var treeName: String?
): Serializable
{
    companion object{
        val EMPTY = PutTreeInfoRequestDTO(true,"","")
    }
}