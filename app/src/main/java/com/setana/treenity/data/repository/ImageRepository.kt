package com.setana.treenity.data.repository

import com.setana.treenity.data.api.ImageApiService
import javax.inject.Inject

class ImageRepository
@Inject
constructor(private var api: ImageApiService) {

    suspend fun getAllImages() = api.getAllImages()

}