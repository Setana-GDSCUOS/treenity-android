package com.setana.treenity.data.repository.mypage

import androidx.lifecycle.MutableLiveData
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.MyPageApiService
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeResponse
import com.setana.treenity.di.NetworkModule
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class TreeListRepository @Inject constructor(val retroInstance: MyPageApiService) {

    // code for recyclerview
    fun makeAPICall(liveTreeList: MutableLiveData<MyTreeResponse>) {
        val retroInstance = NetworkModule.provideRetrofitInstance()
        val call  = retroInstance.getTreeData(PREFS.getLong(USER_ID_KEY, -1).toString())

        call.enqueue(object : Callback<MyTreeResponse> { // 비동기 처리
            override fun onFailure(call: Call<MyTreeResponse>, t: Throwable) {
                liveTreeList.postValue(null)
                
                // TODO: id 가 -1인지 확인하고 로딩 액티비티로 보내거나 에러메시지 띄우기
            }

            override fun onResponse(
                call: Call<MyTreeResponse>,
                response: Response<MyTreeResponse>
            ) {
                liveTreeList.postValue(response.body())
            }
        })
    }
}