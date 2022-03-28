package com.setana.treenity.data.repository

import com.setana.treenity.data.api.StoreApiHelper
import com.setana.treenity.data.api.dto.store.StoreItem
import retrofit2.Response
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val remoteSource: StoreApiHelper
) : StoreRepository {

    override suspend fun getStoreItems(): Response<List<StoreItem>> =
        remoteSource.getStoreItems()

    override suspend fun buyTree(userId: String, storeItem: StoreItem): Response<Void> =
        remoteSource.buyTree(userId, storeItem)
}
