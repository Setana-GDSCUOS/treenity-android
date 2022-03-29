package com.setana.treenity.ui.ar

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState
import com.google.ar.core.Config
import com.google.ar.sceneform.Camera
import com.gorisse.thomas.lifecycle.doOnCreate
import com.setana.treenity.R
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.*
import com.setana.treenity.databinding.ArFragmentBinding
import com.setana.treenity.ui.loading.LoadingActivity
import com.setana.treenity.ui.map.MapActivity
import com.setana.treenity.ui.mypage.MyPageActivity
import com.setana.treenity.util.AuthUtils
import com.setana.treenity.util.CloudAnchorManager
import com.setana.treenity.util.EventObserver
import com.setana.treenity.util.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.ar.arcore.depthEnabled
import io.github.sceneview.ar.arcore.planeFindingEnabled
import io.github.sceneview.ar.node.ArModelNode

import io.github.sceneview.ar.node.CursorNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.ar.scene.PlaneRenderer
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import java.lang.Integer.min


/** Ar 화면의 MainView 를 담당하는 Fragment
 * SceneView 부연설명 포함
 * Anchor 는 3D 공간상의 위치정보
 * ArNode 는 모델을 포함하는 3D 공간상의 개채로 앵커를 포함하면 해당 위치에 표시됨
 * */

@AndroidEntryPoint
class ArFragment : Fragment(R.layout.ar_fragment) {
    private lateinit var arFragmentBinding: ArFragmentBinding

    // Floating Action Button
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabProfile: FloatingActionButton
    private lateinit var fabSeed: FloatingActionButton
    private lateinit var fabRefresh: FloatingActionButton
    private lateinit var fabMap: FloatingActionButton

    // Ar session
    private lateinit var cursorNode: CursorNode
    private lateinit var sceneView: ArSceneView
    private lateinit var loadingView: View
    private var cloudAnchorManager = CloudAnchorManager()
    private var modelNode: ArModelNode? = null // 주변 모델 로드할 때 사용
    private var seedNode: ArModelNode? = null // 씨앗 심을 때 씨앗에 입히기 위해 사용
    private var textNode: ArModelNode? = null // 씨앗 심을 때 타이틀로 사용

    // 나무 심기 모드 액티베이트
    private var isSeeding = false

    // 위치
    private lateinit var mLocationRequest: com.google.android.gms.location.LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    private var mLastLocation: Location? = null // 위치 값을 가지고 있는 객체
    private val REQUEST_PERMISSION_LOCATION = 10

    // Floating Action Button
    private var isFabOpen = false

    // 뷰모델
    private val arViewModel: ArViewModel by viewModels()

    // 식별
    //PREFS.getLong(USER_ID_KEY, -1)
    private val renderLimit = PREFS.getString(PreferenceManager.RENDER_TREE_NO_KEY, "4")
    private var localUserId = -1L
    //private var isLoggedIn: Boolean = false
    private var once:Boolean = true
    private var selectedItemId:Long = 0
    private var selectedUserItemId:Long = 0

    // 로드된 나무들을 treeId 기준으로 저장하여 다시 로드되는 일 없이 관리. 최대 렌더링수와 분리할 수 있게
    private var resolvedTreeMap: HashMap<Long,Anchor> = hashMapOf()


    /** 로딩뷰 + 액션 버튼 상호작용 활성/비활성 조작용 */
    private var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    /** 메인을 담당할 부분이다. Fragment 전체를 담당 */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        // fragment 의 lifecycle 에 의한 메모리 누수 방지를 위해 inflate 말고 bind 사용
        requireActivity().setStatusBarTransparent()
        arFragmentBinding = ArFragmentBinding.bind(view)
        setUpScene()
        checkUser()
        //  FAB 추가
        setUpFab()
        // 위치 업데이트 시 주위의 앵커를 불러오기 위한 부분
        setUpLocationCheck()
        setUpViewModel()
    }
    private fun Activity.setStatusBarTransparent() {

        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        if(Build.VERSION.SDK_INT >= 30) {	// API 30 에 적용
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    private fun Activity.resetStatusBarTransparent() {

        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
        if(Build.VERSION.SDK_INT >= 30) {	// API 30 에 적용
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    }

    private fun Context.navigationHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
        else 0
    }


    private fun checkUser() {
        if (AuthUtils.userId <= 0) {
            Toast.makeText(requireContext(), "Invalid user credentials!", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoadingActivity::class.java)
            requireActivity().startService(intent)
            requireActivity().finish()
        } else {
            localUserId = AuthUtils.userId
        }
    }


    private fun setUpScene(){
        sceneView = arFragmentBinding.sceneView

        // 뷰의 터치된 장소에 노드를 생성 hitResult 가 터치된 장소. -> 커서노드로 품질 좋은 위치를 선정하도록 유도
        // 씨앗심기 모드에서만 노드 생성 가능
        sceneView.configureSession { _, config ->
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            config.planeFindingEnabled = true
            //config.depthMode = Config.DepthMode.AUTOMATIC
            //config.depthEnabled = true
            config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        }
        sceneView.onArFrame = {
            cloudAnchorManager.onUpdate()
        }
        sceneView.planeRenderer.lifecycle.doOnCreate {
            sceneView.planeRenderer.isShadowReceiver = true
            sceneView.planeRenderer.planeRendererMode = PlaneRenderer.PlaneRendererMode.RENDER_TOP_MOST
        }
        sceneView.onArSessionResumed = {
            // 세션 재시작되면 나무 위치 다 다시 받아와야해서

            arViewModel?.let{
                arViewModel->
                mLastLocation?.let{
                    mLastLocation ->
                    // 이거 false 될 수도 있음
                    clearView(forced = true, feedBack = true)
                    arViewModel.listAroundTrees(mLastLocation.latitude,mLastLocation.longitude,localUserId)
                }
            }
        }

        sceneView.onTouchAr = { _, _ ->
            if(isSeeding && cursorNode.isTracking) {
                if(selectedUserItemId != 0L){
                    cursorNode.createAnchor()?.let { createSeed(it) }
                }
                else{
                    // 아이템 선택하지 않은 경우 아예 눈길도 안 줌
                    isSeeding = false
                }
            }
            else if(isSeeding && !cursorNode.isTracking){
                Toast.makeText(requireContext(), "Choose the place that cursor indicates white color", Toast.LENGTH_SHORT).show()
            }
        }
        loadingView = arFragmentBinding.loadingView
        // 액션버튼은 화면의 직관성을 떨어뜨린다 판단하여 제외. 테스트용으로만 사용중
        //actionButton.isGone = true
        // 지면에 뜨는 CursorNode 생성하여 객제에 할당
        cursorNode = CursorNode(context = requireContext(), coroutineScope = lifecycleScope)
        sceneView.addChild(cursorNode)

        arFragmentBinding.innerContainer.setPadding(
            0,
            0,
            0,
            requireContext().navigationHeight()
        )

    }

    override fun onStop(){
        Log.d("bimoon","onStop")
        // cloudAnchorManager.clearListeners()
        requireActivity().resetStatusBarTransparent()
        stopLocationUpdates()
        super.onStop()
    }

    override fun onPause() {
        sceneView.arSession?.pause()
        super.onPause()
    }


    /**
     * 기본적인 상호작용 가능한 노드를 생성하는 함수
     * 메뉴나 씨앗심기 동작등을 작성할 때 사용 가능
     * */
    private fun createSeed(anchor: Anchor) {
        //configureSession()
        // Todo 씨앗 종류에 따라서 샘플 종류도 1단계로 나무로 바꿔주기
        isLoading = true
        val modelPath = getSeedModelWithItemIdAndLevel(selectedItemId,1)
        seedNode = ArModelNode(placementMode = PlacementMode.BEST_AVAILABLE).apply {
            loadModelAsync(
                context = requireContext(),
                glbFileLocation = modelPath,
                coroutineScope = lifecycleScope,
                autoAnimate = true,
                autoScale = false,
                // Place the model origin at the bottom center
                centerOrigin = Position(y = -1.0f)
            ) {
                if(selectedItemId!=2L){
                    //seedNode!!.modelScale = Scale(0.3f,0.3f,0.3f)
                }
                seedNode!!.anchor=anchor
                sceneView.addChild(seedNode!!)
                isLoading = false
                /*ViewRenderable.builder()
                    .setView(requireContext(),R.layout.title_button_plant)
                    .build()
                    .thenAccept { renderable ->
                        textNode = ArModelNode(placementMode = PlacementMode.BEST_AVAILABLE).apply {
                            loadModelAsync(
                                context = requireContext(),
                                glbFileLocation = "models/sample.glb",
                                coroutineScope = lifecycleScope,
                                autoAnimate = true,
                                autoScale = false,
                                // Place the model origin at the bottom center
                                centerOrigin = Position(y = +0.1f)
                            )
                            // textNode 는 버튼이 있는 ModelNode 의 좌표에 생성되므로 위치를 올려 줌
                            textNode!!.setModel(renderable)
                            seedNode!!.addChild(textNode!!)
                        }
                    }
                    .exceptionally {
                        throw AssertionError("Could not load title of the button")
                    }*/
            }
        }

        // 이곳에 심으시겠습니까?
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Position Check")
        builder.setMessage("Press continue to plant your tree here")
        builder.setPositiveButton("Continue"){
            _, _ ->
            // Todo 모델노드 미삭제 및 plantTreeAnchor 로의 전송을 통한 즉각적인 interact 허용
            textNode?.let{
                seedNode!!.removeChild(it)
            }
            createCloudAnchorWithAnchor(seedNode!!,anchor)
            isSeeding = false
            isLoading = true
            // modelNode!!.destroy()
        }
        builder.setNegativeButton("Cancel"){
            _, _ ->
            // 캔슬하면 씨앗 다시 고르라그래
            selectedUserItemId = 0
            Toast.makeText(requireContext(), "Seeding plant canceled", Toast.LENGTH_SHORT).show()
            seedNode!!.destroy()
        }
        val alertDialog:AlertDialog = builder.create()
        alertDialog.window!!.setGravity(Gravity.BOTTOM)
        alertDialog.show()
        // Todo 나무이름 등록
    }

    private fun createCloudAnchorWithAnchor(currentNode: ArModelNode,anchor:Anchor?){
        //ttl 여기
        sceneView.arSession?.let{
            cloudAnchorManager.hostCloudAnchor(it,anchor,300,
                object : CloudAnchorManager.CloudAnchorResultListener {
                    override fun onCloudTaskComplete(anchor: Anchor?) {
                        onHostedAnchor(currentNode,anchor)
                    }
                })
        }
    }


    /**
     * Cloud Anchor 등록이 완료되었을 때의 피드백을 위해 작성된 함수
     * 유저에게 클라우드 앵커의 적상작동을 위해서 3D 공간정보를 추가 제공해야 함을 알려줄 수 있음
     * 추가로 cloudAnchor 가 충분한 3D 정보를 가지고 등록되었는지 확인할 수 있는 함수도 있었던 것으로 기억
     * */
    @Synchronized
    private fun onHostedAnchor(currentNode: ArModelNode,anchor:Anchor?){
        if (anchor != null) {
            val anchorState = anchor.cloudAnchorState
            if(anchorState== CloudAnchorState.SUCCESS){
                if(anchor.cloudAnchorId != null){
                    val postTreeDTO= PostTreeRequestDTO(anchor.cloudAnchorId,mLastLocation!!.latitude,mLastLocation!!.longitude,"No Name",selectedUserItemId)
                    // 시드 선택된 상태 아니므로
                    selectedUserItemId = 0
                    arViewModel.postHostedTree(localUserId,postTreeDTO)
                    clearView(forced =false, feedBack = false) // 이거 Forced 안 되게 해야 나무 심고 안 없어진다.
                    arViewModel.listAroundTrees(mLastLocation!!.latitude,mLastLocation!!.longitude,localUserId)
                    seedNode = currentNode // 전역변수로 뷰모델 onTouch설정에 사용
                }
            }else if(anchorState == CloudAnchorState.ERROR_HOSTING_DATASET_PROCESSING_FAILED || anchorState == CloudAnchorState.ERROR_INTERNAL){
                Toast.makeText(requireContext(), "Failed : Not enough 3D information", Toast.LENGTH_SHORT).show()
            }
            else if(anchorState == CloudAnchorState.TASK_IN_PROGRESS){
                Toast.makeText(requireContext(), "Now processing. Please Wait...", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(), "Planting Error : $anchorState", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 등록된 cloudAnchor 를 ID 를 통해 받아옴, session configure 은 Resolve 버튼의 리스너에서 처리 */
    @Synchronized
    fun resolveAnchor(arTree: GetAroundTreeResponseDTO) {
        val cloudAnchorId = arTree.cloudAnchorId
        sceneView.arSession?.let {
            isLoading = true

            cloudAnchorManager.resolveCloudAnchor(
                it,
                cloudAnchorId,
                object : CloudAnchorManager.CloudAnchorResultListener{
                    override fun onCloudTaskComplete(anchor: Anchor?) {
                        if(anchor!=null){
                            when(anchor.cloudAnchorState){
                                CloudAnchorState.SUCCESS -> {
                                    onResolvedAnchor(anchor, arTree)
                                }
                                CloudAnchorState.ERROR_CLOUD_ID_NOT_FOUND->{
                                    //Toast.makeText(requireContext(), "Oops, your tree was dead left unattended too long.", Toast.LENGTH_SHORT).show()
                                    //Todo 나무 정보에서 클라우드 앵커 빼버리면 죽은 거긴 하겠다
                                }
                              else -> {
                                  Toast.makeText(requireContext(), "로드 에러 : " + anchor.cloudAnchorState.toString(), Toast.LENGTH_SHORT).show()
                              }
                            }
                        }
                    }
                }
            )
            isLoading = false
        }
    }

    /** 클라우드 앵커의 로드가 완료되었을 경우에 호출되는 함수
     * 로드된 앵커 위에 노드를 덧씌워 준다.
     * */
    @Synchronized
    private fun onResolvedAnchor(anchor:Anchor?, arTree: GetAroundTreeResponseDTO) {
        val itemId = arTree.itemId
        val level = arTree.level
        var modelPath = getSeedModelWithItemIdAndLevel(itemId,level)
        val treeId = arTree.treeId
        if (anchor != null) {
            modelNode = ArModelNode(placementMode = PlacementMode.BEST_AVAILABLE).apply {
                loadModelAsync(
                    context = requireContext(),
                    glbFileLocation = modelPath,
                    coroutineScope = lifecycleScope,
                    autoAnimate = true,
                    autoScale = false,
                    // Place the model origin at the bottom center
                    centerOrigin = Position(x=+0.3f,y=-0.1f,z=+0.3f)
                ){
                    if(itemId!=2L){
                        // 하드코딩 모델마다 리스케일이 좀 필요할 것 같습니다.
                        //modelNode!!.modelScale = Scale(0.3f,0.3f,0.3f)
                    }
                    modelNode!!.modelScale = Scale(2f,2f,2f)
                    modelNode!!.onTouched = { _, _ ->
                        arViewModel.treeListLiveData.value?.find { it.treeId == treeId }
                        arViewModel.getTreeInformation(treeId)
                    }
                    modelNode!!.anchor = anchor
                    sceneView.addChild(modelNode!!)
                    resolvedTreeMap[treeId] = anchor
                    isLoading = false
                }
            }
        }
    }

    // 위치 권한이 있는지 확인하는 메서드
    @RequiresApi(Build.VERSION_CODES.S)
    private fun setUpLocationCheck(){
        checkPermissionForLocation(requireContext())
        mLocationRequest = com.google.android.gms.location.LocationRequest.create()
        mLocationRequest.priority = LocationRequest.QUALITY_HIGH_ACCURACY
        mLocationRequest.interval = 5 * 1000
        startLocationUpdates()
    }
    private fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 지리 확보(위치) 권한에 추가 런타임 권한이 필요합니다.
        return if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            // 권한이 없으므로 권한 요청 알림 보내기
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
            false
        }
    }

    // 사용자에게 권한 요청 후 결과에 대한 처리 로직
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
                // View Button 활성화 상태 변경
            } else {
                Toast.makeText(requireContext(), "You have no permission.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        //FusedLocationProviderClient 의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mFusedLocationProviderClient!!.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                mLastLocation = location
                if(once){
                    arViewModel.let {
                        it.listAroundTrees(mLastLocation!!.latitude,mLastLocation!!.longitude,localUserId)
                        once = false
                    }
                }
            }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest,
            mLocationCallback,
            Looper.getMainLooper())
    }

    // 시스템으로 부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }
    // 위치가 바뀌면 인식해서 전달하는 메소드
    fun onLocationChanged(location: Location) {
        val distance = location.distanceTo(mLastLocation)
        // 위치 정확도 관련해서도 추가하면 좋을 듯
        // Todo 하드코딩 나무로드조건
        if (distance > 15) {
            // 일정 거리 이상 이동시 주변의 나무 불러옴
            arViewModel.listAroundTrees(location.latitude,location.longitude, localUserId)
        }
        mLastLocation = location
    }
    // 위치 업데이터를 제거 하는 메서드
    private fun stopLocationUpdates() {
        // 지정된 위치 결과 리스너에 대한 모든 위치 업데이트를 제거
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }


    private fun setUpFab(){
        fabMain = arFragmentBinding.fabMain
        fabProfile = arFragmentBinding.fabMyPage
        fabSeed = arFragmentBinding.fabSeed
        fabRefresh = arFragmentBinding.fabRefresh
        fabMap = arFragmentBinding.fabMap
        fabMain.setImageResource(R.drawable.ic_ar_floating_open)
        fabMain.setOnClickListener{
            toggleFab()
        }
        fabProfile.setOnClickListener{
            // Todo 마이페이지 액티비티로의 연결
            startMyPageActivity()
        }
        fabSeed.setOnClickListener{
            isSeeding = true
            val instructionDialog = ArInstructionDialog(requireContext())
            instructionDialog.createInstruction()
            instructionDialog.setButtonListener(object:ArInstructionDialog.ArInstructionDialogListener{
                override fun onButtonClickListener() {
                    arViewModel.getUserItems(localUserId)
                }
            })
            // 선택된 아이템은 전역변수 공유
        }
        fabRefresh.setOnClickListener{
            clearView(forced = true, feedBack = true)
            arViewModel.listAroundTrees(mLastLocation!!.latitude, mLastLocation!!.longitude, localUserId)
        }
        fabMap.setOnClickListener{
            startMapActivity()
        }
    }

    /**
     * 토글버튼 애니메이션
     * */
    private fun toggleFab(){
        // 플로팅 액션 버튼 닫기
        if(isFabOpen){
            closeFab()
        }
        // 플로팅 액션 버튼 열기
        else{
            openFab()
        }
        isFabOpen = !isFabOpen
    }

    private fun closeFab(){
        ObjectAnimator.ofFloat(fabMain,"scaleX",+1f).apply{ start()}
        ObjectAnimator.ofFloat(fabMain,"scaleY",+1f).apply{ start()}
        ObjectAnimator.ofFloat(fabMap, "translationY", -0f).apply{ start()}
        ObjectAnimator.ofFloat(fabRefresh, "translationX", -0f).apply{ start()}
        ObjectAnimator.ofFloat(fabSeed, "translationX", -0f).apply{ start()}
        ObjectAnimator.ofFloat(fabSeed, "translationY", -0f).apply{ start()}
        ObjectAnimator.ofFloat(fabProfile, "translationX", -0f).apply{ start()}
        ObjectAnimator.ofFloat(fabProfile, "translationY", -0f).apply{ start()}
        ObjectAnimator.ofFloat(fabMain,"rotation",0.0f).apply{start()}
        //fabMain.setImageResource(R.drawable.ic_ar_floating_open)
    }

    private fun openFab() {
        ObjectAnimator.ofFloat(fabMain,"scaleX",+1.5f).apply{ start()}
        ObjectAnimator.ofFloat(fabMain,"scaleY",+1.5f).apply{ start()}
        ObjectAnimator.ofFloat(fabMap, "translationY", -300f).apply{ start()}
        ObjectAnimator.ofFloat(fabRefresh, "translationX", -300f).apply{ start()}
        ObjectAnimator.ofFloat(fabSeed, "translationX", -260f).apply{ start()}
        ObjectAnimator.ofFloat(fabSeed, "translationY", -150f).apply{ start()}
        ObjectAnimator.ofFloat(fabProfile, "translationX", -150f).apply{ start()}
        ObjectAnimator.ofFloat(fabProfile, "translationY", -260f).apply{ start()}
        ObjectAnimator.ofFloat(fabMain,"rotation",45.0f).apply{start()}
        //fabMain.setImageResource(R.drawable.ic_ar_floating_close)
    }

    /**
    * 뷰모델과의 상호작용 부분입니다.
    * */
    private fun setUpViewModel() {
        // 세션은 빠르게 세팅해줘야 안 튕기고 잘 돌아감 여기서 하는 것도 안 될 수 있음

        arViewModel.treeListLiveData.observe(viewLifecycleOwner) { treeList ->
            treeList?.let { it ->
                val renderSize = min(treeList.size,renderLimit.toInt())-1
                for(i in 0..renderSize){
                    // 거리순으로 정렬되기 때문에 주변 나무 갯수/최대 렌더링수중에서 고르기
                    resolveNotResolvedAnchor(it[i])
                }
                /*
                for (arTree in it) {
                    resolveNotResolvedAnchor(arTree)
                }
                */
            }
            isLoading = false
        }

        arViewModel.treeInformationResponseLiveData.observe(viewLifecycleOwner){
            treeInformation ->
            treeInformation?.let{
                createInteractDialog(it)
            }
        }

        arViewModel.postTreeResponseLiveData.observe(viewLifecycleOwner){
            plantedTree ->
            seedNode?.let {
                seedNode->
                seedNode.onTouched = { _, _ ->
                    arViewModel.treeListLiveData.value?.find { it.treeId == plantedTree.treeId }
                    arViewModel.getTreeInformation(plantedTree.treeId)
                }
                isLoading = false
                Toast.makeText(requireContext(), "Succeed to plant tree! click tree to enter its name!", Toast.LENGTH_SHORT).show()
                arViewModel.getTreeInformation(plantedTree.treeId)
            }
        }

        arViewModel.userItemListLiveData.observe(viewLifecycleOwner){
            itemList->
            itemList?.let{
                createItemDialog(it)
            }

        }

        arViewModel.showErrorToast.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })

    }

    private fun clearView(forced : Boolean, feedBack:Boolean){
        // 어떻게든 최대 렌더링 수를 넘어서 로드되었거나 강제 리로드시 리로드 카메라노드랑 커서노드는 빼야 됨
        if(sceneView.children.size > 2 + renderLimit.toInt() || forced){
            isLoading = true
            // 최대 렌더링 수보다 SceneView 에 자식노드가 많으면 카메라랑 커서 빼고 잘라버림
            for(child in sceneView.children){
                if(child !is Camera && child !=cursorNode){
                    Log.d("target",child.toString())
                    child.destroy()
                }
            }
            resolvedTreeMap.clear()
        }
        if(forced&&feedBack){
            // 강제 리셋시에는 리셋해서 로드중이라는 피드백 제공
            Toast.makeText(requireContext(), "Loading Trees Around...", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 전역변수인 resolvedTreeSet 을 확인하여 등록되지 않은 나무의 경우만 심어 줌
     * */
    private fun resolveNotResolvedAnchor(arTree: GetAroundTreeResponseDTO){
        if (arTree.treeId !in resolvedTreeMap.keys){
            if(sceneView.children.size > renderLimit.toInt() + 1){
                // 새로 로드하면 제한 넘어갈 예정이라면
                clearView(forced= true, feedBack = true)
            }
            else{
                clearView(forced= false, feedBack = false)
            }
            resolveAnchor(arTree)
            // 등록은 api 해시맵에 등록은 모델 로드까지 완료되면

        }
    }

    private fun createItemDialog(userItemList: List<GetUserItemResponseDTO>){
        val arSeedDialog = ArSeedDialog(requireContext(),userItemList)
        arSeedDialog.createDialog()
        arSeedDialog.setListener(object :ArSeedDialog.ArSeedDialogListener{
            override fun onItemClickListener(userItemId:Long,itemId: Long) {
                //Toast.makeText(requireContext(), "Seed Selected", Toast.LENGTH_SHORT).show()
                selectedUserItemId = userItemId
                selectedItemId = itemId
                Toast.makeText(requireContext(), "Tap plane to plant on Cursor.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createInteractDialog(treeInfoResponse:GetTreeInformationResponseDTO){
        // 커스텀 다이얼로그를 통한 나무 정보 획득, info 부분에는 나무 설명의 수정과 등록이 가능하도록
        val isTreeOwner = treeInfoResponse.user.userId == localUserId
        Log.d("interact",treeInfoResponse.toString())

        val oldInfo = PutTreeInfoRequestDTO(treeInfoResponse.bookmark,treeInfoResponse.treeDescription,treeInfoResponse.treeName)
        if(oldInfo.treeDescription==null){
            oldInfo.treeDescription = "There is no description"
        }
        if(oldInfo.treeName==null){
            oldInfo.treeName = "No name"
        }

        val arDialogFragment = ArInfoDialogFragment(requireContext(),treeInfoResponse,isTreeOwner)
        arDialogFragment.createDialog()
        arDialogFragment.setListener(object: ArInfoDialogFragment.ArDialogListener{
            override fun onNameSaveListener(treeName: String,description:String) {
                Toast.makeText(requireContext(), "Tree name saved", Toast.LENGTH_SHORT).show()
                //Todo 북마크 끌어오기
                val treeInfoDTO = PutTreeInfoRequestDTO(treeInfoResponse.bookmark,description,treeName)
                arViewModel.putTreeInfo(treeInfoResponse.user.userId,treeInfoResponse.treeId,treeInfoDTO)
            }
            override fun onDescriptionSaveListener(treeName: String,description: String) {
                Toast.makeText(requireContext(), "Description saved", Toast.LENGTH_SHORT).show()
                val treeInfoDTO = PutTreeInfoRequestDTO(treeInfoResponse.bookmark,description,treeName)
                arViewModel.putTreeInfo(treeInfoResponse.user.userId,treeInfoResponse.treeId,treeInfoDTO)
            }

            override fun onWaterListener(treeId: Long) {
                //TODO("여기 물주기 "), 클라우드 앵커 아이디 재발급받아서 서버에 등록하기
                //Toast.makeText(requireContext(), "물주기 반환값 : $treeId", Toast.LENGTH_SHORT).show()
                refreshCloudAnchorId(treeId)
                // 물 주고 나면 갱신 필수 (레벨 업 할 수도 있으니) 다른 문구 있으니 로드 피드백은 안 줌
                clearView(forced = true, feedBack = false)
                arViewModel.listAroundTrees(mLastLocation!!.latitude,mLastLocation!!.longitude,localUserId)
            }
        })

    }


    private fun refreshCloudAnchorId(treeId: Long){
        // 이부분 지금 갱신 아예 빼놨음
        val oldAnchor = resolvedTreeMap[treeId]

        if(oldAnchor!=null) {
            //var newCloudAnchor = createCloudAnchorWithAnchor(oldAnchor)
            val waterTreeRequestDTO = WaterTreeRequestDTO(oldAnchor.cloudAnchorId)
            Log.d("water",oldAnchor.cloudAnchorId)
            Log.d("water",waterTreeRequestDTO.toString())
            if (oldAnchor.cloudAnchorId != null) {
                arViewModel.waterTree(localUserId, treeId, waterTreeRequestDTO)
            }
        }
    }

    private fun startMapActivity() {
        val intent = Intent(requireContext(), MapActivity::class.java)
        startActivity(intent)
    }

    private fun startMyPageActivity() {
        val intent = Intent(requireContext(), MyPageActivity::class.java)
        startActivity(intent)
    }

    private fun startLoadingActivity() {
        val intent = Intent(requireContext(), LoadingActivity::class.java)
        startActivity(intent)
    }

    private fun getSeedModelWithItemIdAndLevel(itemId:Long,level: Int):String {
        when(itemId){
            3L->{
                return when(level){
                    1 -> {
                        getString(R.string.sakura_1)
                    }
                    2->{
                        getString(R.string.sakura_2)
                    }
                    3->{
                        getString(R.string.sakura_3)
                    }
                    4->{
                        getString(R.string.sakura_4)
                    }
                    else -> {

                        getString(R.string.sakura_4)
                    }
                }
            }
            2L->{
                return when(level){
                    1 -> {
                        getString(R.string.basic_1)
                    }
                    else -> {

                        getString(R.string.basic_1)
                    }
                }
            }
            else ->{
                return ""
            }
        }
    }
}