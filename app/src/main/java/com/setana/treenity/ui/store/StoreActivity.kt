package com.setana.treenity.ui.store

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.store.StoreItem
import com.setana.treenity.databinding.StoreActivityMainBinding
import com.setana.treenity.databinding.StoreConfirmationMainBinding
import com.setana.treenity.ui.purchase.PurchaseActivity
import com.setana.treenity.ui.store.adapter.StoreAdapter
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class StoreActivity : AppCompatActivity() {
    lateinit var storeAdapter: StoreAdapter
    private lateinit var binding : StoreActivityMainBinding
    private lateinit var binding2 : StoreConfirmationMainBinding

    private val storeViewModel: StoreViewModel by viewModels()
    val userId = PREFS.getLong(USER_ID_KEY, -1)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = StoreActivityMainBinding.inflate(layoutInflater)
        binding2 = StoreConfirmationMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setUpViewModel()

        initRecyclerView()



        // 이벤트 등록 : Seeds의 아이템을 누르면, 해당 아이템의 itemId 을 상세페이지로 전달해줌
        storeAdapter.setOnItemClickListener(object : StoreAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {

                val nextIntent = Intent(this@StoreActivity, PurchaseActivity::class.java)

                // 선택한 아이템의 itemId 를 넘겨준다 (itemId 는 1부터 시작하고, 1번인 물을 제외하면 모두 씨앗이다
                nextIntent.putExtra("ChosenItemId", position + 2)
                startActivity(nextIntent)
            }
        })

        // 이벤트 등록 : Water 의 아이템을 누르면, 물의 itemId 을 상세페이지로 화면 전환
        binding.water.setOnClickListener{

            val intent = Intent(this@StoreActivity, PurchaseActivity::class.java)
            intent.putExtra("ChosenItemId", 1)
            startActivity(intent)
        }

    }

    private fun setUpViewModel() {

        // observe point and # of buckets
        storeViewModel.userLiveData.observe(this, { user ->

            binding.apply {

                (user.point.toString() + "P").also { point.text = it }
                ("X " + user.buckets.toString()).also { bucket.text = it }
            }
        })


        storeViewModel.storeItemLiveData.observe(this, {items ->

            val seedList =  ArrayList<StoreItem>()
            for(index in 1 until items.size) // 처음에 subList로 만들어봤는데 그렇게 하니 메모리 누수 생김
                seedList.add(items[index])

             val water = items[0]

            storeAdapter.itemList = seedList // adapter 에서 binding

            binding.apply {
                (water.cost.toString() + "P").also { bucketPrice.text = it }
                bucketImage.load(water.imagePath)
            }
        })


    }

    override fun onStart() {
        super.onStart()

        storeViewModel.getStoreItems()

        if(userId != -1L)
            storeViewModel.getUserInfo(userId)
    }



    // adapter 부착
    private fun initRecyclerView() {
        // init adapter
        val item = StoreItem(0,"", "", 0L, "", "")
        storeAdapter = StoreAdapter(listOf(item))

        binding.storeRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.storeRecycler.adapter = storeAdapter

    }



}