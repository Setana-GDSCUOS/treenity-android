//package com.setana.treenity.ui.store.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.AsyncListDiffer
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.RecyclerView
//import coil.load
//import com.setana.treenity.data.model.ImageItem
//import com.setana.treenity.databinding.ImageLayoutBinding
//
//class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>(){
//
//    inner class ImageViewHolder(
//        val binding: ImageLayoutBinding
//    ) :
//            RecyclerView.ViewHolder(binding.root)
//
//    // 지금 보니까 bind 함수가 빠져있는데 일반적으로 onBindViewHolder가 아닌 Recycler Holder 클래스 내부에 별도의 bind 함수를 구현하여 바인딩을 진행합니다! 한번 알아보시면 도움이 될 것 같아요!
//
//    private val diffCallBack = object : DiffUtil.ItemCallback<ImageItem>() {
//        override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
//            return oldItem == newItem
//        }
//    }
//
//    private val differ = AsyncListDiffer(this, diffCallBack)
//
//    fun submitList(list: List<ImageItem>) = differ.submitList(list)
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
//        return ImageViewHolder(
//            ImageLayoutBinding.inflate(
//                LayoutInflater.from(parent.context), parent, false
//            )
//        )
//    }
//
//    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
//        val currImage = differ.currentList[position]
//
//        holder.binding.apply {
//            tvDescription.text = currImage.description
//
//            val imageLink = currImage.urls.regular
//            imageView.load(imageLink) {
//                crossfade(true)
//                crossfade(1000)
//            }
//        }
//    }
//
//    override fun getItemCount() = differ.currentList.size
//
//}