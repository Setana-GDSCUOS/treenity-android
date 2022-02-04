package com.setana.treenity.ui.ar

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import com.setana.treenity.R
import com.setana.treenity.databinding.ActivityArBinding
import io.github.sceneview.utils.doOnApplyWindowInsets
import io.github.sceneview.utils.setFullScreen

class ArActivity : AppCompatActivity(R.layout.activity_ar) {

    private lateinit var activityArBinding: ActivityArBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityArBinding = ActivityArBinding.inflate(layoutInflater)
        setContentView(activityArBinding.root)
        supportFragmentManager.commit {
            add(R.id.containerFragment, ArFragment::class.java, Bundle())
        }
    }
}