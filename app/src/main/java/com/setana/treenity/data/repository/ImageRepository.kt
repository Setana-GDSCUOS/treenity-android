package com.setana.treenity.data.repository

import com.setana.treenity.data.api.ImageService
import javax.inject.Inject

class ImageRepository
@Inject
constructor(private var api: ImageService) {

    suspend fun getAllImages() = api.getAllImages()

}