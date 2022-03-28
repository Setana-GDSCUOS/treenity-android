package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.store.StoreItem
import retrofit2.Response

interface StoreApiHelper {

    suspend fun getStoreItems(): Response<List<StoreItem>>
    suspend fun buyTree(userId: String, storeItem: StoreItem): Response<Void>
}