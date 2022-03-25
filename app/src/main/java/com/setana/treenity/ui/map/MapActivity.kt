package com.setana.treenity.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieDrawable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.setana.treenity.R
import com.setana.treenity.data.api.dto.GetAroundTreeResponseDTO
import com.setana.treenity.databinding.ActivityMapBinding
import com.setana.treenity.ui.loading.LoadingActivity
import com.setana.treenity.util.AuthUtils
import com.setana.treenity.util.EventObserver
import com.setana.treenity.util.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.setana.treenity.util.PermissionUtils.isPermissionGranted
import com.setana.treenity.util.PermissionUtils.requestPermission
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private lateinit var activityMapBinding: ActivityMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var loadingAnimationFrameLayout: FrameLayout
    private var lastKnownLocation: Location? = null
    private var permissionDenied = false
    private var localUserId: Long = -1
    private var isFABOpen = false
    private val mapViewModel: MapViewModel by viewModels()
    private val cancellationTokenSource = CancellationTokenSource()
    private val markerHashMap = hashMapOf<Long, Marker>()
    private val bookmarkHashMap = hashMapOf<Long, Boolean>()

    companion object {
        private const val TAG = "MapActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        setupViewModel()
    }

    override fun onStart() {
        super.onStart()
        checkUser()
    }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
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
        setupLoadingAnimationFrameLayout()
        showLoadingAnimation()
        setupMap()
        setupBottomSheet()
        setupFloatingActionButton()
    }

    private fun showLoadingAnimation() {
        loadingAnimationFrameLayout.visibility = View.VISIBLE
        playLottieAnimation()
    }

    private fun hideLoadingAnimation() {
        loadingAnimationFrameLayout.visibility = View.INVISIBLE
//        val lottieAnimationView = activityMapBinding.lottieLoading
//        lottieAnimationView.pauseAnimation()
    }

    private fun playLottieAnimation() {
        val lottieAnimationView = activityMapBinding.lottieLoading
        lottieAnimationView.setAnimation("loading.json")
        lottieAnimationView.repeatCount = LottieDrawable.INFINITE
        lottieAnimationView.playAnimation()
    }

    private fun setupViewBinding() {
        activityMapBinding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(activityMapBinding.root)
    }

    private fun setupLoadingAnimationFrameLayout() {
        loadingAnimationFrameLayout = activityMapBinding.frameLottieHolder
    }

    private fun setupMap() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.apply {
            setOnMarkerClickListener(this@MapActivity)
            setOnMapClickListener(this@MapActivity)
            uiSettings.isMyLocationButtonEnabled = false
        }
        setupViewModel()
        initializeMapItems()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val tree = mapViewModel.treeListLiveData.value?.find { it.treeId == marker.tag }
        if (tree != null) {
            bindBottomSheetData(tree, marker)
            expandBottomSheet()
        } else {
            Toast.makeText(
                this,
                "Invalid marker item, Please refresh the data.",
                Toast.LENGTH_SHORT
            ).show()
        }
        return true
    }

    override fun onMapClick(p0: LatLng) {
        collapseBottomSheet()
    }

    @SuppressLint("MissingPermission")
    private fun initializeMapItems() {
        if (!::googleMap.isInitialized) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            updateAroundTreeList()
        } else {
            requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            initializeMapItems()
        } else {
            permissionDenied = true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    @SuppressLint("MissingPermission")
    private fun updateAroundTreeList() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            collapseBottomSheet()
            showLoadingAnimation()
            // 앱 초기 구동 시 lastLocation 의도치 않은 곳으로 잡힐 가능성 존재하므로 항상 currentLocation
            fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                lastKnownLocation = location
                lastKnownLocation?.let {
                    mapViewModel.listAroundTrees(it.latitude, it.longitude, localUserId)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "현재 위치를 불러오는 중 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show()
            }
            /*
            if (lastKnownLocation == null) {
                fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    lastKnownLocation = location
                    lastKnownLocation?.let {
                        mapViewModel.listAroundTrees(it.latitude, it.longitude, localUserId)
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "현재 위치를 불러오는 중 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        lastKnownLocation = location
                        lastKnownLocation?.let {
                            mapViewModel.listAroundTrees(it.latitude, it.longitude, localUserId)
                        }
                    }
            }
            */
        }
    }

    private fun setupViewModel() {
        mapViewModel.treeListLiveData.observe(this, { data ->
            googleMap.clear()
            markerHashMap.clear()
            bookmarkHashMap.clear()
            data?.let { treeList ->
                Log.d(TAG, data.toString())
                for (tree in treeList) {
                    val treeId = tree.treeId
                    val coordinate = LatLng(tree.latitude, tree.longitude)
                    val markerOptions =
                        MarkerOptions().position(coordinate).title(tree.treeName).apply {
                            if (tree.bookmark) {
                                icon(
                                    (BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_ROSE
                                    ))
                                )
                            } else {
                                icon(
                                    (BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_GREEN
                                    ))
                                )
                            }
                        }
                    val marker = googleMap.addMarker(markerOptions)
                    marker?.let { markerItem ->
                        markerItem.tag = treeId
                        markerHashMap[treeId] = markerItem
                    }
                    bookmarkHashMap[treeId] = tree.bookmark
                }
                lastKnownLocation?.let { myLocation ->
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            myLocation.latitude,
                            myLocation.longitude
                        ), 16F
                    )
                    googleMap.animateCamera(cameraUpdate)
                    googleMap.addCircle(
                        CircleOptions()
                            .center(
                                LatLng(
                                    myLocation.latitude,
                                    myLocation.longitude
                                )
                            )
                            .radius(500.0)
                            .strokeColor(0xFF7FB414.toInt())
                            .fillColor(0x22A0E418)
                    )
                }
            }
            hideLoadingAnimation()
        })

        mapViewModel.treeBookmarkResponseLiveData.observe(this, { response ->
            if (!response.isSuccessful) {
                Toast.makeText(this, "Failed to add current tree to favorites.", Toast.LENGTH_SHORT)
                    .show()
                updateAroundTreeList()
            }
        })

        mapViewModel.showErrorToast.observe(this, EventObserver {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
    }

    /**
     * [setupFloatingActionButton]
     * FloatingActionButton 설정
     * TODO 후에 서브 버튼 옆에 Label 추가하기
     */
    @SuppressLint("MissingPermission")
    private fun setupFloatingActionButton() {
        activityMapBinding.fabMapMain.setOnClickListener {
            it.animate().rotationBy(180f)
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }

        // Tree List FAB
        activityMapBinding.fabMapSub1.setOnClickListener {
            val treeList = mapViewModel.treeListLiveData.value
            if (treeList.isNullOrEmpty()) {
                Toast.makeText(this, "There are no trees around you now.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val builder = AlertDialog.Builder(this)
                val adapter =
                    ArrayAdapter<String>(builder.context, android.R.layout.simple_list_item_1)
                for (tree in treeList) {
                    adapter.add("${tree.treeName}  (Distance : ${tree.distance.toInt()}m)")
                }

                builder.apply {
                    setTitle("Tree List")
                    setAdapter(adapter) { _, which ->
                        val tree = treeList[which]
                        val marker = markerHashMap[tree.treeId]
                        if (marker == null) {
                            Toast.makeText(
                                this@MapActivity,
                                "Invalid tree object, please refresh the data.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val coordinate = LatLng(tree.latitude, tree.longitude)
                            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 16F)
                            googleMap.animateCamera(cameraUpdate)
                            bindBottomSheetData(tree, marker)
                            expandBottomSheet()
                        }
                    }
                    show()
                }
            }
        }

        activityMapBinding.fabMapSub2.setOnClickListener {
            updateAroundTreeList()
        }
    }

    /**
     * [showFABMenu]
     * FAB Menu 펼치기
     * */
    private fun showFABMenu() {
        isFABOpen = true
        activityMapBinding.fabMapSub1.animate()
            .translationY(-resources.getDimension(R.dimen.standard_110))
        activityMapBinding.fabMapSub2.animate()
            .translationY(-resources.getDimension(R.dimen.standard_60))
        activityMapBinding.tvLabelSub1.apply {
            alpha = 0.0f
            visibility = View.VISIBLE
            animate().alpha(1.0f)
        }
        activityMapBinding.tvLabelSub2.apply {
            alpha = 0.0f
            visibility = View.VISIBLE
            animate().alpha(1.0f)
        }
    }

    /**
     * [closeFABMenu]
     * FAB Menu 숨기기
     * */
    private fun closeFABMenu() {
        isFABOpen = false
        activityMapBinding.fabMapMain.bringToFront()
        activityMapBinding.fabMapSub1.animate().translationY(0F)
        activityMapBinding.fabMapSub2.animate().translationY(0F)
        activityMapBinding.tvLabelSub1.animate().alpha(0.0f)
        activityMapBinding.tvLabelSub2.animate().alpha(0.0f)
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(activityMapBinding.bottomSheet.root)
    }

    @SuppressLint("SetTextI18n")
    private fun bindBottomSheetData(tree: GetAroundTreeResponseDTO, marker: Marker) {
        activityMapBinding.bottomSheet.apply {
            bsTvTreeName.text = tree.treeName
            bsTvCreatedDate.text = tree.createdDate
            bsTvOwner.text = tree.user.username
            bsTvLevel.text = "Lv.${tree.level}"
            bsTvDistance.text = tree.distance.toInt().toString().plus("m")
            btnBookmark.isActivated = bookmarkHashMap[tree.treeId] == true
            btnBookmark.setOnClickListener {
                btnBookmark.isActivated = !btnBookmark.isActivated
                bookmarkHashMap[tree.treeId] = bookmarkHashMap[tree.treeId] != true
                // TODO API 변경 가능성
                mapViewModel.updateTreeBookmarkState(
                    localUserId,
                    tree.treeId,
                    bookmarkHashMap[tree.treeId] == true
                )
                marker.apply {
                    if (!btnBookmark.isActivated) setIcon(
                        (BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                        ))
                    )
                    else setIcon(
                        (BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_ROSE
                        ))
                    )
                }
            }
        }
    }

    /**
     * [expandBottomSheet]
     * BottomSheet 펼치기
     */
    private fun expandBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * [collapseBottomSheet]
     * BottomSheet 내리기
     */
    private fun collapseBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    /**
     * [onBackPressed]
     * Back 키 눌렀을 때 FAB Menu 닫을 수 있도록 override
     * */
    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            if (isFABOpen) {
                closeFABMenu()
            } else {
                collapseBottomSheet()
            }
        } else {
            if (isFABOpen) {
                closeFABMenu()
            } else {
                super.onBackPressed()
            }
        }
    }
}