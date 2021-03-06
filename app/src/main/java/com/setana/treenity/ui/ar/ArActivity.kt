package com.setana.treenity.ui.ar

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.commit
import com.setana.treenity.R
import com.setana.treenity.databinding.ActivityArBinding
import dagger.hilt.android.AndroidEntryPoint

/** Fragment 의 우월한(?) 생명주기 + 카메라의 원활한 사용을 위해 Activity 에서는 바로 Fragment 띄움 */

@AndroidEntryPoint
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