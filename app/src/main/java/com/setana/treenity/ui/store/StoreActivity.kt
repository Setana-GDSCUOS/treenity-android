package com.setana.treenity.ui.store

import androidx.appcompat.app.AppCompatActivity
<<<<<<< Updated upstream:app/src/main/java/com/setana/treenity/MainActivity.kt
import android.os.Bundle

class MainActivity : AppCompatActivity() {
=======
import androidx.recyclerview.widget.LinearLayoutManager
import com.setana.treenity.ui.store.adapter.ImageAdapter
import com.setana.treenity.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageAdapter: ImageAdapter

    private val viewModel: ImageViewModel by viewModels()

>>>>>>> Stashed changes:app/src/main/java/com/setana/treenity/ui/store/StoreActivity.kt
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
<<<<<<< Updated upstream:app/src/main/java/com/setana/treenity/MainActivity.kt
=======

    private fun setupRecyclerView() {

        imageAdapter = ImageAdapter()

        binding.recyclerView.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(this@StoreActivity)
            setHasFixedSize(true)
        }

        viewModel.responseImages.observe(this, { response ->

            if(response != null) {
                imageAdapter.submitList(response)
            }
        })
    }

    // used https://unsplash.com/developers to get the api
>>>>>>> Stashed changes:app/src/main/java/com/setana/treenity/ui/store/StoreActivity.kt
}