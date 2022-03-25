package com.setana.treenity.ui.ar

import android.app.Dialog
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.setana.treenity.databinding.ArInstructionDialogBinding

class ArInstructionDialog(context: Context) {
    interface ArInstructionDialogListener {
        fun onButtonClickListener()
    }
    private val context = context
    private val dialog = Dialog(context)
    private var arInstructionDialogBinding: ArInstructionDialogBinding
    = ArInstructionDialogBinding.inflate(LayoutInflater.from(context))
    private var page = 0
    private lateinit var dialogListener : ArInstructionDialogListener
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    fun createInstruction(){


        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog!!.setContentView(arInstructionDialogBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        arInstructionDialogBinding.buttonClose.setOnClickListener {
            dialogListener.onButtonClickListener()
            dialog.dismiss()
        }
        arInstructionDialogBinding.buttonNext.setOnClickListener {
            page ++
            page%=2
            loadPage(page)
        }
        arInstructionDialogBinding.buttonPrevious.setOnClickListener {
            page--
            //pageMax
            if(page < 0)page=1
            loadPage(page)
        }
        loadPage(0)
        dialog.show()
    }
    private fun loadPage(page:Int){
        when(page){
          0 -> {
              Log.d("bimoon","p1")
              
              arInstructionDialogBinding.instruction.load("https://ifh.cc/g/SfNsk8.gif"){
                  crossfade(true)
                  crossfade(500)
              }
          }
          1 -> {
              Log.d("bimoon","p2")
              arInstructionDialogBinding.instruction.load("https://ifh.cc/g/pVpD0f.gif"){
                  crossfade(true)
                  crossfade(500)
              }
          }
          else -> {
              Toast.makeText(context, "error : no page", Toast.LENGTH_SHORT).show()
          }
        }
    }
    fun setButtonListener(listener: ArInstructionDialogListener){
        dialogListener = listener
    }
}