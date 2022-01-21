package com.setana.treenity.repository

import com.setana.treenity.api.ImageApiService
import javax.inject.Inject

class ImageRepository
@Inject
constructor(private var api: ImageApiService) {

    suspend fun getAllImages() = api.getAllImages()

}