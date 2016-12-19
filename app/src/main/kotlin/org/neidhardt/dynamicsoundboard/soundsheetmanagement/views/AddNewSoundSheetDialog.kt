package org.neidhardt.dynamicsoundboard.soundsheetmanagement.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.widget.EditText
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseDialog
import org.neidhardt.dynamicsoundboard.manager.NewSoundSheetManager
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet

class AddNewSoundSheetDialog : BaseDialog() {

	private val manager = SoundboardApplication.newSoundSheetManager

	private var soundSheetName: EditText? = null
	private var suggestedName: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.suggestedName = this.manager.suggestedName
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val context = this.activity

		@SuppressLint("InflateParams") val view = context.layoutInflater.inflate(R.layout.dialog_add_new_sound_sheet, null)

		this.soundSheetName = view.findViewById(R.id.et_name_new_sound_sheet) as EditText

		return AlertDialog.Builder(context).apply {
			this.setTitle(R.string.dialog_add_new_sound_sheet_title)
			this.setView(view)
			this.setPositiveButton(R.string.dialog_add, { dialogInterface, i ->
				deliverResult()
				dismiss()
			})
			this.setNegativeButton(R.string.dialog_cancel, { dialogInterface, i -> dismiss()})
		}.create()
	}

	private fun deliverResult()
    {
		var label = this.soundSheetName!!.text.toString()
		if (label.isEmpty())
			label = this.suggestedName ?: ""

		val soundSheet = NewSoundSheet().apply {
			this.label = label
			this.fragmentTag = NewSoundSheetManager.getNewFragmentTagForLabel(label)
		}
		this.manager.add(soundSheet)
		this.manager.setSelected(soundSheet)
	}

	companion object
    {
		private val TAG = AddNewSoundSheetDialog::class.java.name

		fun showInstance(manager: FragmentManager) {
			val dialog = AddNewSoundSheetDialog()
			dialog.show(manager, TAG)
		}
	}

}
