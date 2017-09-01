package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.soundlayouts

import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.NavigationDrawerListBasePresenter
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 17.07.2015.
 */
class SoundLayoutsPresenter() : NavigationDrawerListBasePresenter() {

	companion object {
		fun createSoundLayoutsPresenter(adapter: SoundLayoutsAdapter): SoundLayoutsPresenter {
			return SoundLayoutsPresenter().apply {
				this.adapter = adapter
			}
		}
	}

	private val manager = SoundboardApplication.soundLayoutManager

	var adapter: SoundLayoutsAdapter by Delegates.notNull<SoundLayoutsAdapter>()
	val values: List<SoundLayout> get() = this.manager.soundLayouts

	override fun onAttachedToWindow() {
		this.adapter.notifyDataSetChanged()
	}

	override fun deleteSelectedItems() {
		val soundLayoutsToRemove = this.getSoundLayoutsSelectedForDeletion()
		this.manager.remove(soundLayoutsToRemove)
		this.stopDeletionMode()
	}

	override fun onDetachedFromWindow() { }

	public override val numberOfItemsSelectedForDeletion: Int
		get() = this.getSoundLayoutsSelectedForDeletion().size

	override val itemCount: Int
		get() = this.values.size

	override fun deselectAllItemsSelectedForDeletion() {
		val selectedSoundLayouts = this.getSoundLayoutsSelectedForDeletion()
		for (soundLayout in selectedSoundLayouts) {
			soundLayout.isSelectedForDeletion = false
		}
		this.adapter.notifyDataSetChanged()
	}

	override fun selectAllItems() {
		val selectedSoundLayouts = this.values
		for (soundLayout in selectedSoundLayouts) {
			soundLayout.isSelectedForDeletion = true
		}
		this.adapter.notifyDataSetChanged()
	}

	private fun getSoundLayoutsSelectedForDeletion(): List<SoundLayout> {
		val existingSoundLayouts = this.adapter.values
		val selectedSoundLayouts = existingSoundLayouts.orEmpty().filter { it.isSelectedForDeletion }
		return selectedSoundLayouts
	}

	fun onItemClick(data: SoundLayout) {
		if (this.isInSelectionMode) {
			data.isSelectedForDeletion = !data.isSelectedForDeletion
			this.adapter.notifyDataSetChanged()
		}
		else {
			this.manager.setSelected(data)
		}
	}
}