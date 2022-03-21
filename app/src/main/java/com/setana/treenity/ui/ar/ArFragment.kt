package com.setana.treenity.ui.ar

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState
import com.google.ar.core.Config
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.setana.treenity.R
import com.setana.treenity.data.api.dto.*
import com.setana.treenity.data.model.ArTree
import com.setana.treenity.data.model.Tree
import com.setana.treenity.databinding.ArFragmentBinding
import com.setana.treenity.databinding.ArInfoDialogBinding
import com.setana.treenity.ui.map.MapActivity
import com.setana.treenity.ui.map.MapViewModel
import com.setana.treenity.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.internal.aggregatedroot.codegen._com_setana_treenity_TreenityApplication
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.CursorNode
import io.github.sceneview.ar.scene.PlaneRenderer
import io.github.sceneview.node.Node
import io.github.sceneview.utils.doOnApplyWindowInsets
import kotlin.reflect.typeOf


/** Ar화면의 MainView를 담당하는 Fragment
 * SceneView 부연설명 포함
 * Anchor는 3D공간상의 위치정보
 * ArNode는 모델을 포함하는 3D 공간상의 개채로 앵커를 포함하면 해당 위치에 표시됨
 * */

@AndroidEntryPoint
class ArFragment : Fragment(R.layout.ar_fragment) {




    lateinit var actionButton: ExtendedFloatingActionButton
    lateinit var arFragmentBinding: ArFragmentBinding

    // Floating Action Button
    lateinit var fabMain: FloatingActionButton
    lateinit var fabProfile: FloatingActionButton
    lateinit var fabSeed: FloatingActionButton

    // Ar session
    lateinit var cursorNode: CursorNode
    lateinit var session: ArSession
    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    var cloudAnchorManager = CloudAnchorManager()
    var modelNode: ArNode? = null
    var textNode: ArNode? = null
    var isSessionConfigured = false

    // 나무 심기 모드 액티베이트
    private var isSeeding = false

    // 위치
    lateinit var mLocationRequest: com.google.android.gms.location.LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    private var mLastLocation: Location? = null // 위치 값을 가지고 있는 객체
    private val REQUEST_PERMISSION_LOCATION = 10

    // Floating Action Button
    private var isFabOpen = false

    // 뷰모델
    private val arViewModel: ArViewModel by viewModels()

    //테스트용
    lateinit var clipboardManager: ClipboardManager

    // 수정요함
    private val USER_ID_KEY = "userId"



    // 식별
    lateinit var sharedPreferences: SharedPreferences
    var userId: Long = -1
    var isLoggedIn: Boolean = false
    var once:Boolean = true

    // 로드된 나무들을 treeId 기준으로 저장하여 다시 로드되는 일 없이 관리. 최대 렌더링수와 분리할 수 있게
    //var resolvedTreeIdSet : MutableSet<Long> = mutableSetOf()
    //var resolvedTreeAnchorSet : MutableSet<Anchor> = mutableSetOf()
    // 아래거 사용하면 위에거 필요없음
    var resolvedTreeMap: HashMap<Long,Anchor> = hashMapOf()

    /** 로딩뷰 + 액션 버튼 상호작용 활성/비활성 조작용 */
    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
            //actionButton.isGone = value
        }

    /** 메인을 담당할 부분이다. Fragment 전체를 담당 */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        // fragment의 lifecycle에 의한 메모리 누수 방지를 위해 inflate 말고 bind 사용
        arFragmentBinding = ArFragmentBinding.bind(view)



        setUpSharedData()
        setUpScene()
        //  FAB 추가
        setUpFab()
        // 위치 업데이트 시 주위의 앵커를 불러오기 위한 부분
        setUpLocationCheck()
        setUpViewModel()


    }

    private fun setUpSharedData(){
        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE)
        sharedPreferences.getLong(USER_ID_KEY,-1)
        val editor = sharedPreferences.edit()
        //userId = sharedPreferences.getLong("userId",0)

        // Todo 하드코딩 여기있다!!!
        userId = 1
        if(userId != -1L){
            isLoggedIn = true
        }
        else{
            // Todo 로그인 액티비티로
        }
    }


    private fun setUpScene(){
        sceneView = arFragmentBinding.sceneView
        // 뷰의 터치된 장소에 노드를 생성 hitResult 가 터치된 장소.
        // 씨앗심기 모드에서만 노드 생성 가능
        sceneView.onTouchAr = { hitResult, _ ->
            if(isSeeding) {
                createSeed(hitResult.createAnchor())
            }
        }
        // 씨앗심기 모드로 편입됨.

        loadingView = arFragmentBinding.loadingView
        // 액션버튼은 화면의 직관성을 떨어뜨린다 판단하여 제외. 테스트용으로만 사용중

        actionButton =arFragmentBinding.actionButton.apply {
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            /*
            setOnClickListener{
                //onResolveButtonPressed()
                cursorNode.createAnchor()?.let{
                    if(isSeeding) {
                        createButtonNode(it)
                    }
                }
            }
             */
        }
        actionButton.isGone = true
        // 지면에 뜨는 CursorNode 생성하여 객제에 할당
        cursorNode = CursorNode(context = requireContext(), coroutineScope = lifecycleScope)
        /*cursorNode.onTrackingChanged = { _, isTracking ->
            if (!isLoading) {
                actionButton.isGone = !isTracking
            }
        }
         */

        sceneView.addChild(cursorNode)
        sceneView.onArFrameUpdated = {

            // CloudAnchorManager의 onUpdate는 Frame이 업데이트되어 Session이 갱신될 때 작동하도록 설계되어 있음.
            cloudAnchorManager.onUpdate()
            //val planeRenderer = sceneView.planeRenderer
            //planeRenderer.isEnabled = true
            //planeRenderer.isVisible = true
            //sceneView.planeRenderer.material?.setFloat(PlaneRenderer.MATERIAL_SPOTLIGHT_RADIUS,100f)

            // Frame이 생성되었을 경우 session도 생성된 상태에 있기 때문에 최초 1회 configure를 진행해 줌
            if(!isSessionConfigured){
                isLoading=true
                configureSession()
                isSessionConfigured=true
                Log.d("session",session.config.toString())
                isLoading = false
            }

        }

        // sceneView.onArSessionCreated(session) = {}


    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }


    /**
     * 기본적인 상호작용 가능한 노드를 생성하는 함수
     * 메뉴나 씨앗심기 동작등을 작성할 때 사용 가능
     * */
    private fun createSeed(anchor: Anchor) {
        //configureSession()
        isLoading = true
        modelNode = ArNode(
            modelGlbFileLocation = "models/sphere.glb",
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
        /*
        modelNode!!.onTap = { _, _ ->
            Log.v("onTap" ,"버튼 앵커 클릭")
            // 버튼이 Tap 되면 기 설치된 버튼 노드를 보이지 않게 설정하면서
            // 해당 anchor(위치)에 새로운 tree 앵커 생성
            modelNode!!.isVisible = false
            plantTreeAnchor(anchor)
        }
         */
        sceneView.addChild(modelNode!!)

        // 이곳에 심으시겠습니까?
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Position Check")
        builder.setMessage("Press continue to plant your tree here")
        builder.setPositiveButton("Continue"){
            _, _ ->
            modelNode!!.destroy()
            plantTreeAnchor(anchor)
            isSeeding = false
        }
        builder.setNegativeButton("Cancel"){
            _, _ ->
            Toast.makeText(requireContext(), "Seeding plant canceled", Toast.LENGTH_SHORT).show()
            modelNode!!.destroy()
        }
        val alertDialog:AlertDialog = builder.create()
        alertDialog.window!!.setGravity(Gravity.BOTTOM)
        alertDialog.show()
        // Todo
        // 나무이름 등록 - 수정가능하게? 나중에 하는게 좋지 않을까
        // 템창도 생각해보니 다이얼로그에서 스크롤로 처리가능
    }


    /** Cloud Anchor Mode 로 등록 가능한 Tree Anchor 를 생성하는 함수 */
    private fun plantTreeAnchor(anchor: Anchor) {
        // 세션이 Cloud Anchor를 사용할 수 있게 Configure 해줌
        //configureSession()

        modelNode = ArNode(
            //로드하는 모델의 종류는 사용자의 씨앗에 따라 서버에서 받아와야 함
            modelGlbFileLocation = "models/sample.glb",
            context = requireContext(),
            coroutineScope = lifecycleScope,
            onModelLoaded = {
                //actionButton.text = getString(R.string.host_tree)
                //actionButton.icon = resources.getDrawable(R.drawable.ic_target)
                //actionButton.icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_target,null)
                isLoading = false
            }
        )
        modelNode!!.onTap = { pickHitResult, motionEvent ->
            // Todo 이제 onTap 에 두기 보다 심으면 바로 등록되고 모델 reload 명령 보내는 게 맞음 but 아직 API 과다사용 방지목적으로 안해두겠음
            // onTap 리스너 등록
            Log.v("onTap" ,"나무 노드 클릭")

            //createCloudAnchorWithAnchor(anchor)
            //isLoading = true
            /*
            var cloudAnchor:Anchor? = null
            if(cloudAnchor?.cloudAnchorState==CloudAnchorState.SUCCESS || cloudAnchor?.cloudAnchorState==CloudAnchorState.TASK_IN_PROGRESS){
                // 이미 등록이 진행중이거나 완료되어 클라우드 재 등록을 할 피요가 없는 경우
                onHostedAnchor(cloudAnchor)
            }
            else{
                // 여기에서 Session.FeatureMapQuality 확인해서 미리 실패 확률을 줄이도록 할 것
                // 등록에 실패했을 시 또는 등록된 경우가 아닐 시 재등록 허용
                cloudAnchor = cloudAnchorManager.hostCloudAnchor(session,anchor,1,
                    object : CloudAnchorManager.CloudAnchorResultListener {
                        override fun onCloudTaskComplete(anchor: Anchor?) {
                            onHostedAnchor(anchor)
                        }
                    })
            }
            */

            //actionButton.text = getString(R.string.hosting_tree)
        }
        // 등록된 cloudAnchor 의 위치로 재설정
        // 상기 등록 구문에서 hostCloudAnchor 를 통해 anchor 의 위치가 재설정 된 바 있음
        val cloudAnchor = createCloudAnchorWithAnchor(anchor)

        modelNode!!.anchor = cloudAnchor
        sceneView.addChild(modelNode!!)

    }

    private fun createCloudAnchorWithAnchor(anchor:Anchor?): Anchor {
        var cloudAnchor: Anchor?
        if(anchor?.cloudAnchorState==CloudAnchorState.SUCCESS || anchor?.cloudAnchorState==CloudAnchorState.TASK_IN_PROGRESS){
            // 이미 등록이 진행중이거나 완료되어 클라우드 재 등록을 할 피요가 없는 경우
            onHostedAnchor(anchor)
            return anchor
        }
        else{
            // 여기에서 Session.FeatureMapQuality 확인해서 미리 실패 확률을 줄이도록 할 것
            // 등록에 실패했을 시 또는 등록된 경우가 아닐 시 재등록 허용
            cloudAnchor = cloudAnchorManager.hostCloudAnchor(session,anchor,1,
                object : CloudAnchorManager.CloudAnchorResultListener {
                    override fun onCloudTaskComplete(anchor: Anchor?) {
                        onHostedAnchor(anchor)
                    }
                })
            return cloudAnchor!!
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
            //actionButton.text = getString(R.string.resolve_tree)
            /*
                이제 클립보드로 앵커 아이디 전송할 필요성 없음
                clipboardManager = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData:ClipData = ClipData.newPlainText("label",anchor.cloudAnchorId)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(requireContext(), "클립보드로 앵커의 아이디가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                */
            if(anchorState== CloudAnchorState.SUCCESS){
                // Todo 여기 아이템아이디 고유화 하드코딩

                if(anchor.cloudAnchorId != null){
                    val postTreeDTO= PostTreeDTO(anchor.cloudAnchorId,mLastLocation!!.latitude,mLastLocation!!.longitude,"BIMOON",userId,3)
                    arViewModel.postHostedTree(postTreeDTO)
                    // 리로드 하기 전에 먼저 심어진거는 삭제
                    modelNode!!.destroy()
                    clearView()
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
    fun resolveAnchor(cloudAnchorId:String, treeId: Long, level: Int): Anchor? {
        isLoading = true
        var cloudAnchor:Anchor? =
        cloudAnchorManager.resolveCloudAnchor(
            session,
            cloudAnchorId,
            object : CloudAnchorManager.CloudAnchorResultListener{
                override fun onCloudTaskComplete(anchor: Anchor?) {
                    if(anchor!!.cloudAnchorState==CloudAnchorState.SUCCESS){
                        //Toast.makeText(requireContext(), "나무가 로드되었습니다. 설치한 공간을 비춰보세요.", Toast.LENGTH_SHORT).show()
                        //onResolvedAnchor 해서 나무 노드 생성하는거 만들기
                        onResolvedAnchor(anchor, treeId, level)
                    }
                    else{
                        Toast.makeText(requireContext(), "로드 에러 : " + anchor.cloudAnchorState.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        isLoading = false
        return cloudAnchor
    }

    /** 클라우드 앵커의 로드가 완료되었을 경우에 호출되는 함수
     * 로드된 앵커 위에 노드를 덧씌워 준다.
     * */
    @Synchronized
    private fun onResolvedAnchor(anchor:Anchor?, treeId: Long, level: Int){
        // 레벨 반영비율 + 상수로 크기 결정, 따로 value로 관리해도 좋을 것 같음
        val fLevel = 0.5f*level.toFloat()
        if(anchor!=null){
            modelNode = ArNode(
                //로드하는 모델의 종류는 서버에서 받아와야 함
                modelGlbFileLocation = "models/sample.glb",
                context = requireContext(),
                coroutineScope = lifecycleScope,
                scales = Vector3(0.5f+fLevel,0.5f+fLevel,0.5f+fLevel),
                onModelLoaded = {
                    //.text = getString(R.string.hosted_tree)
                    //actionButton.icon = resources.getDrawable(R.drawable.ic_target)
                    //actionButton.icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_target,null)

                    isLoading = false
                }
            )
            modelNode!!.onTap = { _,_ ->

                val tree = arViewModel.treeListLiveData.value?.find { it.treeId == treeId }
                Log.d("interact",tree.toString())
                arViewModel.getTreeInformation(treeId,treeId,userId)
            }
            modelNode!!.anchor = anchor
            sceneView.addChild(modelNode!!)

        }
    }


    /** Cloud Anchor를 사용하기 위해서는 Session이 먼저 Configure 되어 있어야 한다.
     * 이를 위해서 기 생성된 session!! 의 Config정보를 config에 저장하고
     * cloudAnchor 모드를 사용 설정, Configure가 초기화되기 때문에 조명 사용 모드를 미사용 설정해준다
     * 물체의 앞 뒤를 구분하는 Depth모드도 Configure에서 설정할 수 있다.
     * session.configure(config) 로 적용 */
    private fun configureSession(){
        session = sceneView.session!!
        var config = Config(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        session.configure(config)
    }


    // 위치 권한이 있는지 확인하는 메서드
    private fun setUpLocationCheck(){
        checkPermissionForLocation(requireContext())
        mLocationRequest = com.google.android.gms.location.LocationRequest.create()
        mLocationRequest.priority = LocationRequest.QUALITY_HIGH_ACCURACY
        mLocationRequest.interval = 5 * 1000
        startLocationUpdates()
    }
    fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 지리 확보(위치) 권한에 추가 런타임 권한이 필요합니다.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
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
    @SuppressLint ("missing permission")
    protected fun startLocationUpdates() {
        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mFusedLocationProviderClient!!.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                mLastLocation = location
                if(once){
                    arViewModel?.let {
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
        var distance = location.distanceTo(mLastLocation)
        // 위치 정확도 관련해서도 추가하면 좋을 듯
        // Todo 하드코딩 나무로드조건
        if (distance > 10) {
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
        fabProfile = arFragmentBinding.fabProfile
        fabSeed = arFragmentBinding.fabSeed
        fabMain.setImageResource(R.drawable.ic_ar_floating_main_open)

        fabMain.setOnClickListener{
            toggleFab()
        }
        fabProfile.setOnClickListener{
            // Todo 마이페이지 액티비티로의 연결
            /**
             * 테스트코드 주의
             * */
            arViewModel.listAroundTrees(mLastLocation!!.latitude, mLastLocation!!.longitude, userId)
            //Toast.makeText(requireContext(), "Move to MyPage.", Toast.LENGTH_SHORT).show()
        }
        fabSeed.setOnClickListener{
            // Todo 씨앗 템창으로 연결 악 씨앗 템창 언제만들어!!!
            Toast.makeText(requireContext(), "Tap plane to plant.", Toast.LENGTH_SHORT).show()
            isSeeding = true
        }
    }

    /**
     * 토글버튼 애니메이션
     * */
    private fun toggleFab(){
        // 플로팅 액션 버튼 닫기
        if(isFabOpen){
            ObjectAnimator.ofFloat(fabSeed, "translationY", 0f).apply{ start()}
            ObjectAnimator.ofFloat(fabProfile, "translationY", 0f).apply{ start()}
            fabMain.setImageResource(R.drawable.ic_ar_floating_main_open)
        }
        // 플로팅 액션 버튼 열기
        else{
            ObjectAnimator.ofFloat(fabSeed, "translationY", -200f).apply{ start()}
            ObjectAnimator.ofFloat(fabProfile, "translationY", -400f).apply{ start()}
            fabMain.setImageResource(R.drawable.ic_ar_floating_main_close)
        }
        isFabOpen = !isFabOpen
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
                    // Todo 여기 서버단 관련 하드코딩 있음
                    if(cloudAnchorID != null) {
                        resolveNotResolvedAnchor(cloudAnchorID,treeId,level)
                    }
                }
            }
        }

        arViewModel.treeInformationLiveData.observe(viewLifecycleOwner){
            treeInformation ->
            treeInformation?.let{
                createInteractDialog(it)
            }
        }

        arViewModel.showErrorToast.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })
    }

    private fun clearView(){
        // Todo 여기 최대 렌더링 수
        if(sceneView.children.size>10){
            // 최대 렌더링 수보다 Sceneview 에 자식노드가 많으면 카메라랑 커서 빼고 잘라버림
            for(child in sceneView.children){
                if(child !is Camera && child is Node){
                    if (child != cursorNode){
                        child.destroy()
                    }
                }
            }
            // 등록된 앵커 집합 청소
            /*
            for(item in resolvedTreeIdSet){
                resolvedTreeIdSet.remove(item)
            }

            for(item in resolvedTreeMap){
                resolvedTreeMap.remove(item.key)
            }

            */
            resolvedTreeMap.clear()
        }

    }

    /**
     * 전역변수인 resolvedTreeSet을 확인하여 등록되지 않은 나무의 경우만 심어 줌
     * */
    private fun resolveNotResolvedAnchor(cloudAnchorId: String,treeId: Long, level: Int){
        clearView()
        /*
        if (treeId !in resolvedTreeIdSet){
            resolveAnchor(cloudAnchorId,treeId,level)
            resolvedTreeIdSet.add(treeId)
        }
         */
        if (treeId !in resolvedTreeMap.keys){
            var cloudAnchor = resolveAnchor(cloudAnchorId,treeId,level)
            if (cloudAnchor?.cloudAnchorId != null) {
                // 이거 null 처리는 API에서 해주는거라
                resolvedTreeMap[treeId] = cloudAnchor
            }
        }
    }

    private fun createInteractDialog(treeInfo:GetTreeInformationDTO){
        // 커스텀 다이얼로그를 통한 나무 정보 획득, info 부분에는 나무 설명의 수정과 등록이 가능하도록
        val isTreeOwner = treeInfo.user.userId == userId
        Log.d("interact",treeInfo.toString())

        var oldInfo = PutTreeInfoDTO(treeInfo.bookmark,treeInfo.treeDescription,treeInfo.treeName)
        if(oldInfo.treeDescription==null){
            oldInfo.treeDescription = "There is no description"
        }
        if(oldInfo.treeName==null){
            oldInfo.treeName = "No name"
        }

        val arDialogFragment = ArDialogFragment(requireContext(),treeInfo,isTreeOwner)
        arDialogFragment.createDialog()
        arDialogFragment.setListener(object: ArDialogFragment.ArDialogListener{
            override fun onNameSaveListener(treeName: String) {
                Toast.makeText(requireContext(), "Tree name saved", Toast.LENGTH_SHORT).show()
                //Todo 북마크 끌어오기
                val treeInfoDTO = PutTreeInfoDTO(treeInfo.bookmark,oldInfo.treeDescription,treeName)
                arViewModel.putTreeInfo(treeInfo.user.userId,treeInfo.treeId,treeInfoDTO)
            }
            override fun onDescriptionSaveListener(description: String) {
                //TODO("여기 디스크립션 풋") 디스크립션 반환값 받아서 서버에 등록하기
                Toast.makeText(requireContext(), "Description saved", Toast.LENGTH_SHORT).show()
                val treeInfoDTO = PutTreeInfoDTO(treeInfo.bookmark,description,oldInfo.treeName)
                arViewModel.putTreeInfo(treeInfo.user.userId,treeInfo.treeId,treeInfoDTO)
            }

            override fun onWaterListener(treeId: Long) {
                //TODO("여기 물주기 "), 클라우드 앵커 아이디 재발급받아서 서버에 등록하기
                Toast.makeText(requireContext(), "물주기 반환값 : $treeId", Toast.LENGTH_SHORT).show()
                //refreshCloudAnchorId(treeId)
            }
        })

    }

    private fun refreshCloudAnchorId(treeId: Long){
        var oldAnchor = resolvedTreeMap[treeId]
        if(oldAnchor!=null) {
            var newCloudAnchor = createCloudAnchorWithAnchor(oldAnchor)
            var waterTreeDTO = WaterTreeDTO(treeId, newCloudAnchor.cloudAnchorId)
            Log.d("water",oldAnchor.cloudAnchorId)
            Log.d("water",waterTreeDTO.toString())
            if (newCloudAnchor.cloudAnchorId != null) {
                arViewModel.waterTree(treeId, waterTreeDTO)
            }
        }
    }

    /**
     * 아이디를 가진 Cloud Anchor를 로드하기 위한 과정이 여기 기입되어 있음
     * 추후 데이터베이스에서부터의 연결에 활용
     */
    /*
    @Synchronized
    private fun cloudAnchorLoadFromCode() {
        // 세션이 configure 될 충분한 시간을 확보하기 위해 여기서 configure
        //configureSession()
        // Custom Dialog 동적으로 생성
        val dialog = ResolveDialogFragment.setOkListener(object : ResolveDialogFragment.OkListener {
            override fun onOkPressed(dialogValue: String) {
                Toast.makeText(requireContext(), "서버에서 나무 정보를 로드합니다...", Toast.LENGTH_SHORT).show()
                //resolveAnchor(dialogValue)
            }
        })
        dialog.show(requireActivity().supportFragmentManager, "Resolve")
    }
*/
}