package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundLayoutManager
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 17.07.2015.
 */
class SoundLayoutsPresenter
(
		override val eventBus: EventBus
) :
		NavigationDrawerListBasePresenter(),
		NavigationDrawerItemClickListener<NewSoundLayout>
{
	companion object {
		fun createSoundLayoutsPresenter(eventBus: EventBus, adapter: SoundLayoutsAdapter): SoundLayoutsPresenter {
			return SoundLayoutsPresenter(eventBus).apply {
				this.adapter = adapter
			}
		}
	}

	private val manager = SoundboardApplication.soundLayoutManager

	var adapter: SoundLayoutsAdapter by Delegates.notNull<SoundLayoutsAdapter>()
	val values: List<NewSoundLayout> get() = this.manager.soundLayouts

	override fun onAttachedToWindow() {
		this.adapter.notifyDataSetChanged()
	}

	override fun deleteSelectedItems() {
		val soundLayoutsToRemove = this.getSoundLayoutsSelectedForDeletion()
		this.manager.remove(soundLayoutsToRemove)
		this.stopDeletionMode()
	}

	override fun onDetachedFromWindow() { }

	override val numberOfItemsSelectedForDeletion: Int
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

	private fun getSoundLayoutsSelectedForDeletion(): List<NewSoundLayout> {
		val existingSoundLayouts = this.adapter.values
		val selectedSoundLayouts = existingSoundLayouts.orEmpty().filter { it.isSelectedForDeletion }
		return selectedSoundLayouts
	}

	override fun onItemClick(data: NewSoundLayout) {
		if (this.isInSelectionMode) {
			data.isSelectedForDeletion = !data.isSelectedForDeletion
			super.onItemSelectedForDeletion()
			this.adapter.notifyDataSetChanged()
		}
		else {
			this.manager.setSelected(data)
		}
	}
}