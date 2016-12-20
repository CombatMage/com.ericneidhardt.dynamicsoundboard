package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.support.v7.widget.RecyclerView
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundLayoutManager
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutSelectedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.ISoundLayoutManager
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.RxSoundLayoutManager
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

/**
 * File created by eric.neidhardt on 17.07.2015.
 */
fun createSoundLayoutsPresenter(
		eventBus: EventBus, recyclerView: RecyclerView, soundLayoutsManager: ISoundLayoutManager): SoundLayoutsPresenter
{
	return SoundLayoutsPresenter(
			eventBus = eventBus
	).apply {
		this.adapter = SoundLayoutsAdapter(eventBus, this)
		this.view = recyclerView
	}
}

class SoundLayoutsPresenter
(
		override val eventBus: EventBus
) :
		NavigationDrawerListBasePresenter<RecyclerView?>(),
		NavigationDrawerItemClickListener<NewSoundLayout>
{
	override var view: RecyclerView? = null

	private val manager = SoundboardApplication.newSoundLayoutManager

	var adapter: SoundLayoutsAdapter? = null
	val values: List<NewSoundLayout> get() = this.manager.soundLayouts

	private var subscriptions = CompositeSubscription()

	override fun onAttachedToWindow() {
		this.adapter?.notifyDataSetChanged()
		this.subscriptions = CompositeSubscription()
		this.subscriptions.add(RxNewSoundLayoutManager.soundLayoutsChanges(this.manager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.adapter?.notifyDataSetChanged() })
	}

	override fun deleteSelectedItems() {
		val soundLayoutsToRemove = this.getSoundLayoutsSelectedForDeletion()
		this.manager.remove(soundLayoutsToRemove)
		this.stopDeletionMode()
	}

	override fun onDetachedFromWindow() {
		this.subscriptions.unsubscribe()
	}

	override val numberOfItemsSelectedForDeletion: Int
		get() = this.getSoundLayoutsSelectedForDeletion().size

	override val itemCount: Int
		get() = this.values.size

	override fun deselectAllItemsSelectedForDeletion() {
		val selectedSoundLayouts = this.getSoundLayoutsSelectedForDeletion()
		for (soundLayout in selectedSoundLayouts) {
			soundLayout.isSelectedForDeletion = false
		}
		this.adapter?.notifyDataSetChanged()
	}

	override fun selectAllItems() {
		val selectedSoundLayouts = this.values
		for (soundLayout in selectedSoundLayouts) {
			soundLayout.isSelectedForDeletion = true
		}
		this.adapter?.notifyDataSetChanged()
	}

	private fun getSoundLayoutsSelectedForDeletion(): List<NewSoundLayout> {
		val existingSoundLayouts = this.adapter?.values
		val selectedSoundLayouts = existingSoundLayouts.orEmpty().filter { it.isSelectedForDeletion }
		return selectedSoundLayouts
	}

	override fun onItemClick(data: NewSoundLayout) {
		if (this.isInSelectionMode) {
			data.isSelectedForDeletion = !data.isSelectedForDeletion
			super.onItemSelectedForDeletion()
			this.adapter?.notifyDataSetChanged()
		}
		else {
			this.manager.setSelected(data)
			//this.eventBus.post(SoundLayoutSelectedEvent(data))
		}
	}
}