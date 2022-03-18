package com.setana.treenity.ui.ar

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment

// Todo 원래 테스트용 다이얼로그였는데 개조해서 물주기용 다이얼로그로 쓸 예정
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
