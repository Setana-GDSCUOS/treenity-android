package com.setana.treenity.ui.ar

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.sceneform.rendering.ViewRenderable
import com.setana.treenity.R
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.CursorNode

import io.github.sceneview.utils.doOnApplyWindowInsets



class ArFragment : Fragment(R.layout.ar_fragment) {

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var actionButton: ExtendedFloatingActionButton

    lateinit var cursorNode: CursorNode
    var modelNode: ArNode? = null
    var textNode: ArNode? = null
    var isLoading = false
    set(value) {
        field = value
        loadingView.isGone = !value
        actionButton.isGone = value
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sceneView = view.findViewById(R.id.sceneView)

        sceneView.onTouchAr = { hitResult, _ ->
            createButtonNode(hitResult.createAnchor())
        }
        loadingView = view.findViewById(R.id.loadingView)
        actionButton = view.findViewById<ExtendedFloatingActionButton>(R.id.actionButton).apply {
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            //setOnClickListener { cursorNode.createAnchor()?.let { loadAnchor(it) } }
        }

        cursorNode = CursorNode(context = requireContext(), coroutineScope = lifecycleScope)
        cursorNode.onTrackingChanged = { _, isTracking ->
            if (!isLoading) {
                actionButton.isGone = !isTracking
            }
        }
        sceneView.addChild(cursorNode)
    }
    fun createButtonNode(anchor: Anchor) {
        isLoading = true
        modelNode = ArNode(
            modelGlbFileLocation = "models/sphere.glb",
            context = requireContext(),
            coroutineScope = lifecycleScope,
            onModelLoaded = {
                actionButton.text = getString(R.string.move_object)
                actionButton.icon = resources.getDrawable(R.drawable.ic_target)
                ViewRenderable.builder()
                    .setView(requireContext(),R.layout.title_button_plant)
                    .build()
                    .thenAccept { renderable->
                        textNode= ArNode(
                            viewLayoutResId = R.id.button_plant,
                            context = requireContext(),
                            coroutineScope = lifecycleScope,
                        )
                        textNode!!.positionY+=0.05f
                        textNode!!.setRenderable(renderable)
                        modelNode!!.addChild(textNode!!)
                    }
                    .exceptionally { Throwable->
                        throw AssertionError("Could not load title of the button")
                    }
                isLoading = false
            }
        )
        modelNode!!.anchor=anchor
        modelNode!!.onTap = { pickHitResult, motionEvent ->
            Log.v("onTap" ,"버튼 앵커 클릭")

            // Todo: add reaction of button, as planting trees or...
            modelNode!!.isVisible = false
            plantTreeAnchor(anchor)
        }
        sceneView.addChild(modelNode!!)
    }


    fun plantTreeAnchor(anchor: Anchor) {
        isLoading = true
        modelNode = ArNode(
            modelGlbFileLocation = "models/sample.glb",
            context = requireContext(),
            coroutineScope = lifecycleScope,
            onModelLoaded = {
                actionButton.text = getString(R.string.move_object)
                actionButton.icon = resources.getDrawable(R.drawable.ic_target)
                isLoading = false
            }
        )
        modelNode!!.onTap = { pickHitResult, motionEvent ->

            Log.v("onTap" ,"나무 노드 클릭")
            // Todo : add reaction of Tree Node


        }
        sceneView.addChild(modelNode!!)
        modelNode!!.anchor = anchor
    }



    fun loadAnchor(anchor: Anchor) {
        // Todo : load fragment for shortcode first
        if (modelNode == null) {
            isLoading = true
            modelNode = ArNode(
                modelGlbFileLocation = "models/sample.glb",
                context = requireContext(),
                coroutineScope = lifecycleScope,
                onModelLoaded = {
                    actionButton.text = getString(R.string.move_object)
                    actionButton.icon = resources.getDrawable(R.drawable.ic_target)
                    isLoading = false
                }
            )
            modelNode!!.anchor=anchor
            modelNode!!.onTap = { pickHitResult, motionEvent ->
                Log.v("onTap" ,"로그를 찍어보자")
            }
            sceneView.addChild(modelNode!!)
        }
        //else {
        //    modelNode!!.anchor = anchor
        //}
    }
}