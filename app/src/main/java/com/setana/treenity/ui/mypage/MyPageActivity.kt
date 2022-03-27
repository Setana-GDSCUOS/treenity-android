package com.setana.treenity.ui.mypage

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.setana.treenity.R
import com.setana.treenity.TreenityApplication.Companion.DAILY_WALK_LOG
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import com.setana.treenity.data.api.dto.mypage.tree.Item
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.databinding.MypageActivityMainBinding
import com.setana.treenity.service.TreenityForegroundService
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
    private val permission = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)

    // animation
    private lateinit var loadingAnimationFrameLayout: FrameLayout

    // MyPage main
    private lateinit var mypageActivityMainBinding: MypageActivityMainBinding
    private lateinit var myTreeAdapter: MyTreeAdapter
    private var myTreeSize = 0
    private val myPageViewModel: MyPageViewModel by viewModels()
    private var localUserId: Long = -1

    private val br = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            intent.extras?.let { it ->
                todaySteps += it.getInt("NEWLY_DETECTED_STEP")
                mypageActivityMainBinding.dailyWalk.text = todaySteps.toString()

                // updating daily point user have earned
                ((mypageActivityMainBinding.dailyWalk.text.toString().toFloat() / 100F).toInt()
                    .toString()
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
                if (position == myTreeSize - 1) { // position of last item which is a card view item for going to my tree list activity
                    val nextIntent = Intent(this@MyPageActivity, TreeListActivity::class.java)
                    startActivity(nextIntent)
                }
            }
        })

        // register event: going to SettingsActivity.kt
        mypageActivityMainBinding.settings.setOnClickListener {
            val nextIntent = Intent(this@MyPageActivity, SettingsActivity::class.java)
            startActivity(nextIntent)
        }

        // register event: going to StoreActivity.kt
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

        // attach adapter to recyclerview
        mypageActivityMainBinding.itemRecycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        mypageActivityMainBinding.itemRecycler.adapter = myTreeAdapter
    }

    private fun checkActionPermission() {
        // ask permission to count step
        ActivityCompat.requestPermissions(this, permission, PERMISSION_PHYSICAL_ACTIVITY)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) { // if permitted, set a toast message directly
            Toast.makeText(this, "Activity Sensor is Activated", Toast.LENGTH_SHORT).show()
        }
    }

    // When user hit Deny on user's first physical activity permission request
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
            } else { // if the user permitted
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
                ((mypageActivityMainBinding.dailyWalk.text.toString().toFloat() / 100F).toInt()
                    .toString()
                        + "P").also { mypageActivityMainBinding.dailyUpdatedPoint.text = it }

                // using CO2 emissions per km of vehicles in Europe (6/16) [2018] and assume the step width is 50 cm
                val reducedCarbonEmission =
                    (mypageActivityMainBinding.carbonEmission.text.toString()
                        .toFloat() * 0.0005F) / 32.836F
                (reducedCarbonEmission.toString() + "g").also {
                    mypageActivityMainBinding.carbonEmission.text = it
                }

                // test
                Log.d(TAG, "setUpViewModel: This is your dailyWalk: ${dailyWalk.text}")
            }
            hideLoadingAnimation()
        }

        myPageViewModel.updateWalkLogsResponseLiveData.observe(this) { response ->
            response?.let {
                if (it.isSuccessful) {
                    // set internal WalkLogs null
                    PREFS.setString(DAILY_WALK_LOG_KEY, "")
                    DAILY_WALK_LOG.clear()

                    myPageViewModel.getMyWalkLogs(localUserId)  // for the MPAndroidChart<My WalkLog>
                    myPageViewModel.getUserInfo(localUserId)    // for user info
                } else {
                    Log.d(TAG, "failed to post daily walk!")
                }
            }
        }

        myPageViewModel.myTreesLiveData.observe(this) { myTrees ->
            // test
            Log.d("TAG", "These are my Trees: $myTrees")

            // hosting image is free for 180 days. update it in July 12th
            val lastItem =
                MyTreeItem(0, "Goto TreeList", Item("https://ifh.cc/g/eA7BXD.jpg"), 0, 0, "")
            val arrayList = ArrayList<MyTreeItem>()
            for (i in myTrees.indices)
                arrayList.add(myTrees[i])
            arrayList.add(lastItem)

            myTreeSize =
                arrayList.size // It is now possible to know which item to press to go to TreeListActivity
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
            val barEntries: MutableList<BarEntry> = mutableListOf()
            lateinit var barData: BarData

            val xValues: MutableList<Float> = mutableListOf()
            val walks: MutableList<Float> = mutableListOf()
            val dates: MutableList<String> = mutableListOf()

            // test
            Log.d(
                "tag",
                ": $dailyWalkLogs, ${dailyWalkLogs.size}"
            ) // e.g. [WalkLog(date=2022-03-22, walkLogId=12, walks=402), 1]

            val index = dailyWalkLogs.size - 1

            // Store walkLogId and walk and date separately as MutableList
            for (i in 0..index) { // xAxis
                xValues.add(i.toFloat())
            }

            for (i in 0..index) { // yAxis
                walks.add(dailyWalkLogs[i].walks.toFloat())
            }

            for (i in 0..index) { // save date -> x axis to be replaced
                dates.add(dailyWalkLogs[i].date)
            }

            for (i in 0..index) // Add data to hashMap as (id, date) structure
                dailyWalkLogsMap[xValues[i]] = dailyWalkLogs[i].date

            //Insert data into BarEntry
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
                override fun getFormattedValue(value: Float): String =
                    DecimalFormat("#").format(value)
            }

            barData = BarData(barDataSet)
            barData.barWidth = 0.25f
            barData.isHighlightEnabled

            // Accessing by binding and passing barData
            mypageActivityMainBinding.barChart.data = barData

            // prepare chart
            mypageActivityMainBinding.barChart.run {
                data = barData
                setFitBars(true)

                description.isEnabled =
                    false // A description displayed separately next to the chart
                setPinchZoom(false) // Pinch zoom (two-finger zoom in and zoom out) settings
                setScaleEnabled(false) // set not to zoom
                setDrawBarShadow(false) // shadow on the graph
                setTouchEnabled(false) // disable touch
                moveViewToX((xValues.size - 1).toFloat()) // move to last bar

                xAxis.run {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawAxisLine(true)
                    setDrawGridLines(false)
                    textColor = getColor(R.color.colorSecondary)
                    textSize = 12f
                    granularity = 1.0F
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            if (dailyWalkLogsMap[value] == null) {
                                return ""
                            }
                            return dailyWalkLogsMap[value]?.substring(5, 10)?.replace("-", "/") + ""
                        }
                    }  // Display all dates in MM/dd format
                }

                axisRight.isEnabled = false
                axisLeft.isEnabled = false
                animateY(2000)
                legend.isEnabled = false

                invalidate() // refresh
            }
        }
    }
}




