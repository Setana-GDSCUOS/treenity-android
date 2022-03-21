package com.setana.treenity.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.setana.treenity.R
import com.setana.treenity.data.api.dto.GetAroundTreeResponseDTO
import com.setana.treenity.databinding.ActivityMapBinding
import com.setana.treenity.util.EventObserver
import com.setana.treenity.util.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.setana.treenity.util.PermissionUtils.isPermissionGranted
import com.setana.treenity.util.PermissionUtils.requestPermission
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private lateinit var activityMapBinding: ActivityMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var loadingAnimationFrameLayout: FrameLayout
    private var lastKnownLocation: Location? = null
    private var permissionDenied = false
    private var isFABOpen = false
    private val mapViewModel: MapViewModel by viewModels()
    private val cancellationTokenSource = CancellationTokenSource()

    companion object {
        private const val TAG = "MapActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        setupViewModel()
    }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
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
        map.apply {
            setOnMarkerClickListener(this@MapActivity)
            setOnMapClickListener(this@MapActivity)
            setOnMyLocationButtonClickListener(this@MapActivity)
            setOnMyLocationClickListener(this@MapActivity)
        }
        initializeMapItems()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Toast.makeText(this, "마커 클릭됨", Toast.LENGTH_SHORT).show()
        Log.d("############", "마커 클릭됨")
        val tree = mapViewModel.treeListLiveData.value?.find { it.treeId == marker.tag }
        expandBottomSheet()
        tree?.let { setBottomSheetData(it) }
        return true
    }

    override fun onMapClick(p0: LatLng) {
        Toast.makeText(this, "지도 클릭됨", Toast.LENGTH_SHORT).show()
        Log.d("############", "지도 클릭됨")
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

    override fun onMyLocationButtonClick(): Boolean {
        // Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(location: Location) {
        // Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
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
            showLoadingAnimation()
            if (lastKnownLocation == null) {
                fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    lastKnownLocation = location
                    lastKnownLocation?.let {
                        // Todo userId 연결 부분 추가 필요
                        mapViewModel.listAroundTrees(it.latitude, it.longitude, 1)
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "현재 위치를 불러오는 중 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        lastKnownLocation = location
                        lastKnownLocation?.let {
                            // Todo userId 연결 부분 추가 필요
                            mapViewModel.listAroundTrees(it.latitude, it.longitude, 1)
                        }
                    }
            }
        }
    }

    private fun setupViewModel() {
        mapViewModel.treeListLiveData.observe(this, { treeList ->
            googleMap.clear()
            treeList?.let { it ->
                Log.d(TAG, treeList.toString())
                for (tree in it) {
                    val coordinate = LatLng(tree.latitude, tree.longitude)
                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(coordinate)
                            .title(tree.treeId.toString())
                    )
                    marker?.tag = tree.treeId
                }
                lastKnownLocation?.let {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.latitude,
                            it.longitude
                        ), 16F
                    )
                    googleMap.animateCamera(cameraUpdate)
                    googleMap.addCircle(
                        CircleOptions()
                            .center(
                                LatLng(
                                    it.latitude,
                                    it.longitude
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
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }

        // Tree List
        activityMapBinding.fabMapSub1.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val adapter = ArrayAdapter<String>(builder.context, android.R.layout.simple_list_item_1)
            for (tree in mapViewModel.treeListLiveData.value!!) {
                adapter.add(tree.treeId.toString())
            }

            builder.apply {
                setTitle("Tree List")
                setAdapter(adapter) { _, which ->
                    val tree = mapViewModel.treeListLiveData.value!![which]
                    val coordinate = LatLng(tree.latitude, tree.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 16F)
                    googleMap.animateCamera(cameraUpdate)
                    setBottomSheetData(tree)
                    expandBottomSheet()
                }
                show()
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
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(activityMapBinding.bottomSheet.root)
    }

    private fun setBottomSheetData(tree: GetAroundTreeResponseDTO) {
        activityMapBinding.bottomSheet.apply {
            bsTvTreeName.text = tree.treeId.toString()
            bsTvCreatedDate.text = tree.createdDate
            bsTvUsername.text = tree.username
            bsTvDistance.text = tree.distance.toString() + "m"
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