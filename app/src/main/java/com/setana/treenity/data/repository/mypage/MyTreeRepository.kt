package com.setana.treenity.data.repository.mypage

import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.MyPageApiService
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import javax.inject.Inject

class MyTreeRepository
@Inject
constructor(private val apiService: MyPageApiService) {
    suspend fun getMyTrees() = apiService.getMyTrees(PREFS.getLong(USER_ID_KEY, -1).toString())
}