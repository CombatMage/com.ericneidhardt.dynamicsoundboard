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

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
fun createSoundSheetPresenter(eventBus: EventBus, recyclerView: RecyclerView): SoundSheetsPresenter {
	return SoundSheetsPresenter(eventBus).apply {
		this.adapter = SoundSheetsAdapter(this)
		this.view = recyclerView
	}
}

open class SoundSheetsPresenter(
		override val eventBus: EventBus
) :
		NavigationDrawerListBasePresenter<RecyclerView?>(),
		NavigationDrawerItemClickListener<NewSoundSheet>
{
	override var view: RecyclerView? = null

	private val soundSheetManager = SoundboardApplication.newSoundSheetManager
	private val soundManager = SoundboardApplication.newSoundManager

	private var subscriptions = CompositeSubscription()

	var adapter: SoundSheetsAdapter? = null
	val values: List<NewSoundSheet> get() = this.soundSheetManager.soundSheets

	override fun onAttachedToWindow() {
		this.adapter?.notifyDataSetChanged()

		this.subscriptions = CompositeSubscription()
		this.subscriptions.add(RxNewSoundSheetManager.soundSheetsChanged(this.soundSheetManager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.adapter?.notifyDataSetChanged() })

		this.subscriptions.add(RxSoundManager.changesSoundList(this.soundManager)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { this.adapter?.notifyDataSetChanged() })
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
			this.adapter?.notifyItemChanged(data)
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

}
