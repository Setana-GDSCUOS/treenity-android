package com.setana.treenity.ui.mytreelist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.databinding.MypageTreelistActivityMainBinding
import com.setana.treenity.ui.mypage.MyPageActivity
import com.setana.treenity.ui.mytreelist.adapter.TreeListAdapter
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TreeListActivity : AppCompatActivity() {
    lateinit var treeListAdapter: TreeListAdapter
    private lateinit var binding : MypageTreelistActivityMainBinding
    private val treeListViewModel: TreeListViewModel by viewModels()
    val userId = PREFS.getLong(USER_ID_KEY, -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MypageTreelistActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initRecyclerView()
        setUpViewModel()

        // 유저가 보유한 나무 목록을 보여주는 페이지의 좌측 상단 뒤로가기 아이콘을 누르면 마이페이지 창으로 전환됨
        binding.gotoMyPage.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }

    }

    // adapter 부착
    private fun initRecyclerView() {
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        treeListAdapter = TreeListAdapter()
        binding.recyclerview.adapter = treeListAdapter
    }

    private fun setUpViewModel() {

        treeListViewModel.myTreeListLiveData.observe(this, { response ->

            response?.let {
                if(it.isSuccessful) {

                    treeListViewModel.getTreeList(userId)

                    treeListAdapter.setDataList(it.body())
                    treeListAdapter.notifyDataSetChanged()

                } else {
                    Log.d("SetupViewModel", response.message())
                }
            }
        })

    }

}