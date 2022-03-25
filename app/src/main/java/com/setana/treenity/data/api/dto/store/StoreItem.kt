package com.setana.treenity.data.api.dto.store

import com.google.gson.annotations.SerializedName

data class StoreItem(
    @SerializedName("cost") val cost: Int,
    @SerializedName("itemDescription") val itemDescription: String,
    @SerializedName("imagePath") val imagePath: String,
    @SerializedName("itemId") val itemId: Long,
    @SerializedName("itemType") val itemType: String,
    @SerializedName("itemName") val itemName: String
)