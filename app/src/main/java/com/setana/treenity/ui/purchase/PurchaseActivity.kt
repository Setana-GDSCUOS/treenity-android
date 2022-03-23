package com.setana.treenity.ui.purchase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.setana.treenity.R
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.store.StoreItem
import com.setana.treenity.databinding.StoreActivityMainBinding
import com.setana.treenity.databinding.StoreConfirmationMainBinding
import com.setana.treenity.ui.store.StoreActivity
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Response
import kotlin.properties.Delegates

@AndroidEntryPoint
class PurchaseActivity : AppCompatActivity() {

    private lateinit var binding : StoreConfirmationMainBinding
    private lateinit var binding2 : StoreActivityMainBinding
    private val purchaseViewModel: PurchaseViewModel by viewModels()

    val userId = PREFS.getLong(USER_ID_KEY, -1)
    private var itemId by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = StoreConfirmationMainBinding.inflate(layoutInflater)
        binding2 = StoreActivityMainBinding.inflate(layoutInflater)


        setContentView(binding.root)

        setUpViewModel()


        binding.bringConfirmation.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setIcon(R.drawable.store_tree_icon)    // 제목 아이콘
            builder.setTitle("CONFIRMATION")    // 제목
            builder.setView(layoutInflater.inflate(R.layout.store_confirmation_dialog, null))

            // BUY 버튼 눌렀을 때 이벤트 -> TODO: POST 요청
            builder.setPositiveButton("BUY") { dialog, which ->

                val secondIntent = intent
                // default 값으로 0을 준 이유는 itemId 는 1부터 시작하기에 0임을 밝히면 아이템이 없거나 에러가 있는 상황임을 알 수 있기 때문
                itemId = secondIntent.getIntExtra("ChosenItemId", 0).toLong()
                val item = StoreItem(0, "", "", itemId, "", "")

                purchaseViewModel.buyItem(userId.toString(), item)
                // POST
                purchaseViewModel.buyItemResponseLiveData.observe(this, {response ->
                    response.let {
                        if (it.code() == 409) //  409면 구매개수제한, 406이면 잔액 부족
                            Toast.makeText(
                                this@PurchaseActivity,
                                "You can buy bucket upto 3 per day",
                                Toast.LENGTH_SHORT
                            ).show()
                        else if (it.code() == 406) {
                            Toast.makeText(
                                this@PurchaseActivity,
                                "Not enough points. Shall we go for a walk?",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@PurchaseActivity,
                                "Successfully purchased",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })

                // 구매 버튼 누르고 나면, 상점페이지로 화면 전환
                val intent = Intent(this@PurchaseActivity, StoreActivity::class.java)
                startActivity(intent)
                finish()
            }

            // Return 버튼 추가
            builder.setNegativeButton("RETURN") { dialog, which ->
                // 아무 처리 안하면 상세페이지로 복귀
            }

            // 뒤로 가기 or 바깥 부분 터치
            builder.setOnCancelListener {
                // 아무 처리 안하면 상세페이지로 복귀
            }

            builder.show()
        }
    }

    override fun onStart() {
        super.onStart()

        purchaseViewModel.getStoreItems()
    }

    private fun setUpViewModel() {

        purchaseViewModel.storeItemLiveData.observe(this, {items ->

            val water = items[0]

            val secondIntent = intent
            // default 값으로 0을 준 이유는 itemId 는 1부터 시작하기에 0임을 밝히면 아이템이 없거나 에러가 있는 상황임을 알 수 있기 때문
            val chosenItemId = secondIntent.getIntExtra("ChosenItemId", 0)

            if(chosenItemId != 1) { // 고른게 물이 아니라면
                binding.apply {
                    itemImage.load((items[chosenItemId-1].imagePath)) // 처음에 seedList 로 만들어 어댑터에 넣을 때 물을 뺐기 때문
                    seedName.text = items[chosenItemId-1].itemName
                    description.text = items[chosenItemId-1].description
                    (items[chosenItemId-1].cost.toString() + "P").also { cost.text = it }

                    itemId = items[chosenItemId-1].itemId
                }
            }
            else { // 물을 선택했다면
                    binding.apply {
                        itemImage.load(water.imagePath)
                        seedName.text = water.itemName
                        description.text = water.description
                        (water.cost.toString() + "P").also { cost.text = it }

                        itemId = water.itemId // 후에 POST 요청을 위해 필요한 부분
                    }
                
            }
        })
    }
}