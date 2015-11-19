package org.neidhardt.dynamicsoundboard.soundsheetmanagement.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.FragmentManager
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.view.View
import android.widget.EditText
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.views.BaseDialog
import org.neidhardt.dynamicsoundboard.views.DialogBaseLayout

class AddNewSoundSheetDialog : BaseDialog(), View.OnClickListener
{
	private var soundSheetName: EditText? = null
	private var suggestedName: String? = null

	override fun onCreate(savedInstanceState: Bundle?)
    {
		super.onCreate(savedInstanceState)

		val args = this.arguments
		if (args != null)
			this.suggestedName = args.getString(KEY_SUGGESTED_NAME)
	}

	override fun onCreateDialog(savedInstanceState: Bundle): Dialog
    {
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_add_new_sound_sheet, null)

		this.mainView = view as DialogBaseLayout

		this.soundSheetName = view.findViewById(R.id.et_name_new_sound_sheet) as EditText

		view.findViewById(R.id.b_cancel).setOnClickListener(this)
		view.findViewById(R.id.b_ok).setOnClickListener(this)

		val dialog = AppCompatDialog(this.activity, R.style.DialogTheme)

		dialog.setContentView(view)
		dialog.setTitle(R.string.dialog_add_new_sound_sheet_title)

		return dialog
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.b_cancel -> this.dismiss()
			R.id.b_ok -> {
				this.deliverResult()
				this.dismiss()
			}
		}
	}

	private fun deliverResult()
    {
		var label = this.soundSheetName!!.text.toString()
		if (label.length == 0)
			label = this.suggestedName ?: ""

		val soundSheet = this.soundSheetsDataUtil.getNewSoundSheet(label)
		soundSheet.isSelected = true
		this.soundSheetsDataStorage.addSoundSheetToManager(soundSheet)
	}

	companion object
    {
		private val TAG = AddNewSoundSheetDialog::class.java.name

		private val KEY_SUGGESTED_NAME = "org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.AddNewSoundSheetDialog.suggestedName"

		fun showInstance(manager: FragmentManager, suggestedName: String) {
			val dialog = AddNewSoundSheetDialog()

			val args = Bundle()
			args.putString(KEY_SUGGESTED_NAME, suggestedName)
			dialog.arguments = args

			dialog.show(manager, TAG)
		}
	}

}
