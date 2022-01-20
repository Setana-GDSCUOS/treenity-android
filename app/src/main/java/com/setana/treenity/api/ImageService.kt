package com.setana.treenity.api

import com.setana.treenity.model.ImageItem
import com.setana.treenity.utils.Constants.CLIENT_ID
import com.setana.treenity.utils.Constants.END_POINT
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers


interface ImageService {

    @Headers("Accept-Version: v1", "Authorization: Client-ID $CLIENT_ID")
    @GET(END_POINT)
    suspend fun getAllImages(): Response<List<ImageItem>>

}