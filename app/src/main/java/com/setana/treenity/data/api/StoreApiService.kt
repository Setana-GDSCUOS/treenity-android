package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.store.StoreItem
import retrofit2.Response
import retrofit2.http.*

interface StoreApiService {

    @GET("items")
    suspend fun getStoreItems(): Response<List<StoreItem>>

    @POST("users/{userId}/items")
    suspend fun buyTree(
        @Path(value = "userId", encoded = true) userId: String,
        @Body storeItem: StoreItem
    ): Response<Void>
}