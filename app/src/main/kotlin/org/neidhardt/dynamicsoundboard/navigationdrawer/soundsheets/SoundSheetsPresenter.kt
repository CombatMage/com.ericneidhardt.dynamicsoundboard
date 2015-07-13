package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
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
public class SoundSheetsPresenter
(
		public val soundSheetsDataAccess: SoundSheetsDataAccess,
		private val soundSheetsDataStorage: SoundSheetsDataStorage,
		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataStorage: SoundsDataStorage
) :
		NavigationDrawerListPresenter<SoundSheets>(),
		OnSoundSheetsChangedEventListener,
		OnSoundsChangedEventListener
{
	public var adapter: SoundSheetsAdapter? = null

	public val values: MutableList<SoundSheet> = ArrayList()

	override fun isEventBusSubscriber(): Boolean
	{
		return true
	}

	override fun deleteSelectedItems() // TODO check this out
	{
		val soundSheetsToRemove = this.getSoundSheetsSelectedForDeletion()
		for (soundSheet in soundSheetsToRemove)
		{
			val soundsInFragment = this.soundsDataAccess.getSoundsInFragment(soundSheet.getFragmentTag())
			this.soundsDataStorage.removeSounds(soundsInFragment)
		}
		this.soundSheetsDataStorage.removeSoundSheets(soundSheetsToRemove)

		super<NavigationDrawerListPresenter>.onSelectedItemsDeleted()
	}

	public fun onItemClick(data: SoundSheet)
	{
		if (this.isInSelectionMode()) {
			data.setIsSelectedForDeletion(!data.getIsSelectedForDeletion())
			super<NavigationDrawerListPresenter>.onItemSelectedForDeletion()
		}
		else
		{
			this.soundSheetsDataAccess.setSoundSheetSelected(data)
			this.getEventBus().post(OpenSoundSheetEvent(data))
		}
		this.adapter!!.notifyItemChanged(this.values.indexOf(data))
	}

	override fun getNumberOfItemsSelectedForDeletion(): Int
	{
		return this.getSoundSheetsSelectedForDeletion().size()
	}

	override fun deselectAllItemsSelectedForDeletion()
	{
		val selectedSoundSheets = this.getSoundSheetsSelectedForDeletion()
		for (soundSheet in selectedSoundSheets)
		{
			soundSheet.setIsSelectedForDeletion(false)
			this.adapter!!.notifyItemChanged(soundSheet)
		}
	}

	private fun getSoundSheetsSelectedForDeletion(): List<SoundSheet>
	{
		val selectedSoundSheets = ArrayList<SoundSheet>()
		val existingSoundSheets = this.adapter!!.getValues()
		for (soundSheet in existingSoundSheets) {
			if (soundSheet.getIsSelectedForDeletion())
				selectedSoundSheets.add(soundSheet)
		}
		return selectedSoundSheets
	}

	public fun getSoundsInFragment(fragmentTag: String): List<EnhancedMediaPlayer>
	{
		return this.soundsDataAccess.getSoundsInFragment(fragmentTag)
	}

	override fun onEventMainThread(event: SoundAddedEvent)
	{
		val fragmentTag = event.player.getMediaPlayerData().getFragmentTag()
		val changedSoundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(fragmentTag)

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
				affectedFragmentTags.add(player.getMediaPlayerData().getFragmentTag())

			for (fragmentTag in affectedFragmentTags)
			{
				val changedSoundSheet = this.soundSheetsDataAccess
						.getSoundSheetForFragmentTag(fragmentTag)
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

	override fun onEventMainThread(event: SoundSheetsRemovedEvent)
	{
		// TODO do it

		this.notifyDataSetChanged()
	}

	override fun onEventMainThread(event: SoundSheetChangedEvent) {
		this.adapter?.notifyItemChanged(event.soundSheet)
	}

}
