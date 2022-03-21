package com.setana.treenity.data.api.dto.mypage.user

data class User (
    val userId: Long,
    val username: String,
    val point: Int,
    val totalWalks: Int,
    val buckets: Int
)