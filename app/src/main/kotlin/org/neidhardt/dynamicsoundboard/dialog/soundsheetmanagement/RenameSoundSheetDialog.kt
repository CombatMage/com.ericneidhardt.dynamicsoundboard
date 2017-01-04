package org.neidhardt.dynamicsoundboard.dialog.soundsheetmanagement

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.widget.EditText
import kotlinx.android.synthetic.main.dialog_rename_sound_sheet.view.*
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseDialog
import org.neidhardt.dynamicsoundboard.manager.selectedSoundSheet

/**
 * Created by eric.neidhardt@gmail.com on 04.01.2017.
 */
class RenameSoundSheetDialog : BaseDialog() {

	companion object {
		private val TAG = RenameSoundSheetDialog::class.java.name
		fun showInstance(fragmentManager: FragmentManager) {
			val dialog = RenameSoundSheetDialog()
			dialog.show(fragmentManager, TAG)
		}
	}

	private val soundSheetManager = SoundboardApplication.newSoundSheetManager
	private var soundSheetName: EditText? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val context = this.activity

		@SuppressLint("InflateParams") val view = context.layoutInflater.inflate(R.layout.dialog_rename_sound_sheet, null)

		this.soundSheetName = (view.findViewById(R.id.et_name_sound_sheet) as EditText)
		val editTextHint = view.et_name_sound_sheet_hint
		editTextHint?.hint = soundSheetManager.soundSheets.selectedSoundSheet?.label

		return AlertDialog.Builder(context).apply {
			this.setTitle(R.string.dialog_rename_sound_sheet_title)
			this.setView(view)
			this.setPositiveButton(R.string.dialog_rename, { dialogInterface, i ->
				deliverResult()
				dismiss()
			})
			this.setNegativeButton(org.neidhardt.dynamicsoundboard.R.string.dialog_cancel, { dialogInterface, i -> dismiss()})
		}.create()
	}

	private fun deliverResult() {
		val newLabel = this.soundSheetName?.text.toString()
		this.soundSheetManager.soundSheets.selectedSoundSheet?.let { selectedSoundSheet ->
			selectedSoundSheet.label = newLabel
			this.soundSheetManager.notifyHasChanged(selectedSoundSheet)
		}
	}
}