package com.setana.treenity.ui.ar.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.setana.treenity.R
import com.setana.treenity.data.api.dto.GetUserItemResponseDTO
import com.setana.treenity.data.api.dto.store.StoreItem


class SeedAdapter(
    // 전달받은 아이템 목록
    items: List<GetUserItemResponseDTO>
) : RecyclerView.Adapter<SeedAdapter.SeedViewHolder>() {
    private var seedId:Long = 0
    var items = items



    // 각각의 아이템들에 부착될 listener, 포지션을 반환하도록 설계
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        // 아이템 누르면 포지션 알려줘서 items 로 접근하게 함
        itemClickListener = listener
    }

    private lateinit var itemClickListener: OnItemClickListener

    // 아이템들을 View 와 연결해 줄 수 있는 ViewHolder 클래스
    inner class SeedViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        init{
            itemView.setOnClickListener{
                itemClickListener.onItemClick(adapterPosition)
            }
        }
        val itemImageView: ImageView = itemView.findViewById(R.id.itemImage)
        val itemCountTextView:TextView = itemView.findViewById(R.id.itemCount)
        val itemNameTextView:TextView = itemView.findViewById(R.id.itemName)
    }

    // 아이템 레이아웃과 결합
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SeedViewHolder {
        val context: Context = parent.context // parent 로부터 context 받음
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // view 는 미리 정의한 layout 사용
        val view: View = inflater.inflate(R.layout.ar_seed_item, parent, false)
        return SeedViewHolder(view) // ViewHolder 반환
    }

    // View 에 내용 입력
    override fun onBindViewHolder(holder: SeedAdapter.SeedViewHolder, position: Int) {
        val item: GetUserItemResponseDTO = items[position] // 어떤 포지션의 텍스트인지 조회
        holder.itemCountTextView.text = item.count.toString()
        holder.itemNameTextView.text = item.itemName
        holder.itemImageView.load(item.imagePath){
            crossfade(true)
            crossfade(100)
        }
    }

    // 전달받은 아이템의 갯수
    override fun getItemCount() = items.size

}