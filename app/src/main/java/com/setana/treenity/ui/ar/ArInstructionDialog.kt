package com.setana.treenity.ui.ar

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.ImageDecoder
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.isGone
import coil.ComponentRegistry
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
    val imageLoader = ImageLoader.Builder(context).componentRegistry {
        if (SDK_INT >= 28) {
            add(ImageDecoderDecoder())
        } else {
            add(GifDecoder())
        }
    }.build()

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
              arInstructionDialogBinding.buttonPrevious.isEnabled = false
              arInstructionDialogBinding.buttonNext.isEnabled = true
              arInstructionDialogBinding.buttonClose.isEnabled = false
              arInstructionDialogBinding.instruction.load("https://ifh.cc/g/SfNsk8.gif",imageLoader){
                  crossfade(true)
                  crossfade(500)
              }
          }
          1 -> {

              arInstructionDialogBinding.buttonPrevious.isEnabled = true
              arInstructionDialogBinding.buttonNext.isEnabled = false
              arInstructionDialogBinding.buttonClose.isEnabled = true
              arInstructionDialogBinding.instruction.load("https://ifh.cc/g/pVpD0f.gif",imageLoader){
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