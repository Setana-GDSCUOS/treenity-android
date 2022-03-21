package com.setana.treenity.ui.mypage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeResponse
import com.setana.treenity.databinding.MypageTreelistRowBinding

class TreeListAdapter: RecyclerView.Adapter<TreeListAdapter.MyViewHolder>() {

    private lateinit var binding: MypageTreelistRowBinding

    private var treeListItemList: MyTreeResponse? = null

    fun setDataList(treeListItemList: MyTreeResponse?) {
        this.treeListItemList = treeListItemList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TreeListAdapter.MyViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        binding = MypageTreelistRowBinding.inflate(inflater) // xml 에 씌여져 있는 view 의 정의를 실제 view 객체로 만듦

        return MyViewHolder( // ViewBinding 을 이용하기 위해 view 가 들어가지 않고 view 객체를 넣음
            binding
        )
    }

    override fun onBindViewHolder(holder: TreeListAdapter.MyViewHolder, position: Int) {
        holder.bind(treeListItemList?.get(position)!!)
    }

    override fun getItemCount(): Int {
        return if (treeListItemList == null) 0
        else treeListItemList?.size!!
    }

    inner class MyViewHolder(itemView: MypageTreelistRowBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        // 일반적으로 onBindViewHolder가 아닌 Recycler Holder 클래스 내부에 별도의 bind 함수를 구현하여 바인딩을 진행합니다! 한번 알아보시면 도움이 될 것 같아요! --> resolved!
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