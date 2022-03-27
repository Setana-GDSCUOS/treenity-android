package com.setana.treenity.ui.ar

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.setana.treenity.data.api.dto.GetUserItemResponseDTO
import com.setana.treenity.databinding.ArSeedDialogBinding
import com.setana.treenity.ui.ar.adapter.SeedAdapter


class ArSeedDialog(context: Context, items:List<GetUserItemResponseDTO>) {
    interface ArSeedDialogListener {
        fun onItemClickListener(userItemId:Long,itemId:Long)
    }
    private val context = context
    private val dialog = Dialog(context)
    private val arSeedDialogBinding:ArSeedDialogBinding =  ArSeedDialogBinding.inflate(
        LayoutInflater.from(context))
    private var seedAdapter: SeedAdapter = SeedAdapter(items)
    private lateinit var dialogListener: ArSeedDialogListener
    private val items = items

    fun setListener(listener: ArSeedDialogListener) {
        dialogListener = listener
    }

    fun createDialog() {
        //아 근데 크기 왜 안돼

        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog!!.setContentView(arSeedDialogBinding.root)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if(seedAdapter.itemCount == 0){
            arSeedDialogBinding.title.text = "No Items"
        }
        else {
            // 이벤트 등록, 아이템 누르면 아이템아이디를 ArFragment 에 전달하기 위한 리스너 세팅
            // 리스너의 리스너라뇨 이게 뭐야
            initRecyclerView()
            // 씨앗중에 있는것만 띄움
            seedAdapter.items = items.filter { it.itemType == "SEED" && it.count!=0 }
            seedAdapter.setOnItemClickListener(object: SeedAdapter.OnItemClickListener{
                override fun onItemClick(position:Int) {
                    dialogListener.onItemClickListener(seedAdapter.items[position].userItemId,seedAdapter.items[position].itemId)
                    Log.d("bimoon","selectedItemID : ${seedAdapter.items[position].userItemId}")
                    dialog.dismiss()
                }
            })
        }

        dialog!!.show()
    }

    // adapter 부착
    private fun initRecyclerView() {
        // init adapter
        val item = GetUserItemResponseDTO.EMPTY
        seedAdapter = SeedAdapter(listOf(item))
        arSeedDialogBinding.seedRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        arSeedDialogBinding.seedRecycler.adapter = seedAdapter
    }
}