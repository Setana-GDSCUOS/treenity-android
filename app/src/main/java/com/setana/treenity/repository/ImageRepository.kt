package com.setana.treenity.repository

import com.setana.treenity.api.ImageService
import javax.inject.Inject

class ImageRepository
@Inject
constructor(private var api: ImageService) {

    suspend fun getAllImages() = api.getAllImages()

}