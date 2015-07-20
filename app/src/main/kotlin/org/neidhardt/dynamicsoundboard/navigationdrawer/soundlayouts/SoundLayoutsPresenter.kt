package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutAddedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRemovedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRenamedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.AddNewSoundLayoutDialog
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.SoundLayoutSettingsDialog
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayouts
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsStorage
import java.util.ArrayList

/**
 * File created by eric.neidhardt on 17.07.2015.
 */
public class SoundLayoutsPresenter
(
		private val soundLayoutsAccess: SoundLayoutsAccess,
		private val soundLayoutsStorage: SoundLayoutsStorage
) :
		NavigationDrawerListPresenter<SoundLayouts>(),
		NavigationDrawerItemClickListener<SoundLayout>,
		SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
		AddNewSoundLayoutDialog.OnSoundLayoutAddedEventListener
{

	public var adapter: SoundLayoutsAdapter? = null

	public var values: MutableList<SoundLayout> = ArrayList<SoundLayout>()

	override fun isEventBusSubscriber(): Boolean
	{
		return true
	}

	override fun onAttachedToWindow()
	{
		super<NavigationDrawerListPresenter>.onAttachedToWindow()
		this.values.clear()
		this.values.addAll(this.soundLayoutsAccess.getSoundLayouts())
		this.adapter?.notifyDataSetChanged()
	}

	override fun deleteSelectedItems()
	{
		val soundLayoutsToRemove = this.getSoundLayoutsSelectedForDeletion()

		this.soundLayoutsStorage.removeSoundLayouts(soundLayoutsToRemove)

		this.adapter?.notifyDataSetChanged()
		this.getEventBus().post(SoundLayoutRemovedEvent())

		super<NavigationDrawerListPresenter>.onSelectedItemsDeleted()
	}

	override fun getNumberOfItemsSelectedForDeletion(): Int
	{
		return this.getSoundLayoutsSelectedForDeletion().size()
	}

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
			if (soundLayout.isSelectedForDeletion())
				selectedSoundLayouts.add(soundLayout)
		}
		return selectedSoundLayouts
	}

	override fun onItemClick(data: SoundLayout)
	{
		if (this.isInSelectionMode())
		{
			data.setIsSelectedForDeletion(!data.isSelectedForDeletion())
			super<NavigationDrawerListPresenter>.onItemSelectedForDeletion()
		}
		else
		{
			this.soundLayoutsAccess.setSoundLayoutSelected(this.values.indexOf(data))
			this.getView().toggleVisibility()
			this.getEventBus().post(SoundLayoutSelectedEvent(data))
		}
		this.adapter?.notifyDataSetChanged()
	}

	override fun onEvent(event: SoundLayoutRenamedEvent)
	{
		val renamedLayout = event.getRenamedSoundLayout()
		this.adapter?.notifyItemChanged(renamedLayout)
	}

	override fun onEvent(event: SoundLayoutAddedEvent)
	{
		val newLayout = event.getData()
		if (!this.values.contains(newLayout))
		{
			this.values.add(newLayout)
			this.adapter?.notifyItemInserted(this.values.size() - 1)
		}
	}

}