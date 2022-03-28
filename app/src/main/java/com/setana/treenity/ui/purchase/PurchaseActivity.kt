package com.setana.treenity.ui.purchase

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.airbnb.lottie.LottieDrawable
import com.setana.treenity.R
import com.setana.treenity.data.api.dto.store.StoreItem
import com.setana.treenity.databinding.StoreActivityMainBinding
import com.setana.treenity.databinding.StoreConfirmationMainBinding
import com.setana.treenity.ui.loading.LoadingActivity
import com.setana.treenity.ui.store.StoreActivity
import com.setana.treenity.util.AuthUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates

@AndroidEntryPoint
class PurchaseActivity : AppCompatActivity() {

    private lateinit var storeConfirmationMainBinding: StoreConfirmationMainBinding
    private lateinit var storeActivityMainBinding: StoreActivityMainBinding
    private val purchaseViewModel: PurchaseViewModel by viewModels()
    private var localUserId: Long = -1
    private var itemId by Delegates.notNull<Long>()

    // animation
    private lateinit var loadingAnimationFrameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        setUpViewModel()
    }

    private fun setupUI() {
        setupViewBinding()
        setupLoadingAnimationFrameLayout()
        showLoadingAnimation()

        storeConfirmationMainBinding.bringConfirmation.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setIcon(R.drawable.store_tree_icon)
            builder.setTitle("CONFIRMATION")
            builder.setView(layoutInflater.inflate(R.layout.store_confirmation_dialog, null))


            // BUY 버튼 눌렀을 때 이벤트 -> TODO: POST 요청
            builder.setPositiveButton("BUY") { _, _ ->

                val secondIntent = intent
                // The reason I gave 0 as the default value is because itemId starts from 1, so if you say 0, we can notice that there is no item or there is an error.
                itemId = secondIntent.getIntExtra("ChosenItemId", 0).toLong()
                val item = StoreItem(0, "", "", itemId, "", "")

                purchaseViewModel.buyItem(localUserId, item)
                // POST
                purchaseViewModel.buyItemResponseLiveData.observe(this) { response ->
                    response.let {
                        if (it.code() == 409) //  Limit number of purchases
                            Toast.makeText(
                                this@PurchaseActivity,
                                "You can buy bucket upto 3 per day",
                                Toast.LENGTH_LONG
                            ).show()
                        else if (it.code() == 406) { // lack of points
                            Toast.makeText(
                                this@PurchaseActivity,
                                "Not enough points. Shall we go for a walk?",
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (it.code() == 200) {
                            // 구매성공
                            val confirmBuilder = AlertDialog.Builder(this)
                            confirmBuilder.setIcon(R.drawable.store_tree_icon)    // 제목 아이콘
                            confirmBuilder.setTitle("Purchase Succeed!")    // 제목
                            confirmBuilder.setView(layoutInflater.inflate(R.layout.confirm_dialog, null))
                            confirmBuilder.setPositiveButton("OK"){_,_ ->
                            }
                            confirmBuilder.show()
                        } else {
                            Toast.makeText(
                                this@PurchaseActivity,
                                "Internal Error has been occurred. Please try once more",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                // no more activity change after purchase
                //val intent = Intent(this@PurchaseActivity, StoreActivity::class.java)
                // 호출하는 Activity 가 스택에 있을 경우, 해당 Activity 를 최상위로 올리면서, 그 위에 있던 Activity들을 모두 삭제하는 Flag
                //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                //startActivity(intent)
                //finish()

            }
            builder.show()
        }
    }

    private fun setupViewBinding() {
        storeConfirmationMainBinding = StoreConfirmationMainBinding.inflate(layoutInflater)
        storeActivityMainBinding = StoreActivityMainBinding.inflate(layoutInflater)
        setContentView(storeConfirmationMainBinding.root)
    }

    private fun setupLoadingAnimationFrameLayout() {
        loadingAnimationFrameLayout = storeConfirmationMainBinding.frameLottieHolder
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
        val lottieAnimationView = storeConfirmationMainBinding.lottieLoading
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

    override fun onStart() {
        super.onStart()
        checkUser()

        purchaseViewModel.getStoreItems()
    }

    private fun setUpViewModel() {

        purchaseViewModel.storeItemLiveData.observe(this, { items ->

            val water = items[0]

            val secondIntent = intent
            val chosenItemId = secondIntent.getIntExtra("ChosenItemId", 0)

            if (chosenItemId != 1) { // If the choice is seed item
                storeConfirmationMainBinding.apply {
                    itemImage.load((items[chosenItemId - 1].imagePath)) // -1 is because we excluded the water item when we first created it as a seedList and put it in the adapter
                    seedName.text = items[chosenItemId - 1].itemName
                    description.text = items[chosenItemId - 1].itemDescription
                    (items[chosenItemId - 1].cost.toString() + "P").also { cost.text = it }

                    itemId = items[chosenItemId - 1].itemId
                }
            } else { // If the choice is water item
                storeConfirmationMainBinding.apply {
                    itemImage.load(water.imagePath)
                    seedName.text = water.itemName
                    description.text = water.itemDescription
                    (water.cost.toString() + "P").also { cost.text = it }

                    itemId = water.itemId // necessary part for POST request
                }

            }
            hideLoadingAnimation()
        })
    }
}
