package com.setana.treenity.ui.mypage

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.airbnb.lottie.LottieDrawable
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.setana.treenity.R
import com.setana.treenity.TreenityApplication
import com.setana.treenity.TreenityApplication.Companion.DAILY_WALK_LOG
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import com.setana.treenity.data.api.dto.mypage.tree.Item
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.databinding.MypageActivityMainBinding
import com.setana.treenity.service.TreenityForegroundService
import com.setana.treenity.ui.ar.ArActivity
import com.setana.treenity.ui.loading.LoadingActivity
import com.setana.treenity.ui.mypage.adapter.MyTreeAdapter
import com.setana.treenity.ui.mytreelist.TreeListActivity
import com.setana.treenity.ui.settings.SettingsActivity
import com.setana.treenity.ui.store.StoreActivity
import com.setana.treenity.util.AuthUtils
import com.setana.treenity.util.PreferenceManager.Companion.DAILY_WALK_LOG_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class MyPageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // today steps
    private var todaySteps = 0

    // sensor permission
    private val PERMISSION_PHYSICAL_ACTIVITY = 100
    val permission = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)

    // animation
    private lateinit var loadingAnimationFrameLayout: FrameLayout

    // MyPage main
    private lateinit var mypageActivityMainBinding: MypageActivityMainBinding

    private lateinit var myTreeAdapter: MyTreeAdapter
    private var myTreeSize = 0

    private val myPageViewModel: MyPageViewModel by viewModels()
    private var localUserId: Long = -1

    // Walk Log


    private val br = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            intent.extras?.let { it ->
                todaySteps += it.getInt("NEWLY_DETECTED_STEP")
                mypageActivityMainBinding.dailyWalk.text = todaySteps.toString()
                // TODO: 오늘 얻은 포인트도 알려줄 것!
                ((mypageActivityMainBinding.dailyWalk.text.toString().toFloat()/100F).toInt().toString()
                + "P").also { mypageActivityMainBinding.dailyUpdatedPoint.text = it }
            }
        }
    }

    companion object {
        private const val TAG = "MyPageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkActionPermission()
        setupUI()
        setUpViewModel()
    }

    override fun onStart() {
        super.onStart()
        checkUser()

        registerReceiver(br, IntentFilter("GOTO_MYPAGE_AND_ADD_DETECTED_STEP"))

        // POST WalkLog
        val hashMapString = PREFS.getString(DAILY_WALK_LOG_KEY, "")
        val type = object : TypeToken<HashMap<String, String>>() {}.type
        val hashMap = Gson().fromJson<HashMap<String, String>>(hashMapString, type)
            ?: hashMapOf(
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).format(Date()) to "0"
            )

        val updateUserWalkLogsRequestDTO = UpdateUserWalkLogsRequestDTO(hashMap)
        myPageViewModel.updateUserWalkLogs(
            localUserId.toString(),
            updateUserWalkLogsRequestDTO
        )
        myPageViewModel.getTreeData(localUserId)

        // test
        Log.d("TAG", "onStart: post dailyWalk in onStart!!!")
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(br)
    }

    /**
     * 현재 User 정보에 대한 간단한 검증을 진행하는 메소드
     * - 액티비티 상단에 userId 로 사용할 private var localUserId: Long = -1 정의
     * - 반드시 onStart() 내에서 제일 먼저 호출
     */
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

    private fun setupUI() {
        setupViewBinding()
        setMyPageProfileFromGoogleProfile()
        setupLoadingAnimationFrameLayout()
        showLoadingAnimation()
        initRecyclerView()

        myTreeAdapter.setOnItemClickListener(object : MyTreeAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (position == myTreeSize - 1) { // 마지막 아이템 누를 시
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

        mypageActivityMainBinding.carbonEmission.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setMyPageProfileFromGoogleProfile() {
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            mypageActivityMainBinding.userprofile.load(currentUser.photoUrl) {
                transformations(CircleCropTransformation())
            }
        }
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

    private fun initRecyclerView() {
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

    private fun setUpViewModel() {

        myPageViewModel.userLiveData.observe(this) { user ->
            mypageActivityMainBinding.apply {
                username.text = user.username
                point.text = user.point.toString()
                bucket.text = user.buckets.toString()
                dailyWalk.text = user.dailyWalks.toString()
                carbonEmission.text = user.totalWalks.toString()

                // showing daily total points user have earned
                ((mypageActivityMainBinding.dailyWalk.text.toString().toFloat()/100F).toInt().toString()
                + "P").also { mypageActivityMainBinding.dailyUpdatedPoint.text = it }

                // TODO: totalWalks 받아서 줄인 탄소배출량 알려주기
                // using CO2 emissions per km of vehicles in Europe (6/16) [2018] and assume the step width is 50 cm
                val reducedCarbonEmission = (mypageActivityMainBinding.carbonEmission.text.toString().toFloat()*0.0005F)/32.836F
                (reducedCarbonEmission.toString() + "g").also { mypageActivityMainBinding.carbonEmission.text = it }

                // test
                Log.d(TAG, "setUpViewModel: This is your dailyWalk: ${dailyWalk.text}")
            }
            hideLoadingAnimation()
        }

        myPageViewModel.updateWalkLogsResponseLiveData.observe(this) { response ->
            response?.let {
                if (it.isSuccessful) {
                    // Internal WalkLogs 초기화
                    PREFS.setString(DAILY_WALK_LOG_KEY, "")
                    DAILY_WALK_LOG.clear()

                    // dailyWalk 갱신
                    myPageViewModel.getMyWalkLogs(localUserId)  // Chart
                    myPageViewModel.getUserInfo(localUserId)    // user info
                } else {
                    Log.d(TAG, "failed to post daily walk!")
                }
            }
        }

        myPageViewModel.myTreesLiveData.observe(this) { myTrees ->
            // test
            Log.d("TAG", "These are my Trees: $myTrees") // These are my Trees: []

            // hosting image is free for 180 days. update it in July 12th
            val lastItem =
                MyTreeItem(0, "Goto TreeList", Item("https://ifh.cc/g/eA7BXD.jpg"), 0, 0, "")
            val arrayList = ArrayList<MyTreeItem>()
            for (i in myTrees.indices)
                arrayList.add(myTrees[i])
            arrayList.add(lastItem)

            myTreeSize =
                arrayList.size // 마지막 아이템 눌렀을 때 TreeListActivity 로 가는데 그때 position 을 쉽게 알기 위함
            myTreeAdapter.trees = arrayList

            myTreeAdapter.notifyDataSetChanged()

        }

        myPageViewModel.myWalkLogsLiveData.observe(this) { dailyWalkLogs ->
            todaySteps = dailyWalkLogs.find {
                it.date == SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).format(Date())
            }?.walks ?: 0

            val dailyWalkLogsMap: HashMap<Float, String> = hashMapOf()

            // init as local variable so that value of graph doesn't overlap with previous data
            val barEntries : MutableList<BarEntry> = mutableListOf()
            lateinit var barData: BarData

            val xValues : MutableList<Float> = mutableListOf()
            val walks : MutableList<Float> = mutableListOf()
            val dates : MutableList<String> = mutableListOf()

            // test
            Log.d(
                "tag",
                ": $dailyWalkLogs, ${dailyWalkLogs.size}"
            ) // e.g. [WalkLog(date=2022-03-22, walkLogId=12, walks=402), 1]

            val index = dailyWalkLogs.size - 1

            // walkLogId 와 walk 와 date 를 모두 따로따로 ArrayList 로 저장
            for (i in 0..index) {// x축
                xValues.add(i.toFloat())
            }

            for (i in 0..index) { // y축
                walks.add(dailyWalkLogs[i].walks.toFloat())
            }

            for (i in 0..index) { // 날짜 저장 -> x 축 대체 예정
                dates.add(dailyWalkLogs[i].date)
            }

            for (i in 0..index) // (id, date) 구조로 map 에 데이터 추가
                dailyWalkLogsMap[xValues[i]] = dailyWalkLogs[i].date

            // BarEntry 에 데이터 삽입
            for (i in 0 until walks.size)
                barEntries.add(BarEntry(xValues[i], walks[i]))

            // setting bar chart data
            // initialize bar data set
            val barDataSet = BarDataSet(barEntries, "Walk Logs")

            //set colors
            barDataSet.color = getColor(R.color.colorPrimary)
            barDataSet.valueTextColor = getColor(R.color.colorPrimary)
            barDataSet.valueTextSize = 18f
            barDataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = DecimalFormat("#").format(value)
            }

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
                setPinchZoom(false) // 핀치줌(두 손가락으로 줌인 줌 아웃하는것) 설정
                setScaleEnabled(false) // 확대 안되게 설정
                setDrawBarShadow(false) // 그래프의 그림자
                setTouchEnabled(false)


                xAxis.run {
                    position = XAxis.XAxisPosition.BOTTOM //X축을 아래에다가 둔다.
                    setDrawAxisLine(true) // 축 그림
                    setDrawGridLines(false) // 격자
                    textColor = getColor(R.color.colorSecondary)
                    textSize = 12f // 텍스트 크기
                    granularity = 1.0F
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            if (dailyWalkLogsMap[value] == null) {
                                return ""
                            }
                            return dailyWalkLogsMap[value]?.substring(5, 10)?.replace("-", "/") + ""
                        }
                    }  // MM/dd 형태로 날짜 모두 표시
                }

                axisRight.isEnabled = false // 오른쪽 Y축을 안보이게 설정
                axisLeft.isEnabled = false // 왼쪽 Y축을 안보이게 설정
                animateY(2000) // 애니메이션 추가
                legend.isEnabled = false //차트 범례 설정

                invalidate() // refresh

            }
        }
    }
}




