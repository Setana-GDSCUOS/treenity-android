package com.setana.treenity.data.model

import java.io.Serializable

// 재욱님이 만든거랑 통합할지도 중복되던데요...?
data class User (
    val userId: Long,
    val username: String,
    val point: Int,
    val totalWalks: Int,
    val buckets: Int
):Serializable{
    companion object{
        val EMPTY = User(0,"",0,0,0)
    }
}