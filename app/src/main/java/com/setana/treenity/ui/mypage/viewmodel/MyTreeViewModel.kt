package com.setana.treenity.ui.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.mypage.tree.Item
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.data.repository.mypage.MyTreeRepository
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyTreeViewModel
@Inject
constructor(private val repository: MyTreeRepository) : ViewModel() {

    private val _response = MutableLiveData<List<MyTreeItem>>()
    val responseMyTree: LiveData<List<MyTreeItem>>
        get() = _response

    init {
        getAllTrees()
    }

    private fun getAllTrees() = viewModelScope.launch {

        repository.getMyTrees().let {response ->
            // treeId는 1부터 시작하기에 겹치지 않기 위해 0번을 주었고, imagePath 의 경우, pinterest 에서 다운다아 ifh 이미지 호스팅을 통해 이미지 주소를 만듦
            val lastItem = MyTreeItem(0, "Goto TreeList", Item("https://ifh.cc/g/eA7BXD.jpg"), 0, 0, "")

            if (response.isSuccessful){ // 아이템은 3개만 보여줄 것(3+1)
                _response.postValue(listOf(response.body()!![0],
                    response.body()!![1],
                    response.body()!![2],
                    lastItem // 더보기 아이템
                )
                )
            } else{
                Log.d("tag", "getAllTrees Error: ${response.code()}")
            }
        }
    }
}