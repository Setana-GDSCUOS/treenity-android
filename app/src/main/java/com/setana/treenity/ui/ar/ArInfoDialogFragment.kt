package com.setana.treenity.ui.ar


import android.app.Dialog
import android.content.Context
import android.view.*
import android.widget.Toast
import androidx.core.view.isGone
import coil.load
import com.setana.treenity.R
import com.setana.treenity.data.api.dto.GetTreeInformationResponseDTO
import com.setana.treenity.databinding.ArInfoDialogBinding



class ArInfoDialogFragment(context: Context, treeInfoResponse: GetTreeInformationResponseDTO, isTreeOwner:Boolean) {
    interface ArDialogListener {
        fun onWaterListener(treeId:Long)
        fun onDescriptionSaveListener(treeName:String, description:String)
        fun onNameSaveListener(treeName:String, description:String)
    }
    private val context = context
    private val dialog = Dialog(context)
    private val isTreeOwner = isTreeOwner
    private val treeInfo = treeInfoResponse
    private val arInfoDialogBinding: ArInfoDialogBinding =
        ArInfoDialogBinding.inflate(LayoutInflater.from(context))
    private lateinit var dialogListener: ArDialogListener

    fun setListener(listener: ArDialogListener) {
        dialogListener = listener
    }


    fun createDialog() {
        //아 근데 크기 왜 안돼

        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog!!.setContentView(arInfoDialogBinding.root)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        arInfoDialogBinding.exit.setOnClickListener {
            dialog.dismiss()
        }

        if(isTreeOwner){
            arInfoDialogBinding.description.isEnabled = true
            arInfoDialogBinding.treeName.isEnabled = true
            arInfoDialogBinding.submitDescription.setOnClickListener {
                var newDescription = arInfoDialogBinding.description.text.toString()
                if(newDescription == null){
                    Toast.makeText(context, "Please enter your tree description", Toast.LENGTH_SHORT).show()
                }
                else{
                    dialogListener.onDescriptionSaveListener(arInfoDialogBinding.treeName.text.toString(),newDescription)
                }

            }
            arInfoDialogBinding.submitName.setOnClickListener {
                var newName = arInfoDialogBinding.treeName.text.toString()
                if (newName == null){
                    Toast.makeText(context, "Please enter your tree name", Toast.LENGTH_SHORT).show()
                }
                else{
                    dialogListener.onNameSaveListener(newName,arInfoDialogBinding.description.text.toString())
                }
            }
        }


        else{
            arInfoDialogBinding.submitDescription.isGone = true
            arInfoDialogBinding.submitName.isGone = true
            arInfoDialogBinding.description.background = null
            arInfoDialogBinding.description.isEnabled = false
            arInfoDialogBinding.description.setTextColor(context.getColor(R.color.black))
            arInfoDialogBinding.treeName.background = null
            arInfoDialogBinding.treeName.isEnabled = false
            arInfoDialogBinding.treeName.setTextColor(context.getColor(R.color.black))
        }
        val plantDate = treeInfo.createdDate.slice(IntRange(0,9))
        arInfoDialogBinding.treeImage.load(treeInfo.item.imagePath)
        arInfoDialogBinding.created.text = "Planted : ${plantDate}"
        arInfoDialogBinding.water.isEnabled = false

        arInfoDialogBinding.buttonLayout.setOnClickListener {
            if (isTreeOwner) {
                // Todo 뭔가 반환해서 Fragment 차원에서 viewModel 시키게 만들어야 할 것 같아
                dialogListener.onWaterListener(treeInfo.treeId)
            } else {
                Toast.makeText(context, "It's not your tree", Toast.LENGTH_SHORT).show()
            }
        }
        if(treeInfo.treeDescription != null){
            arInfoDialogBinding.description.setText(treeInfo.treeDescription)
        }
        // itemId가 k인 경우 n 레벨에서 n*(k-1) 개의 양동이 필요 : (k-1)*(n*(n+1))/2
        val footPrints = 3000*(treeInfo.item.itemId.toInt()-1)*((treeInfo.level - 1)*(treeInfo.level)/2)
        arInfoDialogBinding.effort.text = "Footprints : ${footPrints}"
        arInfoDialogBinding.owner.text = "Owner : ${treeInfo.user.username}"
        arInfoDialogBinding.treeName.setText("${treeInfo.treeName}")

        dialog!!.show()
    }
}



// 원래 테스트용 다이얼로그였는데 개조해서 물주기용 다이얼로그로
/*
class ResolveDialogFragment : DialogFragment() {
    interface OkListener {

        fun onOkPressed(dialogValue: String)
    }

    private var codeField: EditText? = null
    private var okListener: OkListener? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder
            .setView(createDialogLayout())
            .setTitle("Resolve Anchor")
            .setPositiveButton(
                "Resolve"
            ) { dialog: DialogInterface?, which: Int -> onResolvePressed() }
            .setNegativeButton(
                "Cancel"
            ) { dialog: DialogInterface?, which: Int -> }
        return builder.create()
    }

    private fun createDialogLayout(): LinearLayout {
        val context = context
        val layout = LinearLayout(context)
        codeField = EditText(context)
        // Only allow numeric input.
        codeField!!.inputType = InputType.TYPE_CLASS_TEXT
        codeField!!.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        // Set a max length for the input text to avoid overflows when parsing.
        codeField!!.filters = arrayOf<InputFilter>(LengthFilter(MAX_FIELD_LENGTH))
        layout.addView(codeField)
        layout.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        return layout
    }

    private fun onResolvePressed() {
        val roomCodeText = codeField!!.text
        if (okListener != null && roomCodeText != null && roomCodeText.length > 0) {
            val longVal = roomCodeText.toString()
            okListener!!.onOkPressed(longVal)
        }
    }

    companion object {
        // The maximum number of characters that can be entered in the EditText.
        private const val MAX_FIELD_LENGTH = 50
        fun setOkListener(listener: OkListener): ResolveDialogFragment {
            val frag = ResolveDialogFragment()
            frag.okListener = listener
            return frag
        }
    }
}
*/