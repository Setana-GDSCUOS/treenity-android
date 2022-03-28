package com.setana.treenity.ui.mytreelist

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.mypage.tree.Item
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.databinding.MypageTreelistActivityMainBinding
import com.setana.treenity.ui.mytreelist.adapter.TreeListAdapter
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TreeListActivity : AppCompatActivity() {
    lateinit var myTreeListAdapter: TreeListAdapter
    private lateinit var binding : MypageTreelistActivityMainBinding

    private val myTreeListViewModel: TreeListViewModel by viewModels()
    val userId = PREFS.getLong(USER_ID_KEY, -1)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MypageTreelistActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setUpViewModel()

        initRecyclerView()

    }

    private fun setUpViewModel() {

        myTreeListViewModel.myTreeListLiveData.observe(this, {trees ->

            myTreeListAdapter.trees = trees

        })
    }

    override fun onStart() {
        super.onStart()

        if(userId != -1L)
            myTreeListViewModel.getTreeList(userId)
    }


    private fun initRecyclerView() {
        // init adapter
        val item = Item("",0)
        val treeItem = MyTreeItem(0,"", item, 0, 0, "")
        myTreeListAdapter = TreeListAdapter(listOf(treeItem))

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = myTreeListAdapter
    }



}