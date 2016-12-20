package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundSheetManager
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage
import org.neidhardt.eventbus_utils.registerIfRequired
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
fun createSoundSheetPresenter(
		eventBus: EventBus, recyclerView: RecyclerView,
		soundsDataAccess: SoundsDataAccess, soundsDataStorage: SoundsDataStorage): SoundSheetsPresenter
{
	return SoundSheetsPresenter(
			eventBus = eventBus,
			soundsDataAccess = soundsDataAccess,
			soundsDataStorage = soundsDataStorage
	).apply {
		this.adapter = SoundSheetsAdapter(this)
		this.view = recyclerView
	}
}

open class SoundSheetsPresenter
(
		override val eventBus: EventBus,
		private val soundsDataAccess: SoundsDataAccess,
		private val soundsDataStorage: SoundsDataStorage
) :
		NavigationDrawerListBasePresenter<RecyclerView?>(),
		NavigationDrawerItemClickListener<NewSoundSheet>,
		OnSoundSheetsChangedEventListener,
		OnSoundsChangedEventListener
{
	override var view: RecyclerView? = null

	private val manager = SoundboardApplication.newSoundSheetManager

	private var subscriptions = CompositeSubscription()

	var adapter: SoundSheetsAdapter? = null
	val values: List<NewSoundSheet> get() = this.manager.soundSheets

	override fun onAttachedToWindow() {
		this.eventBus.registerIfRequired(this)
		this.adapter?.notifyDataSetChanged()
		this.subscriptions = CompositeSubscription()
		this.subscriptions.add(RxNewSoundSheetManager.soundSheetsChanged(this.manager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.adapter?.notifyDataSetChanged() })
	}

	override fun onDetachedFromWindow() {
		this.eventBus.unregister(this)
		this.subscriptions.unsubscribe()
	}

	override fun deleteSelectedItems() {
		val soundSheetsToRemove = this.getSoundSheetsSelectedForDeletion()
		for (soundSheet in soundSheetsToRemove) {
			val soundsInFragment = this.soundsDataAccess.getSoundsInFragment(soundSheet.fragmentTag)
			this.soundsDataStorage.removeSounds(soundsInFragment)
		}
		this.manager.remove(soundSheetsToRemove)
		this.stopDeletionMode()
	}

	override fun onItemClick(data: NewSoundSheet) {
		if (this.isInSelectionMode) {
			data.isSelectedForDeletion = !data.isSelectedForDeletion
			super.onItemSelectedForDeletion()
			this.adapter?.notifyItemChanged(data)
		}
		else {
			this.manager.setSelected(data)
		}
	}

	override val numberOfItemsSelectedForDeletion: Int
		get() = this.getSoundSheetsSelectedForDeletion().size

	override val itemCount: Int
		get() = this.values.size

	override fun deselectAllItemsSelectedForDeletion() {
		val selectedSoundSheets = this.getSoundSheetsSelectedForDeletion()
		for (soundSheet in selectedSoundSheets) {
			soundSheet.isSelectedForDeletion = false
		}
		this.adapter?.notifyDataSetChanged()
	}

	override fun selectAllItems() {
		val selectedSoundSheets = this.values
		for (soundSheet in selectedSoundSheets) {
			soundSheet.isSelectedForDeletion = true
		}
		this.adapter?.notifyDataSetChanged()
	}

	private fun getSoundSheetsSelectedForDeletion(): List<NewSoundSheet> {
		val selectedSoundSheets = ArrayList<NewSoundSheet>()
		val existingSoundSheets = this.values
		for (soundSheet in existingSoundSheets) {
			if (soundSheet.isSelectedForDeletion)
				selectedSoundSheets.add(soundSheet)
		}
		return selectedSoundSheets
	}

	fun getSoundsInFragment(fragmentTag: String): List<MediaPlayerController> {
		return this.soundsDataAccess.getSoundsInFragment(fragmentTag)
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundAddedEvent)
	{
		/*val fragmentTag = event.player.mediaPlayerData.fragmentTag
		val changedSoundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(fragmentTag)
		if (changedSoundSheet != null)
			this.adapter?.notifyItemChanged(changedSoundSheet)*/
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundsRemovedEvent)
	{
		/*val removedPlayers = event.players
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
		}*/
	}

	// unused events
	override fun onEvent(event: SoundChangedEvent) {}

	override fun onEvent(event: SoundMovedEvent) {}
}
