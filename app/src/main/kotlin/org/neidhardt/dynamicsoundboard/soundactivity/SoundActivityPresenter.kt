package org.neidhardt.dynamicsoundboard.soundactivity

import android.net.Uri
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.AddNewSoundFromIntentDialog

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SoundActivityPresenter(
		private val view: SoundActivityContract.View,
		private val model: SoundActivityContract.Model
) : SoundActivityContract.Presenter {

	override fun onResumed() {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun onPaused() {
		this.model.saveData()
	}

	override fun userClicksSoundSheetTitle() {
		this.view.openRenameSoundSheetDialog()
	}

	override fun userClicksAddSoundSheet() {
		this.view.openAddSheetDialog()
	}

	override fun userClicksAddSoundDialog() {
		this.view.openAddSoundDialog()
	}

	override fun userClicksAddSoundsDialog() {
		this.view.openAddSoundsDialog()
	}

	override fun userOpenSoundFileWithApp(soundUri: Uri) {
		//val suggestedName = this.soundSheetManager.suggestedName
		//val soundSheets = this.soundSheetManager.soundSheets
		//AddNewSoundFromIntentDialog.showInstance(this.supportFragmentManager, intent.data, suggestedName, soundSheets)
	}
}