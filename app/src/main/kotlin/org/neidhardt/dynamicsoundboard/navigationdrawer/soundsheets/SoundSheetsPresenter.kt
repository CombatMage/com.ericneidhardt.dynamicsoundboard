package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage
import java.util.ArrayList
import java.util.HashSet

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
public open class SoundSheetsPresenter
(
		public val soundSheetsDataAccess: SoundSheetsDataAccess,
		private val soundSheetsDataStorage: SoundSheetsDataStorage,
		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataStorage: SoundsDataStorage
) :
		NavigationDrawerListPresenter<SoundSheets>(),
		NavigationDrawerItemClickListener<SoundSheet>,
		OnSoundSheetsChangedEventListener,
		OnSoundsChangedEventListener
{
	public var adapter: SoundSheetsAdapter? = null

	public val values: MutableList<SoundSheet> = ArrayList()

	override fun isEventBusSubscriber(): Boolean
	{
		return true
	}

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.values.clear()
		this.values.addAll(this.soundSheetsDataAccess.getSoundSheets())
		this.adapter?.notifyDataSetChanged()
	}

	override fun deleteSelectedItems()
	{
		val soundSheetsToRemove = this.getSoundSheetsSelectedForDeletion()
		for (soundSheet in soundSheetsToRemove)
		{
			val soundsInFragment = this.soundsDataAccess.getSoundsInFragment(soundSheet.fragmentTag)
			this.soundsDataStorage.removeSounds(soundsInFragment)
		}
		this.soundSheetsDataStorage.removeSoundSheets(soundSheetsToRemove)

		super.onSelectedItemsDeleted()
	}

	public override fun onItemClick(data: SoundSheet)
	{
		if (this.isInSelectionMode)
		{
			data.isSelectedForDeletion = !data.isSelectedForDeletion
			this.adapter!!.notifyItemChanged(data)
			super.onItemSelectedForDeletion()
		}
		else
		{
			this.soundSheetsDataAccess.setSoundSheetSelected(data)
			this.eventBus.post(OpenSoundSheetEvent(data))
		}
	}

	override val numberOfItemsSelectedForDeletion: Int
		get() = this.getSoundSheetsSelectedForDeletion().size()

	override fun deselectAllItemsSelectedForDeletion()
	{
		val selectedSoundSheets = this.getSoundSheetsSelectedForDeletion()
		for (soundSheet in selectedSoundSheets)
		{
			soundSheet.isSelectedForDeletion = false
			this.adapter!!.notifyItemChanged(soundSheet)
		}
	}

	private fun getSoundSheetsSelectedForDeletion(): List<SoundSheet>
	{
		val selectedSoundSheets = ArrayList<SoundSheet>()
		val existingSoundSheets = this.values
		for (soundSheet in existingSoundSheets) {
			if (soundSheet.isSelectedForDeletion)
				selectedSoundSheets.add(soundSheet)
		}
		return selectedSoundSheets
	}

	public fun getSoundsInFragment(fragmentTag: String): List<MediaPlayerController>
	{
		return this.soundsDataAccess.getSoundsInFragment(fragmentTag)
	}

	override fun onEventMainThread(event: SoundAddedEvent)
	{
		val fragmentTag = event.player.mediaPlayerData.fragmentTag
		val changedSoundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(fragmentTag)
		if (changedSoundSheet != null)
			this.adapter?.notifyItemChanged(changedSoundSheet)
	}

	override fun onEventMainThread(event: SoundsRemovedEvent)
	{
		val removedPlayers = event.players
		if (event.removeAll())
			this.adapter?.notifyDataSetChanged()
		else
		{
			val affectedFragmentTags = HashSet<String>()
			for (player in removedPlayers.orEmpty())
				affectedFragmentTags.add(player.mediaPlayerData.fragmentTag)

			for (fragmentTag in affectedFragmentTags)
			{
				val changedSoundSheet = this.soundSheetsDataAccess
						.getSoundSheetForFragmentTag(fragmentTag)

				if (changedSoundSheet == null)
					this.adapter?.notifyDataSetChanged()
				else
					this.adapter?.notifyItemChanged(changedSoundSheet)
			}
		}
	}

	override fun onEventMainThread(event: SoundChangedEvent) {}

	override fun onEventMainThread(event: SoundMovedEvent) {}

	override fun onEventMainThread(event: SoundSheetAddedEvent)
	{
		this.values.add(event.soundSheet)
		this.adapter?.notifyItemInserted(this.values.size())
	}

	override fun onEventMainThread(event: SoundSheetChangedEvent){
		this.adapter?.notifyItemChanged(event.soundSheet)
	}

	override fun onEventMainThread(event: SoundSheetsRemovedEvent)
	{
		val soundSheetsToRemove = event.soundSheets
		for (soundSheet in soundSheetsToRemove)
			this.removeSoundSheet(soundSheet)
	}

	private fun removeSoundSheet(soundSheet: SoundSheet)
	{
		val index = this.values.indexOf(soundSheet)
		if (index != -1) // should no happen
		{
			this.values.remove(index)
			this.adapter?.notifyItemRemoved(index)
		}
	}
}
