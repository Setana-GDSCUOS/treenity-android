package com.setana.treenity.data.repository

import com.setana.treenity.data.api.StoreApiHelper
import com.setana.treenity.data.api.dto.store.StoreItem
import com.setana.treenity.data.api.dto.store.StoreResponse
import retrofit2.Response
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val remoteSource: StoreApiHelper
): StoreRepository {

    override suspend fun getStoreItems(): Response<List<StoreItem>> =
        remoteSource.getStoreItems()
    override suspend fun buyTree(userId: Long, itemId: Long): Response<Void> =
        remoteSource.buyTree(userId, itemId)
}
