package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.ISoundLayoutManager
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.activeLayout
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.AddNewSoundLayoutDialog
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * File created by eric.neidhardt on 17.07.2015.
 */
fun createSoundLayoutsPresenter(
		eventBus: EventBus, recyclerView: RecyclerView, soundLayoutsManager: ISoundLayoutManager): SoundLayoutsPresenter
{
	return SoundLayoutsPresenter(
			eventBus = eventBus,
			soundLayoutsManager = soundLayoutsManager
	).apply {
		this.adapter = SoundLayoutsAdapter(eventBus, this)
		this.view = recyclerView
	}
}

class SoundLayoutsPresenter
(
		override val eventBus: EventBus,
		private val soundLayoutsManager: ISoundLayoutManager
) :
		NavigationDrawerListBasePresenter<RecyclerView?>(),
		NavigationDrawerItemClickListener<SoundLayout>,
		OnSoundLayoutsChangedEventListener,
		AddNewSoundLayoutDialog.OnSoundLayoutAddedEventListener
{
	override var view: RecyclerView? = null

	var adapter: SoundLayoutsAdapter? = null
	var values: MutableList<SoundLayout> = ArrayList()

	private val subscriptions = CompositeSubscription()

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.values.clear()
		this.values.addAll(this.soundLayoutsManager.soundLayouts)
		this.adapter?.notifyDataSetChanged()
	}

	override fun deleteSelectedItems() {
		val soundLayoutsToRemove = this.getSoundLayoutsSelectedForDeletion()

		//this.subscriptions.add(
				this.soundLayoutsManager
				.removeSoundLayouts(soundLayoutsToRemove)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe( { items ->
					this.eventBus.post(SoundLayoutsRemovedEvent(items))
				}, { error ->
					// TODO may show error
					this.stopDeletionMode()
				}, {
					this.stopDeletionMode()
				} )
		//)
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
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
			this.adapter?.notifyItemChanged(soundLayout)
		}
	}

	override fun selectAllItems() {
		val selectedSoundLayouts = this.values
		for (soundLayout in selectedSoundLayouts)
		{
			soundLayout.isSelectedForDeletion = true
			this.adapter?.notifyItemChanged(soundLayout)
		}
	}

	private fun getSoundLayoutsSelectedForDeletion(): List<SoundLayout> {
		val existingSoundLayouts = this.adapter?.values
		val selectedSoundLayouts = existingSoundLayouts.orEmpty().filter { it.isSelectedForDeletion }
		return selectedSoundLayouts
	}

	override fun onItemClick(data: SoundLayout) {
		if (this.isInSelectionMode) {
			data.isSelectedForDeletion = !data.isSelectedForDeletion
			super.onItemSelectedForDeletion()
		}
		else {
			this.soundLayoutsManager.setSoundLayoutSelected(data)
			this.eventBus.post(SoundLayoutSelectedEvent(data))
		}
		this.adapter?.notifyDataSetChanged()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutAddedEvent) {
		val newLayout = event.data
		if (!this.values.contains(newLayout)) {
			this.values.add(newLayout)
			this.adapter?.notifyItemInserted(this.values.size - 1)
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutsRemovedEvent) {
		val layoutsToRemove = event.soundLayouts
		for (layout in layoutsToRemove) {
			val index = this.values.indexOf(layout)
			if (index != -1) // should no happen
			{
				this.values.removeAt(index)
				this.adapter?.notifyItemRemoved(index)
			}
		}

		val soundLayout = this.soundLayoutsManager.soundLayouts.activeLayout
		this.adapter?.notifyItemChanged(soundLayout)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutRenamedEvent) {
		val renamedLayout = event.renamedSoundLayout
		this.adapter?.notifyItemChanged(renamedLayout)
	}

	override fun onEvent(event: SoundLayoutSelectedEvent) {}
}