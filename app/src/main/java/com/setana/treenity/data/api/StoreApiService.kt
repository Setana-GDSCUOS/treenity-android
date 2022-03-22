package com.setana.treenity.data.api

import com.setana.treenity.data.api.dto.store.StoreItem
import com.setana.treenity.data.api.dto.store.StoreResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StoreApiService {

    @GET("items")
    suspend fun getStoreItems() : Response<List<StoreItem>>

    @POST("users/{id}/items")
    suspend fun buyTree(
        @Path("id") id: Long,
        @Body itemId: Long
    ): Response<Void>
}