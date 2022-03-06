package com.setana.treenity.data.model

import java.io.Serializable
import java.util.*

data class Tree(
    val treeId: Long,
    val treeName: String,
    val createdDate: Date,
    val level: Int,
    val lat: Double,
    val lng: Double,
    val ownerId: Long,
    val ownerName: String
) : Serializable {
    companion object {
        val EMPTY = Tree(0, "", Date(), 0, 0.0, 0.0, 0, "")
    }
}
