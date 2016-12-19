package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.support.v7.widget.RecyclerView
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
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
		NavigationDrawerItemClickListener<SoundLayout>
{
	private val TAG = javaClass.name

	override var view: RecyclerView? = null

	var adapter: SoundLayoutsAdapter? = null
	val values: List<SoundLayout> get() = this.soundLayoutsManager.soundLayouts

	private var subscriptions = CompositeSubscription()

	override fun onAttachedToWindow() {
		this.subscriptions = CompositeSubscription()
		this.subscriptions.add(
				RxSoundLayoutManager.changesLayoutList(this.soundLayoutsManager)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe { layouts -> this.adapter?.notifyDataSetChanged() })

		this.subscriptions.add(
				RxSoundLayoutManager.changesLayout(this.soundLayoutsManager)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe { layout -> this.adapter?.notifyItemChanged(layout) }
		)
	}

	override fun deleteSelectedItems() {
		val soundLayoutsToRemove = this.getSoundLayoutsSelectedForDeletion()

		this.subscriptions.add(this.soundLayoutsManager
				.removeSoundLayouts(soundLayoutsToRemove)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe( { items ->
					Logger.d(TAG, "$items removed from list of sound layouts")
				}, { error ->
					Toast.makeText(this.view?.context,
							R.string.sound_layouts_toast_remove_error, Toast.LENGTH_SHORT).show()
					this.stopDeletionMode()
				}, {
					this.stopDeletionMode()
				} )
		)
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
}