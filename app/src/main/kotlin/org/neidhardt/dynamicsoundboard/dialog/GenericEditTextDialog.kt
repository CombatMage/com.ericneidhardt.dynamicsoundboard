package org.neidhardt.dynamicsoundboard.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.widget.EditText
import kotlinx.android.synthetic.main.dialog_genericrename.view.*
import org.neidhardt.android_utils.views.showKeyboard
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.base.BaseDialog
import com.jakewharton.rxbinding.widget.RxTextView

/**
 * Created by eric.neidhardt@gmail.com on 05.01.2017.
 */
open class GenericEditTextDialog : BaseDialog() {

	private var positiveButton: ButtonConfig<GenericEditTextDialog>? = null
	private var negativeButton: ButtonConfig<GenericEditTextDialog>? = null
	private var dialogConfig: DialogConfig? = null
	private var editTextConfig: EditTextConfig? = null

	companion object {
		fun showInstance(fragmentManager: FragmentManager,
						 fragmentTag: String,
						 positiveButton: ButtonConfig<GenericEditTextDialog>,
						 negativeButton: ButtonConfig<GenericEditTextDialog>,
						 dialogConfig: DialogConfig,
						 editTextConfig: EditTextConfig) {

			val dialog = GenericEditTextDialog().apply {
				this.positiveButton = positiveButton
				this.negativeButton = negativeButton
				this.dialogConfig = dialogConfig
				this.editTextConfig = editTextConfig
			}
			dialog.show(fragmentManager, fragmentTag)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.retainInstance = true
	}

	private var editText: EditText? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val context = this.activity

		@SuppressLint("InflateParams") val view = context.layoutInflater.inflate(R.layout.dialog_genericrename, null)

		val editText = view.edittext_genericrename_inputfield
		editText.setText(this.editTextConfig?.text)
		this.editText = editText

		val hint = view.textinputlayout_genericrename_hint
		hint.hint = this.editTextConfig?.hint

		val dialogBuilder = AlertDialog.Builder(context)

		this.dialogConfig?.let { config ->
			if (config.titleId != 0)
				dialogBuilder.setTitle(config.titleId)
			if (config.messageId != 0)
				dialogBuilder.setMessage(config.messageId)
		}
		dialogBuilder.setView(view)

		dialogBuilder.setPositiveButton(this.positiveButton?.labelId ?: 0, { dialogInterface,i ->
			this.positiveButton?.action?.invoke(this, editText.text.toString())
		})
		dialogBuilder.setNegativeButton(this.negativeButton?.labelId ?: 0, { dialogInterface,i ->
			this.negativeButton?.action?.invoke(this, editText.text.toString())
			this.dismiss()
		})

		val dialog = dialogBuilder.create()

		val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

		// empty label is not allowed
		//this.subscriptions.add(
		//		RxTextView.afterTextChangeEvents(editText)
		//				.subscribe { label -> positiveButton.isEnabled = label.toString().isNotBlank() }
		//)

		return dialog
	}

	override fun onResume() {
		super.onResume()
		this.editText?.showKeyboard()
	}

}

class EditTextConfig(val hint: String, val text: String)

class DialogConfig(val titleId: Int, val messageId: Int)

class ButtonConfig<in T : GenericEditTextDialog>(val labelId: Int, val action: (T, String) -> Unit)