package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsStorage
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.AddNewSoundLayoutDialog
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.SoundLayoutSettingsDialog
import java.util.*

/**
 * File created by eric.neidhardt on 17.07.2015.
 */
fun createSoundLayoutsPresenter(
		eventBus: EventBus, recyclerView: RecyclerView, soundLayoutsAccess: SoundLayoutsAccess, soundLayoutsStorage: SoundLayoutsStorage): SoundLayoutsPresenter
{
	return SoundLayoutsPresenter(
			eventBus = eventBus,
			soundLayoutsAccess = soundLayoutsAccess,
			soundLayoutsStorage = soundLayoutsStorage
	).apply {
		this.adapter = SoundLayoutsAdapter(eventBus, this)
		this.view = recyclerView
	}
}

class SoundLayoutsPresenter
(
		override val eventBus: EventBus,
		private val soundLayoutsAccess: SoundLayoutsAccess,
		private val soundLayoutsStorage: SoundLayoutsStorage
) :
		NavigationDrawerListBasePresenter<RecyclerView?>(),
		NavigationDrawerItemClickListener<SoundLayout>,
		SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
		AddNewSoundLayoutDialog.OnSoundLayoutAddedEventListener,
		OnSoundLayoutsChangedEventListener
{
	override var view: RecyclerView? = null

	var adapter: SoundLayoutsAdapter? = null
	var values: MutableList<SoundLayout> = ArrayList()

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.values.clear()
		this.values.addAll(this.soundLayoutsAccess.getSoundLayouts())
		this.adapter?.notifyDataSetChanged()
	}

	override fun deleteSelectedItems()
	{
		val soundLayoutsToRemove = this.getSoundLayoutsSelectedForDeletion()

		this.soundLayoutsStorage.removeSoundLayouts(soundLayoutsToRemove)
	}

	override val numberOfItemsSelectedForDeletion: Int
		get() = this.getSoundLayoutsSelectedForDeletion().size

	override val itemCount: Int
		get() = this.values.size

	override fun deselectAllItemsSelectedForDeletion()
	{
		val selectedSoundLayouts = this.getSoundLayoutsSelectedForDeletion()
		for (soundLayout in selectedSoundLayouts)
		{
			soundLayout.setIsSelectedForDeletion(false)
			this.adapter?.notifyItemChanged(soundLayout)
		}
	}

	override fun selectAllItems()
	{
		val selectedSoundLayouts = this.values
		for (soundLayout in selectedSoundLayouts)
		{
			soundLayout.setIsSelectedForDeletion(true)
			this.adapter?.notifyItemChanged(soundLayout)
		}
	}

	private fun getSoundLayoutsSelectedForDeletion(): List<SoundLayout>
	{
		val selectedSoundLayouts = ArrayList<SoundLayout>()
		val existingSoundLayouts = this.adapter?.getValues()
		for (soundLayout in existingSoundLayouts.orEmpty())
		{
			if (soundLayout.isSelectedForDeletion)
				selectedSoundLayouts.add(soundLayout)
		}
		return selectedSoundLayouts
	}

	override fun onItemClick(data: SoundLayout)
	{
		if (this.isInSelectionMode)
		{
			data.setIsSelectedForDeletion(!data.isSelectedForDeletion)
			super.onItemSelectedForDeletion()
		}
		else
		{
			this.soundLayoutsAccess.setSoundLayoutSelected(this.values.indexOf(data))
			this.eventBus.post(SoundLayoutSelectedEvent(data))
		}
		this.adapter?.notifyDataSetChanged()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutRenamedEvent)
	{
		val renamedLayout = event.renamedSoundLayout
		this.adapter?.notifyItemChanged(renamedLayout)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutAddedEvent)
	{
		val newLayout = event.data
		if (!this.values.contains(newLayout))
		{
			this.values.add(newLayout)
			this.adapter?.notifyItemInserted(this.values.size - 1)
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutsRemovedEvent)
	{
		val layoutsToRemove = event.soundLayouts
		for (layout in layoutsToRemove)
			this.removeLayout(layout)

		val soundLayout = this.soundLayoutsAccess.getActiveSoundLayout()
		this.adapter?.notifyItemChanged(soundLayout)
	}

	private fun removeLayout(soundSheet: SoundLayout)
	{
		val index = this.values.indexOf(soundSheet)
		if (index != -1) // should no happen
		{
			this.values.removeAt(index)
			this.adapter?.notifyItemRemoved(index)
		}
	}
}