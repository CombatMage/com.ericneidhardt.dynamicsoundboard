package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutAddedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRemovedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRenamedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsStorage
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.AddNewSoundLayoutDialog
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.SoundLayoutSettingsDialog
import java.util.*

/**
 * File created by eric.neidhardt on 17.07.2015.
 */
public class SoundLayoutsPresenter
(
		override val eventBus: EventBus,
		private val soundLayoutsAccess: SoundLayoutsAccess,
		private val soundLayoutsStorage: SoundLayoutsStorage
) :
		NavigationDrawerListPresenter<SoundLayouts?>(),
		NavigationDrawerItemClickListener<SoundLayout>,
		SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
		AddNewSoundLayoutDialog.OnSoundLayoutAddedEventListener
{
	override val isEventBusSubscriber: Boolean = true
	override var view: SoundLayouts? = null

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

		this.adapter?.notifyDataSetChanged()
		this.eventBus.post(SoundLayoutRemovedEvent())

		super.onSelectedItemsDeleted()
	}

	override val numberOfItemsSelectedForDeletion: Int
		get() = this.getSoundLayoutsSelectedForDeletion().size

	override fun deselectAllItemsSelectedForDeletion()
	{
		val selectedSoundLayouts = this.getSoundLayoutsSelectedForDeletion()
		for (soundLayout in selectedSoundLayouts)
		{
			soundLayout.setIsSelectedForDeletion(false)
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
			this.view?.toggleVisibility()
			this.eventBus.post(SoundLayoutSelectedEvent(data))
		}
		this.adapter?.notifyDataSetChanged()
	}

	override fun onEvent(event: SoundLayoutRenamedEvent)
	{
		val renamedLayout = event.renamedSoundLayout
		this.adapter?.notifyItemChanged(renamedLayout)
	}

	override fun onEvent(event: SoundLayoutAddedEvent)
	{
		val newLayout = event.data
		if (!this.values.contains(newLayout))
		{
			this.values.add(newLayout)
			this.adapter?.notifyItemInserted(this.values.size - 1)
		}
	}

}