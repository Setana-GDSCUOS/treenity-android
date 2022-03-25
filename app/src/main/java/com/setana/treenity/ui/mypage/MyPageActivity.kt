package com.setana.treenity.ui.mypage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.TreenityApplication.Companion.idAndDate
import com.setana.treenity.data.api.dto.mypage.tree.Item
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.databinding.MypageActivityMainBinding
import com.setana.treenity.service.TreenityForegroundService
import com.setana.treenity.ui.ar.ArActivity
import com.setana.treenity.ui.mytreelist.TreeListActivity
import com.setana.treenity.ui.mypage.adapter.MyTreeAdapter
import com.setana.treenity.ui.settings.SettingsActivity
import com.setana.treenity.ui.store.StoreActivity
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.view.View
import android.widget.FrameLayout
import com.airbnb.lottie.LottieDrawable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.setana.treenity.TreenityApplication.Companion.newlyAddedStep
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import com.setana.treenity.util.PreferenceManager.Companion.DAILY_WALK_LOG_KEY
import java.text.DecimalFormat
import java.text.SimpleDateFormat


@AndroidEntryPoint
class MyPageActivity : AppCompatActivity() {

    // sensor permission
    private val PERMISSION_PHYSICAL_ACTIVITY = 100
    val permission = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)

    // animation
    private lateinit var loadingAnimationFrameLayout: FrameLayout

    // MyPage main
    private lateinit var mypageActivityMainBinding: MypageActivityMainBinding
    private var initialStep = 0

    private lateinit var myTreeAdapter: MyTreeAdapter
    private var myTreeSize = 0

    private val myPageViewModel: MyPageViewModel by viewModels()
    val userId = PREFS.getLong(USER_ID_KEY, -1)

    // Post WalkLog
    private val hashMapString = PREFS.getString(DAILY_WALK_LOG_KEY, "")
    val type = object : TypeToken<HashMap<String, String>>() {}.type

    // Walk Log
    var barEntries = ArrayList<BarEntry>()
    private lateinit var barDataSet: BarDataSet
    private lateinit var barData: BarData

    var xValues = ArrayList<Float>()
    var walks = ArrayList<Float>()
    var dates = ArrayList<String>()

    companion object {
        private const val TAG = "MyPageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkActionPermission()

        setupViewBinding()
        setupLoadingAnimationFrameLayout()
        showLoadingAnimation()
        setViews()
        setUpViewModel()

        registerReceiver(br, IntentFilter("1"))
        gotoArActivity()

        // TODO: 이벤트 등록 : 마지막 아이템을 누르면 나무 목록 리스트 페이지 전환
        myTreeAdapter.setOnItemClickListener(object : MyTreeAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (position == myTreeSize-1) { // 마지막 아이템 누를 시
                    val nextIntent = Intent(this@MyPageActivity, TreeListActivity::class.java)
                    startActivity(nextIntent)
                }
            }
        })

        // 이벤트 등록 : 설정 아이콘 누르면 환경 설정 페이지로 전환
        mypageActivityMainBinding.settings.setOnClickListener {
            val nextIntent = Intent(this@MyPageActivity, SettingsActivity::class.java)
            startActivity(nextIntent)
        }

        // 이벤트 등록 : 설정 아이콘 누르면 상점 페이지로 전환
        mypageActivityMainBinding.store.setOnClickListener {
            val nextIntent = Intent(this@MyPageActivity, StoreActivity::class.java)
            startActivity(nextIntent)
        }

        // 이벤트 등록 : "LET'S SAVE OUR EARTH" 버튼 누르면 상점 페이지로 전환
        mypageActivityMainBinding.gotoAr.setOnClickListener {
            val nextIntent = Intent(this@MyPageActivity, ArActivity::class.java)
            startActivity(nextIntent)
            finish()
        }

    }

    private fun gotoArActivity() { // 지금은 test 용 TODO: 로딩액티비티에서 시작해야 할 것 같다
        val nextIntent = Intent(this@MyPageActivity, ArActivity::class.java)
        startActivity(nextIntent)
    }

    private fun setupViewBinding() {
        mypageActivityMainBinding = MypageActivityMainBinding.inflate(layoutInflater)
        setContentView(mypageActivityMainBinding.root)
    }

    private fun setupLoadingAnimationFrameLayout() {
        loadingAnimationFrameLayout = mypageActivityMainBinding.frameLottieHolder
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
        val lottieAnimationView = mypageActivityMainBinding.lottieLoading
        lottieAnimationView.setAnimation("loading.json")
        lottieAnimationView.repeatCount = LottieDrawable.INFINITE
        lottieAnimationView.playAnimation()
    }

    private fun checkActionPermission() {
        // 걷는 것 인식하기 위한 권한 요청
        ActivityCompat.requestPermissions(this, permission, PERMISSION_PHYSICAL_ACTIVITY)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) { // 허용 할 경우, 바로 서비스 on
            Toast.makeText(this, "Activity Sensor is Activated", Toast.LENGTH_SHORT).show()
        }
    }

    // 첫 신체 활동 권한 요청에서 거부를 눌렀을 때
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode > 0) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_DENIED
            ) {
                Toast.makeText(
                    this,
                    "You can address your authorization by clicking setting icon",
                    Toast.LENGTH_SHORT
                ).show()
            } else { // 승인은 했다면
                val intent = Intent(this, TreenityForegroundService::class.java)
                startService(intent)

                Toast.makeText(this, "Activity Sensor is Activated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // POST WalkLog
        val hashMap = Gson().fromJson<HashMap<String, String>>(hashMapString, type)
            ?: hashMapOf(
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).format(Date()) to newlyAddedStep.toString()
            )

        val updateUserWalkLogsRequestDTO = UpdateUserWalkLogsRequestDTO(hashMap)

        if(userId != -1L) {
            myPageViewModel.updateUserWalkLogs(
                userId.toString(),
                updateUserWalkLogsRequestDTO
            )

            newlyAddedStep = 0 // 보내고 난다음에는 초기화!
            PREFS.setString(DAILY_WALK_LOG_KEY, "")

            myPageViewModel.getUserInfo(userId)
            myPageViewModel.getTreeData(userId)

        }

        // test
        Log.d("TAG", "onStart: post dailyWalk in onStart!!!")
    }

    override fun onPause() {
        super.onPause()

        // POST WalkLog
        val hashMap = Gson().fromJson<HashMap<String, String>>(hashMapString, type)
            ?: hashMapOf(
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).format(Date()) to newlyAddedStep.toString()
            )

        val updateUserWalkLogsRequestDTO = UpdateUserWalkLogsRequestDTO(hashMap)

        if(userId != -1L) {
            myPageViewModel.updateUserWalkLogs(
                userId.toString(),
                updateUserWalkLogsRequestDTO
            )

            newlyAddedStep = 0 // 보내고 난다음에는 초기화!
            PREFS.setString(DAILY_WALK_LOG_KEY, "")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(br)
    }


    private fun setUpViewModel() {

        myPageViewModel.userLiveData.observe(this, { user ->

                mypageActivityMainBinding.apply {
                    username.text = user.username
                    point.text = user.point.toString()
                    bucket.text = user.buckets.toString()
                    dailyWalk.text = user.dailyWalks.toString()
                    Log.d(TAG, "setUpViewModel: This is your dailyWalk: ${dailyWalk.text}")
                }

//            initialStep = user.dailyWalks
//            Log.d("TAG", "setUpViewModel: This is the initial step $initialStep") //
        })

        // test
        Log.d("TAG", "setUpViewModel: This is the initial step $initialStep") //

//        binding.dailyWalk.text = initialStep.toString() // step 기존의 것 초기값 설정

        // response 에 대한 코드 작성
        myPageViewModel.updateWalkLogsResponseLiveData.observe(this, { response ->
            response?.let {
                if (it.isSuccessful) {

                    // 전역변수, SharedPreference 초기화
                    newlyAddedStep = 0
                    PREFS.setString(DAILY_WALK_LOG_KEY, "")

                    // dailyWalk 갱신
                    myPageViewModel.getUserInfo(userId)

                } else {
                    Log.d(TAG, "failed to post daily walk!")
                }
            }
        })

        myPageViewModel.myTreesLiveData.observe(this, {myTrees ->

            // test
            Log.d("TAG", "These are my Trees: $myTrees") // These are my Trees: []

            // hosting image is free for 180 days. update it in July 12th
            val lastItem = MyTreeItem(0, "Goto TreeList", Item("https://ifh.cc/g/eA7BXD.jpg"), 0, 0, "")
            var arrayList = ArrayList<MyTreeItem>()
            for(i in myTrees.indices)
                arrayList.add(myTrees[i])
            arrayList.add(lastItem)

            myTreeSize = arrayList.size // 마지막 아이템 눌렀을 때 TreeListActivity 로 가는데 그때 position 을 쉽게 알기 위함
            myTreeAdapter.trees = arrayList
            

            myTreeAdapter.notifyDataSetChanged()
            hideLoadingAnimation()
        })

        myPageViewModel.getMyWalkLogs(userId)
        myPageViewModel.myWalkLogsLiveData.observe(this, {

            // test
            Log.d("tag", ": $it, ${it.size}") // e.g. [WalkLog(date=2022-03-22, walkLogId=12, walks=402), 1]

            val index = it.size - 1

            // walkLogId 와 walk 와 date 를 모두 따로따로 ArrayList 로 저장
            for(i in 0..index) {// x축
                xValues.add(i.toFloat())
            }

            for(i in 0..index) { // y축
                walks.add(it[i].walks.toFloat())
            }

            for(i in 0..index) { // 날짜 저장 -> x 축 대체 예정
                dates.add(it[i].date)
            }

            for(i in 0..index) // (id, date) 구조로 map 에 데이터 추가
                idAndDate[xValues[i]] = it[i].date

            // BarEntry 에 데이터 삽입
            for(i in 0 until walks.size)
                barEntries.add(BarEntry(xValues[i], walks[i]))

            // setting bar chart data
            // initialize bar data set
            barDataSet = BarDataSet(barEntries,"Walk Logs")


            //set colors
            barDataSet.color = ColorTemplate.rgb("#FF408F43") // 바 색상
            barDataSet.valueTextSize = 18f
            barDataSet.valueFormatter = DecimalFormatter()

            barData = BarData(barDataSet)
            barData.barWidth = 0.25f
            barData.isHighlightEnabled

            //binding 으로 접근하여 barData 전달
            mypageActivityMainBinding.barChart.data = barData

            // prepare chart
            mypageActivityMainBinding.barChart.run {
                data = barData

                setFitBars(true)

                description.isEnabled = false //차트 옆에 별도로 표기되는 description
                setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
                setScaleEnabled(false) // 확대 안되게 설정
                setDrawBarShadow(false) // 그래프의 그림자
                setTouchEnabled(false)

                xAxis.run {
                    position = XAxis.XAxisPosition.BOTTOM//X축을 아래에다가 둔다.
                    setDrawAxisLine(true) // 축 그림
                    setDrawGridLines(false) // 격자
                    textSize = 12f // 텍스트 크기
                    granularity = 1F
                    valueFormatter = DateFormatter()  // MM/dd 형태로 날짜 모두 표시
                }

                axisRight.isEnabled = false // 오른쪽 Y축을 안보이게 설정
                axisLeft.isEnabled = false // 왼쪽 Y축을 안보이게 설정
                animateY(2000) // 애니메이션 추가
                legend.isEnabled = false //차트 범례 설정


                invalidate() // refresh
            }
        })
    }

    private fun setViews() {
        // init adapter
        val item = Item("")
        val myTreeItem = MyTreeItem(0, "", item, 0, 0, "")
        myTreeAdapter = MyTreeAdapter(listOf(myTreeItem))

        // recyclerview 에 myTreeRecyclerviewAdapter 붙이기
        mypageActivityMainBinding.itemRecycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        mypageActivityMainBinding.itemRecycler.adapter = myTreeAdapter
    }

    private val br: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {

            
//            val now = System.currentTimeMillis()
//            val date = Date(now)
//            val dateFormat = SimpleDateFormat("dd", Locale.US)
//            val str_date = dateFormat.format(date)
            
            
            val bundle = intent.extras
            if (bundle != null) {
                
//                if(str_date == dd) {
                    mypageActivityMainBinding.dailyWalk.text = (mypageActivityMainBinding.dailyWalk.text.toString()
                        .toInt() + bundle.getInt("detectedStep")).toString()
                    Log.d("TAG", "onReceive: this is my daily step : ${mypageActivityMainBinding.dailyWalk.text}")
//                } else {
//                    binding.dailyWalk.text = "0" // 날짜가 다르다면 0으로 초기화 후, 1씩 걸음 수를 더해줌
//
//                    binding.dailyWalk.text = (binding.dailyWalk.text.toString()
//                        .toInt() + bundle.getInt("detectedStep")).toString()
//                    Log.d("TAG", "onReceive: this is my daily step : ${binding.dailyWalk.text}")
//                }

                newlyAddedStep += 1 // 신호 보낼 때마다 걸었다는 뜻이니까 newlyAddedStep(전역변수 값) + 1
            }
        }
    }
}

class DecimalFormatter : ValueFormatter() {
    private lateinit var decimalFormat : DecimalFormat

    override fun getFormattedValue(value: Float): String {
        decimalFormat = DecimalFormat("#")

        return decimalFormat.format(value)
    }

}

class DateFormatter : ValueFormatter() { // x 축의 float 값을 날짜로 변환해줄 class
    override fun getFormattedValue(value: Float): String {
        if(idAndDate[value] == null){
            return ""
        }
        return idAndDate[value]?.substring(5, 10)?.replace("-", "/") + ""
    }

}




