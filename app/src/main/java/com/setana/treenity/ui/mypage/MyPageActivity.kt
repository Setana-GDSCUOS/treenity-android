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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.setana.treenity.R
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.TreenityApplication.Companion.idAndDate
import com.setana.treenity.data.api.dto.mypage.tree.Item
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.databinding.MypageMypageActivityMainBinding
import com.setana.treenity.service.StepDetectorService
import com.setana.treenity.ui.ar.ArActivity
import com.setana.treenity.ui.mytreelist.TreeListActivity
import com.setana.treenity.ui.mypage.adapter.MyTreeAdapter
import com.setana.treenity.ui.settings.SettingsActivity
import com.setana.treenity.ui.store.StoreActivity
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class MyPageActivity : AppCompatActivity() {

    // sensor permission
    private val MY_PERMISSION_ACCESS_ALL = 100
    val permission = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)

    // MyPage main
    private lateinit var binding: MypageMypageActivityMainBinding

    private lateinit var myTreeAdapter: MyTreeAdapter
    private var myTreeSize = 0

    private val myPageViewModel: MyPageViewModel by viewModels()
    val userId = PREFS.getLong(USER_ID_KEY, -1)

    // Walk Log
    var barEntries = ArrayList<BarEntry>()
    private lateinit var barDataSet: BarDataSet
    private lateinit var barData: BarData

    var xValues = ArrayList<Float>()
    var walks = ArrayList<Float>()
    var dates = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkActionPermission()

        //inflate
        binding = DataBindingUtil.setContentView(this, R.layout.mypage_mypage_activity_main)
        binding.lifecycleOwner = this
        binding.mypageviewmodel = myPageViewModel

        setContentView(binding.root)
        setViews()
        observeStep()
        setUpViewModel()

        // 이벤트 등록 : 마지막 아이템을 누르면 나무 목록 리스트 페이지 전환
        myTreeAdapter.setOnItemClickListener(object : MyTreeAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (position == myTreeSize-1) { // 마지막 아이템 누를 시
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
            val nextIntent = Intent(this@MyPageActivity, StoreActivity::class.java)
            startActivity(nextIntent)
        }

        // 이벤트 등록 : "LET'S SAVE OUR EARTH" 버튼 누르면 상점 페이지로 전환
        binding.gotoAr.setOnClickListener {
            val nextIntent = Intent(this@MyPageActivity, ArActivity::class.java)
            startActivity(nextIntent)
            finish()
        }

//        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val mSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
//        val triggerEventListener = object : TriggerEventListener() {
//            override fun onTrigger(event: TriggerEvent?) {
//                // Do work
//                myPageViewModel.increase()
//            }
//        }
//        mSensor?.also { sensor ->
//            sensorManager.requestTriggerSensor(triggerEventListener, sensor)
//        }
    }

    private fun observeStep() {
        myPageViewModel.steps.observe(this, {

            myPageViewModel.countStep()

        })
    }


    private fun checkActionPermission() {
        // 걷는 것 인식하기 위한 권한 요청
        ActivityCompat.requestPermissions(this, permission, MY_PERMISSION_ACCESS_ALL)

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
                val intent = Intent(this, StepDetectorService::class.java)
                startService(intent)

                Toast.makeText(this, "Activity Sensor is Activated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() { // 바뀌는 거 탐지!
        super.onStart()

        if(userId != -1L) {
            myPageViewModel.getUserInfo(userId)
            myPageViewModel.getTreeData(userId)
        }
    }

    private fun setUpViewModel() {

        myPageViewModel.userLiveData.observe(this, { user ->

                binding.apply {
                    username.text = user.username
                    point.text = user.point.toString()
                    bucket.text = user.buckets.toString()
                    dailyWalk.text = user.dailyWalks.toString()
                }
        })

        myPageViewModel.myTreesLiveData.observe(this, {myTrees ->

            // test
            Log.d("TAG", "These are my Trees: $myTrees") // These are my Trees: []

            val lastItem = MyTreeItem(0, "Goto TreeList", Item("https://ifh.cc/g/eA7BXD.jpg"), 0, 0, "")
            var arrayList = ArrayList<MyTreeItem>()
            for(i in myTrees.indices)
                arrayList.add(myTrees[i])
            arrayList.add(lastItem)

            myTreeSize = arrayList.size // 마지막 아이템 눌렀을 때 TreeListActivity 로 가는데 그때 position 을 쉽게 알기 위함
            myTreeAdapter.trees = arrayList
            

//            myTreeAdapter.notifyDataSetChanged()
        })

        // 수정 시작!
        myPageViewModel.getMyWalkLogs(userId) // 데이터 갱신 안하기로 결정
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

            barData = BarData(barDataSet)
            barData.barWidth = 0.25f
            barData.isHighlightEnabled

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
                animateY(3000) // 애니메이션 추가
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
        binding.itemRecycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.itemRecycler.adapter = myTreeAdapter
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
