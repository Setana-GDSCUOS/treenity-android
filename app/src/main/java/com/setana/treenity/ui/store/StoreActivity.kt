package com.setana.treenity.ui.store

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.setana.treenity.databinding.ActivityMainBinding
import com.setana.treenity.ui.store.adapter.ImageAdapter

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageAdapter: ImageAdapter

    private val viewModel: ImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        imageAdapter = ImageAdapter()

        binding.recyclerView.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(this@StoreActivity)
            setHasFixedSize(true)
        }

        viewModel.responseImages.observe(this, { response ->

            response?.let {
                imageAdapter.submitList(it)
            }
        })
    }

    // used https://unsplash.com/developers to get the api
}