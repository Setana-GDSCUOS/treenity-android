package com.setana.treenity.data.api

import com.setana.treenity.data.model.ImageItem
import com.setana.treenity.BuildConfig

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers


interface ImageApiService {


    @Headers("Accept-Version: v1", "Authorization: Client-ID ${BuildConfig.CLIENT_ID_TEST}")
    @GET(BuildConfig.END_POINT_TEST)
    suspend fun getAllImages(): Response<List<ImageItem>>

}