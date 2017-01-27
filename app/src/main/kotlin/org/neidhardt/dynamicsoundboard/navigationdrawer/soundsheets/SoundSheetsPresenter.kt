package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets

import android.support.v7.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundSheetManager
import org.neidhardt.dynamicsoundboard.manager.RxSoundManager
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerItemClickListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListBasePresenter
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.*
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 26.05.2015.
 */


open class SoundSheetsPresenter(
		override val eventBus: EventBus
) :
		NavigationDrawerListBasePresenter(),
		NavigationDrawerItemClickListener<NewSoundSheet>
{
	companion object {
		fun createSoundSheetPresenter(eventBus: EventBus, adapter: SoundSheetsAdapter): SoundSheetsPresenter {
			return SoundSheetsPresenter(eventBus).apply {
				this.adapter = adapter
			}
		}
	}

	private val soundSheetManager = SoundboardApplication.soundSheetManager
	private val soundManager = SoundboardApplication.soundManager

	private var subscriptions = CompositeSubscription()

	var adapter: SoundSheetsAdapter by Delegates.notNull<SoundSheetsAdapter>()
	val values: List<NewSoundSheet> get() = this.soundSheetManager.soundSheets

	override fun onAttachedToWindow() {
		this.adapter.notifyDataSetChanged()

		this.subscriptions = CompositeSubscription()

		this.subscriptions.add(this.adapter.clicksViewHolder
				.subscribe { viewHolder ->
					viewHolder.data?.let { this.onItemClick(it) }
				})
	}

	override fun onDetachedFromWindow() {
		this.subscriptions.unsubscribe()
	}

	override fun deleteSelectedItems() {
		val soundSheetsToRemove = this.getSoundSheetsSelectedForDeletion()
		for (soundSheet in soundSheetsToRemove) {
			// remove all souns of this soundSheet to free resources
			this.soundManager.sounds[soundSheet]?.let {
				this.soundManager.remove(soundSheet, it)
			}
		}
		this.soundSheetManager.remove(soundSheetsToRemove)
		this.stopDeletionMode()
	}

	override fun onItemClick(data: NewSoundSheet) {
		if (this.isInSelectionMode) {
			data.isSelectedForDeletion = !data.isSelectedForDeletion
			super.onItemSelectedForDeletion()
			this.adapter.notifyItemChanged(data)
		}
		else {
			this.soundSheetManager.setSelected(data)
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
		this.adapter.notifyDataSetChanged()
	}

	override fun selectAllItems() {
		val selectedSoundSheets = this.values
		for (soundSheet in selectedSoundSheets) {
			soundSheet.isSelectedForDeletion = true
		}
		this.adapter.notifyDataSetChanged()
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

}
