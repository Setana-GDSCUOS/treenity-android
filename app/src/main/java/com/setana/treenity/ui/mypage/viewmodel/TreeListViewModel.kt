package com.setana.treenity.ui.mypage.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeResponse
import com.setana.treenity.data.repository.mypage.TreeListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TreeListViewModel @Inject constructor(val repository: TreeListRepository): ViewModel() {

    var liveData: MutableLiveData<MyTreeResponse> = MutableLiveData()

    fun getLiveDataObserver(): MutableLiveData<MyTreeResponse> {
        return liveData
    }

    fun loadListOfData() {
        repository.makeAPICall(liveData)
    }
}