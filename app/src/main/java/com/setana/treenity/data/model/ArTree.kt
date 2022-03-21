package com.setana.treenity.data.model

import java.io.Serializable

data class ArTree(
    val treeId: Long,
    val latitude: Double,
    val longitude: Double,
    val treeName: String,
    val createdDate: String,
    val level: Int,
    val distance: Double,
    val userId: Long,
    val username: String
) : Serializable
