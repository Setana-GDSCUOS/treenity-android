package com.setana.treenity.ui.ar

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
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
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState
import com.google.ar.core.Config
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.setana.treenity.R
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.*
import com.setana.treenity.databinding.ArFragmentBinding
import com.setana.treenity.ui.loading.LoadingActivity
import com.setana.treenity.ui.map.MapActivity
import com.setana.treenity.ui.mypage.MyPageActivity
import com.setana.treenity.util.CloudAnchorManager
import com.setana.treenity.util.EventObserver
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.depthEnabled
import io.github.sceneview.ar.arcore.planeFindingEnabled
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.CursorNode
import io.github.sceneview.ar.scene.PlaneRenderer
import io.github.sceneview.utils.doOnApplyWindowInsets


/** Ar 화면의 MainView 를 담당하는 Fragment
 * SceneView 부연설명 포함
 * Anchor 는 3D 공간상의 위치정보
 * ArNode 는 모델을 포함하는 3D 공간상의 개채로 앵커를 포함하면 해당 위치에 표시됨
 * */

@AndroidEntryPoint
class ArFragment : Fragment(R.layout.ar_fragment) {
    private lateinit var actionButton: ExtendedFloatingActionButton
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
    private var modelNode: ArNode? = null
    private var textNode: ArNode? = null
    private var isPlaneRenderEnabled = false

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
    private val userId = PREFS.getLong(USER_ID_KEY, -1)
    private var isLoggedIn: Boolean = false
    private var once:Boolean = true

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
        arFragmentBinding = ArFragmentBinding.bind(view)
        checkUserId()
        setUpScene()
        //  FAB 추가
        setUpFab()
        // 위치 업데이트 시 주위의 앵커를 불러오기 위한 부분
        setUpLocationCheck()
        setUpViewModel()

    }


    private fun checkUserId(){
        if(userId != -1L){
            isLoggedIn = true
        }
        else{
            // Todo 로그인 액티비티로
            startLoadingActivity()
        }
    }


    private fun setUpScene(){
        sceneView = arFragmentBinding.sceneView
        // 뷰의 터치된 장소에 노드를 생성 hitResult 가 터치된 장소. -> 커서노드로 품질 좋은 위치를 선정하도록 유도
        // 씨앗심기 모드에서만 노드 생성 가능
        sceneView.configureSession {
            it.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            it.planeFindingEnabled = true
            it.depthMode = Config.DepthMode.DISABLED
            it.depthEnabled = false
            it.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
            it.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        }


        sceneView.onArFrameUpdated = {
            // CloudAnchorManager 의 onUpdate 는 Frame 이 업데이트되어 Session 이 갱신될 때 작동하도록 설계되어 있음.
            cloudAnchorManager.onUpdate()
            /*
            if(isPlaneRenderEnabled){
                sceneView.planeRenderer.isEnabled = true
                sceneView.planeRenderer.material!!.setFloat(PlaneRenderer.MATERIAL_SPOTLIGHT_RADIUS,
                    Float.MAX_VALUE)
            }
            */
        }

        sceneView.onTouchAr = { _, _ ->
            if(isSeeding && cursorNode.isTracking) {
                cursorNode.createAnchor()?.let { createSeed(it) }
            }
            else if(isSeeding && !cursorNode.isTracking){
                Toast.makeText(requireContext(), "Choose the place that cursor indicates white color", Toast.LENGTH_SHORT).show()
            }
        }
        loadingView = arFragmentBinding.loadingView
        // 액션버튼은 화면의 직관성을 떨어뜨린다 판단하여 제외. 테스트용으로만 사용중
        actionButton =arFragmentBinding.actionButton.apply {
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
        }
        actionButton.isGone = true
        // 지면에 뜨는 CursorNode 생성하여 객제에 할당
        cursorNode = CursorNode(context = requireContext(), coroutineScope = lifecycleScope)
        sceneView.addChild(cursorNode)
    }

    override fun onDestroy() {
        super.onDestroy()
        cloudAnchorManager.clearListeners()
        stopLocationUpdates()
    }

    /**
     * 기본적인 상호작용 가능한 노드를 생성하는 함수
     * 메뉴나 씨앗심기 동작등을 작성할 때 사용 가능
     * */
    private fun createSeed(anchor: Anchor) {
        //configureSession()
        // Todo 씨앗 종류에 따라서 샘플 종류도 1단계로 나무로 바꿔주기
        isLoading = true
        modelNode = ArNode(
            modelGlbFileLocation = "models/sample.glb",
            context = requireContext(),
            coroutineScope = lifecycleScope,
            onModelLoaded = {
                //actionButton.text = getString(R.string.hosted_tree)
                //actionButton.icon = resources.getDrawable(R.drawable.ic_target)
                //actionButton.icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_target,null)
                ViewRenderable.builder()
                    .setView(requireContext(),R.layout.title_button_plant)
                    .build()
                    .thenAccept { renderable->
                        // button_plant 라는 textview xml 을 3D 모델로 변환하여 버튼 위에 표시
                        textNode= ArNode(
                            // 기 생성된 layout xml 을 사용하여 렌더링
                            viewLayoutResId = R.id.button_plant,
                            context = requireContext(),
                            coroutineScope = lifecycleScope,
                        )
                        // textNode 는 버튼이 있는 ModelNode 의 좌표에 생성되므로 위치를 올려 줌
                        textNode!!.positionY+=0.05f
                        textNode!!.setRenderable(renderable)
                        modelNode!!.addChild(textNode!!)
                    }
                    .exceptionally {
                        throw AssertionError("Could not load title of the button")
                    }
                isLoading = false
            }
        )
        modelNode!!.anchor=anchor
        sceneView.addChild(modelNode!!)

        // 이곳에 심으시겠습니까?
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Position Check")
        builder.setMessage("Press continue to plant your tree here")
        builder.setPositiveButton("Continue"){
            _, _ ->
            plantTreeAnchor(anchor)
            isSeeding = false
            modelNode!!.destroy()
        }
        builder.setNegativeButton("Cancel"){
            _, _ ->
            Toast.makeText(requireContext(), "Seeding plant canceled", Toast.LENGTH_SHORT).show()
            modelNode!!.destroy()
        }
        val alertDialog:AlertDialog = builder.create()
        alertDialog.window!!.setGravity(Gravity.BOTTOM)
        alertDialog.show()
        // Todo 나무이름 등록
    }


    /** Cloud Anchor Mode 로 등록 가능한 Tree Anchor 를 생성하는 함수 */
    private fun plantTreeAnchor(anchor: Anchor) {
        createCloudAnchorWithAnchor(anchor)
    }


    private fun createCloudAnchorWithAnchor(anchor:Anchor?){
        //ttl 여기
        sceneView.session?.let{
            cloudAnchorManager.hostCloudAnchor(it,anchor,7,
                object : CloudAnchorManager.CloudAnchorResultListener {
                    override fun onCloudTaskComplete(anchor: Anchor?) {
                        onHostedAnchor(anchor)
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
    private fun onHostedAnchor(anchor:Anchor?){
        if (anchor != null) {
            val anchorState = anchor.cloudAnchorState
            if(anchorState== CloudAnchorState.SUCCESS){
                // Todo 여기 아이템아이디 고유화 하드코딩

                if(anchor.cloudAnchorId != null){
                    val postTreeDTO= PostTreeRequestDTO(anchor.cloudAnchorId,mLastLocation!!.latitude,mLastLocation!!.longitude,"DEFAULT NAME",7)
                    arViewModel.postHostedTree(userId,postTreeDTO)
                    clearView(true) // 이거 Forced Reload
                    arViewModel.listAroundTrees(mLastLocation!!.latitude,mLastLocation!!.longitude,userId)
                }
            }else if(anchorState == CloudAnchorState.ERROR_HOSTING_DATASET_PROCESSING_FAILED || anchorState == CloudAnchorState.ERROR_INTERNAL){
                Toast.makeText(requireContext(), "3D 공간의 정보가 부족합니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show()
            }
            else if(anchorState == CloudAnchorState.TASK_IN_PROGRESS){
                Toast.makeText(requireContext(), "등록 작업이 진행중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(), "등록 에러 : $anchorState", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 등록된 cloudAnchor 를 ID 를 통해 받아옴, session configure 은 Resolve 버튼의 리스너에서 처리 */
    @Synchronized
    fun resolveAnchor(cloudAnchorId:String, treeId: Long, level: Int) {
        sceneView.session?.let {
            isLoading = true

            cloudAnchorManager.resolveCloudAnchor(
                it,
                cloudAnchorId,
                object : CloudAnchorManager.CloudAnchorResultListener{
                    override fun onCloudTaskComplete(anchor: Anchor?) {
                        if(anchor!=null){
                            when(anchor.cloudAnchorState){
                                CloudAnchorState.SUCCESS -> {
                                    onResolvedAnchor(anchor, treeId, level)
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
    private fun onResolvedAnchor(anchor:Anchor?, treeId: Long, level: Int){
        // 레벨 반영비율 + 상수로 크기 결정, 따로 value 로 관리해도 좋을 것 같음
        var fLevel = 0.5f*level.toFloat()
        var modelPath = "models/sample.glb"
        if (level ==2){
            //Todo 나중에 이거 해시맵으로
            modelPath = "models/acorn_sample.glb"
            fLevel*=0.005f
        }
        if(anchor!=null){
            modelNode = ArNode(
                //로드하는 모델의 종류는 서버에서 받아와야 함
                modelGlbFileLocation = modelPath,
                context = requireContext(),
                coroutineScope = lifecycleScope,
                scales = Vector3(0.3f+fLevel,0.3f+fLevel,0.3f+fLevel),
                onModelLoaded = {
                    isLoading = false
                }
            )
            modelNode!!.onTap = { _,_ ->
                arViewModel.treeListLiveData.value?.find { it.treeId == treeId }
                arViewModel.getTreeInformation(treeId)
            }
            modelNode!!.anchor = anchor
            sceneView.addChild(modelNode!!)
            resolvedTreeMap[treeId] = anchor
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
                        it.listAroundTrees(mLastLocation!!.latitude,mLastLocation!!.longitude,userId)
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
        if (distance > 30) {
            // 일정 거리 이상 이동시 주변의 나무 불러옴
            arViewModel.listAroundTrees(location.latitude,location.longitude, userId)
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
            // Todo 씨앗 템창으로 연결 악 씨앗 템창 언제만들어!!!
            Toast.makeText(requireContext(), "Tap to plant on Cursor.", Toast.LENGTH_SHORT).show()
            isSeeding = true
        }
        fabRefresh.setOnClickListener{
            clearView(true)
            arViewModel.listAroundTrees(mLastLocation!!.latitude, mLastLocation!!.longitude, userId)
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
        fabMain.setImageResource(R.drawable.ic_ar_floating_open)
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
        fabMain.setImageResource(R.drawable.ic_ar_floating_close)
    }

    /**
    * 뷰모델과의 상호작용 부분입니다.
    * */
    private fun setUpViewModel() {
        // 세션은 빠르게 세팅해줘야 안 튕기고 잘 돌아감 여기서 하는 것도 안 될 수 있음

        arViewModel.treeListLiveData.observe(viewLifecycleOwner) { treeList ->
            treeList?.let { it ->
                for (arTree in it) {
                    val cloudAnchorID = arTree.cloudAnchorId
                    val treeId = arTree.treeId
                    val level = arTree.level
                    Log.d("arTree",arTree.toString())
                    //if(cloudAnchorID != null) {
                    resolveNotResolvedAnchor(cloudAnchorID,treeId,level)
                    //}
                }
            }
        }

        arViewModel.treeInformationResponseLiveData.observe(viewLifecycleOwner){
            treeInformation ->
            treeInformation?.let{
                createInteractDialog(it)
            }
        }

        arViewModel.showErrorToast.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })
    }

    private fun clearView(forced : Boolean){
        // Todo 여기 최대 렌더링 수
        if(sceneView.children.size>10 || forced){
            // 최대 렌더링 수보다 SceneView 에 자식노드가 많으면 카메라랑 커서 빼고 잘라버림
            for(child in sceneView.children){
                if(child !is Camera && child !=cursorNode){
                    Log.d("target",child.toString())
                    child.destroy()
                }
            }
            resolvedTreeMap.clear()
        }

    }

    /**
     * 전역변수인 resolvedTreeSet 을 확인하여 등록되지 않은 나무의 경우만 심어 줌
     * */
    private fun resolveNotResolvedAnchor(cloudAnchorId: String,treeId: Long, level: Int){
        clearView(false)
        if (treeId !in resolvedTreeMap.keys){
            resolveAnchor(cloudAnchorId,treeId,level)
            // 등록은 api 해시맵에 등록은 모델 로드까지 완료되면
        }
    }

    private fun createInteractDialog(treeInfoResponse:GetTreeInformationResponseDTO){
        // 커스텀 다이얼로그를 통한 나무 정보 획득, info 부분에는 나무 설명의 수정과 등록이 가능하도록
        val isTreeOwner = treeInfoResponse.user.userId == userId
        Log.d("interact",treeInfoResponse.toString())

        val oldInfo = PutTreeInfoRequestDTO(treeInfoResponse.bookmark,treeInfoResponse.treeDescription,treeInfoResponse.treeName)
        if(oldInfo.treeDescription==null){
            oldInfo.treeDescription = "There is no description"
        }
        if(oldInfo.treeName==null){
            oldInfo.treeName = "No name"
        }

        val arDialogFragment = ArDialogFragment(requireContext(),treeInfoResponse,isTreeOwner)
        arDialogFragment.createDialog()
        arDialogFragment.setListener(object: ArDialogFragment.ArDialogListener{
            override fun onNameSaveListener(treeName: String) {
                Toast.makeText(requireContext(), "Tree name saved", Toast.LENGTH_SHORT).show()
                //Todo 북마크 끌어오기
                val treeInfoDTO = PutTreeInfoRequestDTO(treeInfoResponse.bookmark,oldInfo.treeDescription,treeName)
                arViewModel.putTreeInfo(treeInfoResponse.user.userId,treeInfoResponse.treeId,treeInfoDTO)
            }
            override fun onDescriptionSaveListener(description: String) {
                Toast.makeText(requireContext(), "Description saved", Toast.LENGTH_SHORT).show()
                val treeInfoDTO = PutTreeInfoRequestDTO(treeInfoResponse.bookmark,description,oldInfo.treeName)
                arViewModel.putTreeInfo(treeInfoResponse.user.userId,treeInfoResponse.treeId,treeInfoDTO)
            }

            override fun onWaterListener(treeId: Long) {
                //TODO("여기 물주기 "), 클라우드 앵커 아이디 재발급받아서 서버에 등록하기
                //Toast.makeText(requireContext(), "물주기 반환값 : $treeId", Toast.LENGTH_SHORT).show()
                refreshCloudAnchorId(treeId)
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
                arViewModel.waterTree(userId, treeId, waterTreeRequestDTO)
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
}