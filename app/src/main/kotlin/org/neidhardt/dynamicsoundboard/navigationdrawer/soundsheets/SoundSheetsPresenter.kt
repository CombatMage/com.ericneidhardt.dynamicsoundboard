package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 26.05.2015.
 */


open class SoundSheetsPresenter() : NavigationDrawerListBasePresenter() {

	companion object {
		fun createSoundSheetPresenter(adapter: SoundSheetsAdapter): SoundSheetsPresenter {
			return SoundSheetsPresenter().apply {
				this.adapter = adapter
			}
		}
	}

	private val soundSheetManager = SoundboardApplication.soundSheetManager
	private val soundManager = SoundboardApplication.soundManager

	var adapter: SoundSheetsAdapter by Delegates.notNull<SoundSheetsAdapter>()
	protected val values: List<SoundSheet> get() = this.soundSheetManager.soundSheets

	override fun onAttachedToWindow() {
		this.adapter.notifyDataSetChanged()
	}

	override fun onDetachedFromWindow() { } // nothing to do

	override fun deleteSelectedItems() {
		val soundSheetsToRemove = this.getSoundSheetsSelectedForDeletion()
		for (soundSheet in soundSheetsToRemove) {
			// remove all sounds of this soundSheet to free resources
			this.soundManager.sounds[soundSheet]?.let {
				this.soundManager.remove(soundSheet, it)
			}
		}
		this.soundSheetManager.remove(soundSheetsToRemove)
		this.stopDeletionMode()
	}

	fun onItemClick(data: SoundSheet) {
		if (this.isInSelectionMode) {
			data.isSelectedForDeletion = !data.isSelectedForDeletion
			this.adapter.notifyItemChanged(data)
		}
		else {
			this.soundSheetManager.setSelected(data)
		}
	}

	public override val numberOfItemsSelectedForDeletion: Int
		get() = this.getSoundSheetsSelectedForDeletion().size

	override val itemCount: Int
		get() = this.values.size

	override fun deselectAllItemsSelectedForDeletion() {
		val selectedSoundSheets = this.getSoundSheetsSelectedForDeletion()
		for (soundSheet in selectedSoundSheets) {
			soundSheet.isSelectedForDeletion = false
		}
		this.adapter.notifyDataSetChanged()
	}

	override fun selectAllItems() {
		val selectedSoundSheets = this.values
		for (soundSheet in selectedSoundSheets) {
			soundSheet.isSelectedForDeletion = true
		}
		this.adapter.notifyDataSetChanged()
	}

	private fun getSoundSheetsSelectedForDeletion(): List<SoundSheet> {
		val existingSoundSheets = this.values
		val selectedSoundSheets = existingSoundSheets.filter { it.isSelectedForDeletion }
		return selectedSoundSheets
	}

}
