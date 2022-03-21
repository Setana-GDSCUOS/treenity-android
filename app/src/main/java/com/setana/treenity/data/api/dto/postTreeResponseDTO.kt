package com.setana.treenity.data.api.dto

import java.io.Serializable

data class PostTreeResponseDTO(
    // 형식에 대한 문제? - Post 에 대한 Response 가 어떻게 올까
    val treeId : Long
):
Serializable {
    companion object {
        val EMPTY = PostTreeResponseDTO(0)
    }
}