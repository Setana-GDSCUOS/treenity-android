package com.setana.treenity.ui.mypage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.databinding.MypageMytreeItemRowBinding

class MyTreeAdapter(myTrees: List<MyTreeItem>) :
    RecyclerView.Adapter<MyTreeAdapter.MyTreeViewHolder>() {

    private val myTrees: List<MyTreeItem>
    private lateinit var mListener: OnItemClickListener

    init {
        this.myTrees = myTrees
    }

    inner class MyTreeViewHolder
    constructor(
        val binding: MypageMytreeItemRowBinding, listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init { // add event on each item of recyclerview
            binding.card.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

        fun bind(myTreeItem: MyTreeItem) { // Items in the recyclerview have pictures of trees and names written on them
            binding.treeName.text = myTreeItem.treeName
            binding.treeImage.load(myTreeItem.item.imagePath)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    private val diffCallback =
        object :
            DiffUtil.ItemCallback<MyTreeItem>() { // Code to optimize recyclerview so that only the parts that change can be updated
            override fun areItemsTheSame(oldItem: MyTreeItem, newItem: MyTreeItem): Boolean {
                return oldItem.treeId == newItem.treeId
            }

            override fun areContentsTheSame(oldItem: MyTreeItem, newItem: MyTreeItem): Boolean {
                return oldItem == newItem
            }
        }

    private val differ =
        AsyncListDiffer(this, diffCallback)
    var trees: List<MyTreeItem>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTreeViewHolder {
        return MyTreeViewHolder(
            MypageMytreeItemRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            mListener
        )
    }

    override fun onBindViewHolder(holder: MyTreeViewHolder, position: Int) {
        val myTreeItem = trees[position]

        holder.apply {
            bind(myTreeItem)
        }
    }

    override fun getItemCount() = trees.size
}