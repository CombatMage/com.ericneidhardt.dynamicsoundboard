package org.neidhardt.dynamicsoundboard.soundactivity

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
}