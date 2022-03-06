package com.setana.treenity.data.api

import javax.inject.Inject

class TreeApiHelperImpl @Inject constructor(
    private val treeApiService: TreeApiService
): TreeApiHelper {
    override suspend fun getAroundTrees(lat: Double, lng: Double) = treeApiService.getAroundTrees(lat, lng)
}