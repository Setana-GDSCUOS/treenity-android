package com.setana.treenity.ui.store

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.airbnb.lottie.LottieDrawable
import com.setana.treenity.data.api.dto.store.StoreItem
import com.setana.treenity.databinding.StoreActivityMainBinding
import com.setana.treenity.databinding.StoreConfirmationMainBinding
import com.setana.treenity.ui.loading.LoadingActivity
import com.setana.treenity.ui.purchase.PurchaseActivity
import com.setana.treenity.ui.store.adapter.StoreAdapter
import com.setana.treenity.util.AuthUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class StoreActivity : AppCompatActivity() {
    lateinit var storeAdapter: StoreAdapter
    private lateinit var storeActivityMainBinding: StoreActivityMainBinding
    private lateinit var storeConfirmationMainBinding: StoreConfirmationMainBinding

    // animation
    private lateinit var loadingAnimationFrameLayout: FrameLayout

    private val storeViewModel: StoreViewModel by viewModels()
    private var localUserId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        setUpViewModel()
    }

    override fun onStart() {
        super.onStart()
        checkUser()

        storeViewModel.getStoreItems()
        storeViewModel.getUserInfo(localUserId)
    }

    private fun setupUI() {
        setupViewBinding()
        setupLoadingAnimationFrameLayout()
        showLoadingAnimation()
        initRecyclerView()

        // If user click on an seed item, the itemId of the item is delivered to the detail page
        storeAdapter.setOnItemClickListener(object : StoreAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {

                val nextIntent = Intent(this@StoreActivity, PurchaseActivity::class.java)

                //
                nextIntent.putExtra("ChosenItemId", position + 2)
                startActivity(nextIntent)
            }
        })

        // Passes the itemId of the selected item (itemId starts at 1, and all of them are seeds except water, which is number 1)
        storeActivityMainBinding.water.setOnClickListener {

            val intent = Intent(this@StoreActivity, PurchaseActivity::class.java)
            intent.putExtra("ChosenItemId", 1)
            startActivity(intent)
        }
    }

    private fun setupViewBinding() {
        storeActivityMainBinding = StoreActivityMainBinding.inflate(layoutInflater)
        storeConfirmationMainBinding = StoreConfirmationMainBinding.inflate(layoutInflater)
        setContentView(storeActivityMainBinding.root)
    }

    private fun setupLoadingAnimationFrameLayout() {
        loadingAnimationFrameLayout = storeActivityMainBinding.frameLottieHolder
        loadingAnimationFrameLayout.bringToFront()
    }

    private fun showLoadingAnimation() {
        loadingAnimationFrameLayout.visibility = View.VISIBLE
        playLottieAnimation()
    }

    private fun hideLoadingAnimation() {
        loadingAnimationFrameLayout.visibility = View.INVISIBLE
    }

    private fun playLottieAnimation() {
        val lottieAnimationView = storeActivityMainBinding.lottieLoading
        lottieAnimationView.setAnimation("loading.json")
        lottieAnimationView.repeatCount = LottieDrawable.INFINITE
        lottieAnimationView.playAnimation()
    }

    private fun checkUser() {
        if (AuthUtils.userId <= 0) {
            Toast.makeText(this, "Invalid user credentials!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoadingActivity::class.java)
            startService(intent)
            finish()
        } else {
            localUserId = AuthUtils.userId
        }
    }

    private fun setUpViewModel() {

        // observe point and # of buckets
        storeViewModel.userLiveData.observe(this, { user ->

            storeActivityMainBinding.apply {

                (user.point.toString() + "P").also { point.text = it }
                ("X " + user.buckets.toString()).also { bucket.text = it }
            }

        })

        storeViewModel.storeItemLiveData.observe(this, { items ->

            val seedList: MutableList<StoreItem> = mutableListOf()
            for (index in 1 until items.size)
                seedList.add(items[index])

            val water = items[0]

            storeAdapter.itemList = seedList

            storeActivityMainBinding.apply { // Water item must be bound separately because it is not in horizontal recyclerview
                (water.cost.toString() + "P").also { bucketPrice.text = it }
                bucketImage.load(water.imagePath)
            }
            storeActivityMainBinding.water.visibility = View.VISIBLE
            hideLoadingAnimation()
        })
    }

    private fun initRecyclerView() {
        // init adapter
        val item = StoreItem(0, "", "", 0L, "", "")
        storeAdapter = StoreAdapter(listOf(item))

        storeActivityMainBinding.storeRecycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        storeActivityMainBinding.storeRecycler.adapter = storeAdapter

    }
}