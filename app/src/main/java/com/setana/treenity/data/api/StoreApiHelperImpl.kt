package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.store.StoreItem
import com.setana.treenity.data.api.dto.store.StoreResponse
import retrofit2.Response
import javax.inject.Inject

class StoreApiHelperImpl @Inject constructor(
    private val storeApiService: StoreApiService
): StoreApiHelper {

    override suspend fun getStoreItems(): Response<List<StoreItem>> = storeApiService.getStoreItems()
    override suspend fun buyTree(userId: String, storeItem: StoreItem): Response<Void> = storeApiService.buyTree(userId, storeItem)
}