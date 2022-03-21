package com.setana.treenity.ui.mypage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.setana.treenity.data.api.dto.mypage.tree.Item
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.data.repository.mypage.WalkLogRepository
import com.setana.treenity.databinding.MypageMypageActivityMainBinding
import com.setana.treenity.databinding.MypageMytreeAlertBinding
import com.setana.treenity.di.NetworkModule
import com.setana.treenity.service.StepDetectorService
import com.setana.treenity.ui.mypage.adapter.MyTreeAdapter
import com.setana.treenity.ui.mypage.viewmodel.MyTreeViewModel
import com.setana.treenity.ui.mypage.viewmodel.UserViewModel
import com.setana.treenity.ui.mypage.viewmodel.ViewModelFactory
import com.setana.treenity.ui.mypage.viewmodel.WalkLogViewModel
import dagger.hilt.android.AndroidEntryPoint

///////////////// 마이페이지 /////////////////
@AndroidEntryPoint
class MyPageActivity : AppCompatActivity() {

    // sensor permission
    private val MY_PERMISSION_ACCESS_ALL = 100
    val permission = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)

    // MyPage main
    private lateinit var binding: MypageMypageActivityMainBinding

    // User
    private val userViewModel: UserViewModel by viewModels()

    // My Tree
    private val myTreeViewModel: MyTreeViewModel by viewModels()
    private lateinit var myTreeAdapter: MyTreeAdapter

    // Walk Log
    var barEntries = ArrayList<BarEntry>()
    private lateinit var barDataSet: BarDataSet
    private lateinit var barData: BarData

    private lateinit var walkLogViewModel: WalkLogViewModel
    var walkLogIds = ArrayList<Float>()
    var walks = ArrayList<Float>()
    var dates = ArrayList<String>()
    var idAndDate: MutableMap<Float, String> = mutableMapOf()

    private lateinit var binding3: MypageMytreeAlertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 걷는 것 인식하기 위한 권한 요청
        ActivityCompat.requestPermissions(this, permission, MY_PERMISSION_ACCESS_ALL)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) { // 허용 할 경우, 바로 서비스 on
            Toast.makeText(this, "Activity Sensor is Activated", Toast.LENGTH_SHORT).show()
        }

        //inflate
        binding = MypageMypageActivityMainBinding.inflate(layoutInflater)
        binding3 = MypageMytreeAlertBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // User 데이터 로드
        getMyUserData()

        // WalkLog
        val walkLogRepository = WalkLogRepository(NetworkModule.provideRetrofitInstance())
        walkLogViewModel = ViewModelProvider(
            this,
            ViewModelFactory(walkLogRepository)
        ).get(WalkLogViewModel::class.java)
        setBarChart()

        // My Tree 데이터 로드 & 더보기 아이템 추가
        getMyTreeData()

        // MyTree 에 들어갈 recyclerview 에 어댑터 attach
        setViews()

        // 이벤트 등록 : 마지막 아이템을 누르면 나무 목록 리스트 페이지 전환
        myTreeAdapter.setOnItemClickListener(object : MyTreeAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (position == 3) { // 마지막 아이템
                    val nextIntent = Intent(this@MyPageActivity, TreeListActivity::class.java)
                    startActivity(nextIntent)
                }
            }
        })

        // 이벤트 등록 : 설정 아이콘 누르면 환경 설정 페이지로 전환
        binding.settings.setOnClickListener {
            val nextIntent = Intent(this@MyPageActivity, SettingsActivity::class.java)
            startActivity(nextIntent)
        }

        // 이벤트 등록 : 설정 아이콘 누르면 상점 페이지로 전환
        binding.store.setOnClickListener {
//            val nextIntent = Intent(this@MyPageActivity, StoreActivity::class.java)
//            startActivity(nextIntent)
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
                val intent = Intent(this, StepDetectorService::class.java)
                startService(intent)

                Toast.makeText(this, "Activity Sensor is Activated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        getMyUserData() // point + bucket + totalWalk 갱신 -> 잘 안됨

        // 이름 갱신 -> 환경 설정에서 유저가 적은 이름
        updateName()
    }

    private fun updateName() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sharedPreferences.getString("signature", "no name")
        if (name != "no name")
            binding.username.text = name
    }

//    private fun setPointAndBucket() { // TODO: LiveData 로 갱신이 잘 안됨.. 잘 모르겠다ㅜ
//        userViewModel.userResp.observe(this, { user->
//
//            binding.apply {
//                (user.point.toString() + "P").also { point.text = it }
//                ("X " + user.buckets.toString()).also { bucket.text = it }
//            }
//        })
//    }


    private fun setBarChart() {

        // retrofit2 를 통해 data fetch
        walkLogViewModel.getWalkLog()

        walkLogViewModel.walkLogs.observe(this, {

            //get data from api and put them in barEntries/pieEntries(리스트 크기만큼 for loop 를 돌며 추가)
            val index = it.size - 1

            // walkLogId 와 walk 와 date 를 모두 따로따로 ArrayList 로 저장
            for (i in 0..index) {// x축
                walkLogIds.add(it[i].walkLogId.toFloat())
            }

            for (i in 0..index) { // y축
                walks.add(it[i].walks.toFloat())
            }

            for (i in 0..index) { // bar touch 하면 보여줄 값
                dates.add(it[i].date)
            }

            for (i in 0..index) // (id, date) 구조로 map 에 데이터 추가
                idAndDate[it[i].walkLogId.toFloat()] = it[i].date

            // BarEntry 에 데이터 삽입
            for (i in 0 until walks.size)
                barEntries.add(BarEntry(walkLogIds[i], walks[i]))

            // setting bar chart data
            // initialize bar data set
            barDataSet = BarDataSet(barEntries, "Walk Logs")

            //set colors
            barDataSet.color = ColorTemplate.rgb("#FF408F43") // 바 색상
            barDataSet.valueTextSize = 12f

            barData = BarData(barDataSet)
            barData.barWidth = 0.35f

            //binding 으로 접근하여 barData 전달
            binding.barChart.data = barData


            // prepare chart
            binding.barChart.run {
                data = barData
                setFitBars(true)

                description.isEnabled = false //차트 옆에 별도로 표기되는 description
                setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
                setScaleEnabled(false) // 확대 안되게 설정
                setDrawBarShadow(false) // 그래프의 그림자

                xAxis.run {
                    position = XAxis.XAxisPosition.BOTTOM//X축을 아래에다가 둔다.
                    setDrawAxisLine(true) // 축 그림
                    setDrawGridLines(false) // 격자
                    textSize = 12f // 텍스트 크기
                    valueFormatter = object : ValueFormatter() { // MM/dd 형태로 날짜 모두 표시
                        override fun getFormattedValue(value: Float): String? {

                            return idAndDate[value]?.substring(5, 10)?.replace("-", "/")
                        }
                    }
                }

                axisRight.isEnabled = false // 오른쪽 Y축을 안보이게 설정
                axisLeft.isEnabled = false // 왼쪽 Y축을 안보이게 설정
                animateY(2000) // 애니메이션 추가
                legend.isEnabled = false //차트 범례 설정

                invalidate() // refresh
            }
        })
    }

    private fun getMyUserData() {
        userViewModel.userResp.observe(this, { user ->

            binding.apply {
                username.text = user.username
                point.text = user.point.toString()
                bucket.text = user.buckets.toString()
                totalWalk.text = user.totalWalks.toString()
            }
        })
    }

    private fun getMyTreeData() {
        myTreeViewModel.responseMyTree.observe(this, { listMyTrees ->

            myTreeAdapter.trees = listMyTrees
        })
    }

    private fun setViews() {
        // init adapter
        val item = Item("")
        val myTreeItem = MyTreeItem(0, "", item, 0, 0, "")
        myTreeAdapter = MyTreeAdapter(listOf(myTreeItem))

        // rv 에 myTreeRecyclerviewAdapter 붙이기
        binding.itemRecycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.itemRecycler.adapter = myTreeAdapter
    }
}