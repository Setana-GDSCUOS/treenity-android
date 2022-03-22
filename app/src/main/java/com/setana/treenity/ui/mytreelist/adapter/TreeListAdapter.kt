package com.setana.treenity.ui.mytreelist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.databinding.MypageTreelistRowBinding

class TreeListAdapter: RecyclerView.Adapter<TreeListAdapter.MyViewHolder>() {

    private lateinit var binding: MypageTreelistRowBinding

    private var treeListItemList: List<MyTreeItem>? = null

    fun setDataList(treeListItemList: List<MyTreeItem>?) {
        this.treeListItemList = treeListItemList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        binding = MypageTreelistRowBinding.inflate(inflater) // xml 에 씌여져 있는 view 의 정의를 실제 view 객체로 만듦

        return MyViewHolder( // ViewBinding 을 이용하기 위해 view 가 들어가지 않고 view 객체를 넣음
            binding
        )
    }

    private val diffCallback = object : DiffUtil.ItemCallback<MyTreeItem>() { // 달라지는 부분만 갱신할 수 있도록 recyclerview 최적화하는 코드
        override fun areItemsTheSame(oldItem: MyTreeItem, newItem: MyTreeItem): Boolean {
            return oldItem.treeId == newItem.treeId
        }

        override fun areContentsTheSame(oldItem: MyTreeItem, newItem: MyTreeItem): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback) // 달라지는 부분만 갱신할 수 있도록 recyclerview 최적화하는 코드
    var trees: List<MyTreeItem>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val myTreeItem = trees[position]

        holder.apply {
            bind(myTreeItem)
        }
    }

    override fun getItemCount() = trees.size

    inner class MyViewHolder(itemView: MypageTreelistRowBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        fun bind(tree: MyTreeItem) {
            binding.itemName.text = tree.treeName
            ("Planted Date : " + tree.createdDate).also { binding.plantedDate.text = it.substring(0,25) }
            ("LEV : " + tree.level.toString()).also { binding.level.text = it } // assignment 방식으로 concat 하는 것을 권장(ide)
            ("EXP : " + tree.bucket.toString()).also { binding.exp.text = it }

            // coil 이미지 로더 사용
            binding.imagePath.load(tree.item.imagePath) {
                crossfade(true)
                crossfade(1000)
            }
        }
    }
}