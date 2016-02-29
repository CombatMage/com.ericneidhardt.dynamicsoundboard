package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage
import java.util.*

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
open class SoundSheetsPresenter
(
		override val eventBus: EventBus,
		val soundSheetsDataAccess: SoundSheetsDataAccess,
		private val soundSheetsDataStorage: SoundSheetsDataStorage,
		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataStorage: SoundsDataStorage
) :
		NavigationDrawerListBasePresenter<RecyclerView?>(),
		NavigationDrawerItemClickListener<SoundSheet>,
		OnSoundSheetsChangedEventListener,
		OnSoundsChangedEventListener
{
	override var view: RecyclerView? = null

	var adapter: SoundSheetsAdapter? = null
	val values: MutableList<SoundSheet> = ArrayList()

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
	}

	override fun onItemClick(data: SoundSheet)
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
		get() = this.getSoundSheetsSelectedForDeletion().size

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

	fun getSoundsInFragment(fragmentTag: String): List<MediaPlayerController>
	{
		return this.soundsDataAccess.getSoundsInFragment(fragmentTag)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundAddedEvent)
	{
		val fragmentTag = event.player.mediaPlayerData.fragmentTag
		val changedSoundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(fragmentTag)
		if (changedSoundSheet != null)
			this.adapter?.notifyItemChanged(changedSoundSheet)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundsRemovedEvent)
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundSheetChangedEvent){
		this.adapter?.notifyItemChanged(event.soundSheet)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundSheetsRemovedEvent)
	{
		val soundSheetsToRemove = event.soundSheets
		for (soundSheet in soundSheetsToRemove)
			this.removeSoundSheet(soundSheet)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundSheetAddedEvent)
	{
		this.values.add(event.soundSheet)
		this.adapter?.notifyItemInserted(this.values.size)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	private fun removeSoundSheet(soundSheet: SoundSheet)
	{
		val index = this.values.indexOf(soundSheet)
		if (index != -1) // should no happen
		{
            this.values.removeAt(index)
			this.adapter?.notifyItemRemoved(index)
		}
	}

	// unused events
	override fun onEvent(event: SoundChangedEvent) {}

	override fun onEvent(event: SoundMovedEvent) {}
}
