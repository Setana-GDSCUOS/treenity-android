package com.setana.treenity.ui.ar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState
import com.google.ar.core.Config
import com.google.ar.sceneform.rendering.ViewRenderable
import com.setana.treenity.R
import com.setana.treenity.databinding.ArFragmentBinding
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.CursorNode
import io.github.sceneview.utils.doOnApplyWindowInsets

/** Ar화면의 MainView를 담당하는 Fragment
 * SceneView 부연설명 포함
 * Anchor는 3D공간상의 위치정보
 * ArNode는 모델을 포함하는 3D 공간상의 개채로 앵커를 포함하면 해당 위치에 표시됨
 * */
class ArFragment : Fragment(R.layout.ar_fragment) {
    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var actionButton: ExtendedFloatingActionButton
    lateinit var arFragmentBinding: ArFragmentBinding

    lateinit var cursorNode: CursorNode
    lateinit var session: ArSession

    //테스트용
    lateinit var clipboardManager: ClipboardManager

    var cloudAnchorManager = CloudAnchorManager()
    var modelNode: ArNode? = null
    var textNode: ArNode? = null

    /** 로딩뷰 + 액션 버튼 상호작용 활성/비활성 조작용 */
    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
            actionButton.isGone = value
        }

    /** 메인을 담당할 부분이다. Fragment 전체를 담당 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        // fragment의 lifecycle에 의한 메모리 누수 방지를 위해 inflate 말고 bind 사용
        arFragmentBinding = ArFragmentBinding.bind(view)
        sceneView = arFragmentBinding.sceneView
        // 뷰의 터치된 장소에 노드를 생성 hitResult가 터치된 장소.
        sceneView.onTouchAr = { hitResult, _ ->
            createButtonNode(hitResult.createAnchor())
        }
        loadingView = arFragmentBinding.loadingView
        actionButton =arFragmentBinding.actionButton.apply {
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            setOnClickListener{
                onResolveButtonPressed()
            }
        }
        // 지면에 뜨는 CursorNode 생성하여 객제에 할당
        cursorNode = CursorNode(context = requireContext(), coroutineScope = lifecycleScope)
        cursorNode.onTrackingChanged = { _, isTracking ->
            if (!isLoading) {
                actionButton.isGone = !isTracking
            }
        }
        sceneView.addChild(cursorNode)
        sceneView.onArFrameUpdated = {
            // CloudAnchorManager의 onUpdate는 Frame이 업데이트되어 Session이 갱신될 때 작동하도록 설계되어 있음.
            cloudAnchorManager.onUpdate()
        }
        // sceneView.onArSessionCreated(session) = {}

    }

    /**
     * 기본적인 상호작용 가능한 노드를 생성하는 함수
     * 메뉴나 씨앗심기 동작등을 작성할 때 사용 가능
     * */
    fun createButtonNode(anchor: Anchor) {
        isLoading = true
        modelNode = ArNode(
            modelGlbFileLocation = "models/sphere.glb",
            context = requireContext(),
            coroutineScope = lifecycleScope,
            onModelLoaded = {
                actionButton.text = getString(R.string.hosted_tree)
                //actionButton.icon = resources.getDrawable(R.drawable.ic_target)
                actionButton.icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_target,null)
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
        modelNode!!.onTap = { _, _ ->
            Log.v("onTap" ,"버튼 앵커 클릭")
            // 버튼이 Tap 되면 기 설치된 버튼 노드를 보이지 않게 설정하면서
            // 해당 anchor(위치)에 새로운 tree 앵커 생성
            modelNode!!.isVisible = false
            plantTreeAnchor(anchor)
        }
        sceneView.addChild(modelNode!!)
    }

    /** Cloud Anchor Mode 로 등록 가능한 Tree Anchor 를 생성하는 함수 */
    fun plantTreeAnchor(anchor: Anchor) {
        // 세션이 Cloud Anchor를 사용할 수 있게 Configure 해줌
        configureSession()
        var cloudAnchor:Anchor? = null
        modelNode = ArNode(
            //로드하는 모델의 종류는 사용자의 씨앗에 따라 서버에서 받아와야 함
            modelGlbFileLocation = "models/sample.glb",
            context = requireContext(),
            coroutineScope = lifecycleScope,
            onModelLoaded = {
                actionButton.text = getString(R.string.host_tree)
                //actionButton.icon = resources.getDrawable(R.drawable.ic_target)
                actionButton.icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_target,null)
                isLoading = false
            }
        )
        modelNode!!.onTap = { pickHitResult, motionEvent ->
            // onTap 리스너 등록
            Log.v("onTap" ,"나무 노드 클릭")
            //isLoading = true
            if(cloudAnchor?.cloudAnchorState==CloudAnchorState.SUCCESS && cloudAnchor?.cloudAnchorState==CloudAnchorState.TASK_IN_PROGRESS){
                // 이미 등록이 진행중이거나 완료되어 클라우드 재 등록을 할 피요가 없는 경우
                onHostedAnchor(cloudAnchor)
            }
            else{
                // 등록에 실패했을 시 또는 등록된 경우가 아닐 시 재등록 허용
                cloudAnchor = cloudAnchorManager.hostCloudAnchor(session,anchor,1,
                    object : CloudAnchorManager.CloudAnchorResultListener {
                        override fun onCloudTaskComplete(anchor: Anchor?) {
                            onHostedAnchor(anchor)
                        }
                    })
            }
            actionButton.text = getString(R.string.hosting_tree)
        }
        // 등록된 cloudAnchor 의 위치로 재설정
        // 상기 등록 구문에서 hostCloudAnchor 를 통해 anchor 의 위치가 재설정 된 바 있음
        modelNode!!.anchor = anchor
        sceneView.addChild(modelNode!!)
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
            actionButton.text = getString(R.string.resolve_tree)
            if(anchorState== CloudAnchorState.SUCCESS){
                clipboardManager = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData:ClipData = ClipData.newPlainText("label",anchor.cloudAnchorId)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(requireContext(), "클립보드로 앵커의 아이디가 전송되었습니다.", Toast.LENGTH_SHORT).show()
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
    fun resolveAnchor(cloudAnchorId:String) {
        isLoading = true
        cloudAnchorManager.resolveCloudAnchor(
            session,
            cloudAnchorId,
            object : CloudAnchorManager.CloudAnchorResultListener{
                override fun onCloudTaskComplete(anchor: Anchor?) {
                    if(anchor!!.cloudAnchorState==CloudAnchorState.SUCCESS){
                        Toast.makeText(requireContext(), "나무가 로드되었습니다. 설치한 공간을 비춰보세요.", Toast.LENGTH_SHORT).show()
                        //onResolvedAnchor 해서 나무 노드 생성하는거 만들기
                        onResolvedAnchor(anchor)
                    }
                    else{
                        Toast.makeText(requireContext(), "로드 에러 : " + anchor.cloudAnchorState.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        isLoading = false
    }

    /** 클라우드 앵커의 로드가 완료되었을 경우에 호출되는 함수
     * 로드된 앵커 위에 노드를 덧씌워 준다.
     * */
    @Synchronized
    private fun onResolvedAnchor(anchor:Anchor?){
        if(anchor!=null){
            modelNode = ArNode(
                //로드하는 모델의 종류는 서버에서 받아와야 함
                modelGlbFileLocation = "models/sample.glb",
                context = requireContext(),
                coroutineScope = lifecycleScope,
                onModelLoaded = {
                    actionButton.text = getString(R.string.hosted_tree)
                    //actionButton.icon = resources.getDrawable(R.drawable.ic_target)
                    actionButton.icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_target,null)

                    isLoading = false
                }
            )
            modelNode!!.onTap = { _,_ ->
                Toast.makeText(requireContext(), "로드된 나무도 상호작용 가능", Toast.LENGTH_SHORT).show()
            }
            modelNode!!.anchor = anchor
            sceneView.addChild(modelNode!!)
        }
    }

    @Synchronized
    private fun onResolveButtonPressed() {
        // 세션이 configure 될 충분한 시간을 확보하기 위해 여기서 configure
        configureSession()
        // Custom Dialog 동적으로 생성
        val dialog = ResolveDialogFragment.setOkListener(object : ResolveDialogFragment.OkListener {
            override fun onOkPressed(dialogValue: String) {
                Toast.makeText(requireContext(), "서버에서 나무 정보를 로드합니다...", Toast.LENGTH_SHORT).show()
                resolveAnchor(dialogValue)
            }
        })
        dialog.show(requireActivity().supportFragmentManager, "Resolve")
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
        config.lightEstimationMode = Config.LightEstimationMode.DISABLED
        session.configure(config)
    }
}