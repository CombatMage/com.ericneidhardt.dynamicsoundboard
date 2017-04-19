package org.neidhardt.dynamicsoundboard.dialog.generic

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import org.neidhardt.dynamicsoundboard.base.BaseDialog

/**
 * Created by eric.neidhardt@gmail.com on 09.01.2017.
 */
open class GenericConfirmDialog : BaseDialog() {

	private var positiveButton: ButtonConfig<GenericConfirmDialog>? = null
	private var negativeButton: ButtonConfig<GenericConfirmDialog>? = null
	private var dialogConfig: DialogConfig? = null

	companion object {
		fun showInstance(fragmentManager: FragmentManager,
						 fragmentTag: String,
						 positiveButton: ButtonConfig<GenericConfirmDialog>,
						 negativeButton: ButtonConfig<GenericConfirmDialog>,
						 dialogConfig: DialogConfig) {

			val dialog = GenericConfirmDialog().apply {
				this.positiveButton = positiveButton
				this.negativeButton = negativeButton
				this.dialogConfig = dialogConfig
			}
			dialog.show(fragmentManager, fragmentTag)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.retainInstance = true
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val context = this.activity
		val dialogBuilder = AlertDialog.Builder(context)

		this.dialogConfig?.let { config ->
			if (config.titleId != 0)
				dialogBuilder.setTitle(config.titleId)
			if (config.messageId != 0)
				dialogBuilder.setMessage(config.messageId)
		}
		dialogBuilder.setPositiveButton(this.positiveButton?.labelId ?: 0, { _, _ ->
			this.positiveButton?.action?.invoke(this)
		})
		dialogBuilder.setNegativeButton(this.negativeButton?.labelId ?: 0, { _, _ ->
			this.negativeButton?.action?.invoke(this)
			this.dismiss()
		})

		return dialogBuilder.create()
	}

	class DialogConfig(val titleId: Int, val messageId: Int)

	class ButtonConfig<in T : GenericConfirmDialog>(val labelId: Int, val action: (T) -> Unit)
}