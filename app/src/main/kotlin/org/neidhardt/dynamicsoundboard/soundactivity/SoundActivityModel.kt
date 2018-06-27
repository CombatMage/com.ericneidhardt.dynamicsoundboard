package org.neidhardt.dynamicsoundboard.soundactivity

import android.content.Context
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.neidhardt.dynamicsoundboard.manager.RxNewSoundSheetManager
import org.neidhardt.dynamicsoundboard.manager.SoundLayoutManager
import org.neidhardt.dynamicsoundboard.manager.SoundSheetManager
import org.neidhardt.dynamicsoundboard.notifications.NotificationService
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.dynamicsoundboard.repositories.AppDataRepository


/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SoundActivityModel(
		private val context: Context,
		private val appDataRepository: AppDataRepository,
		private val soundLayoutManager: SoundLayoutManager,
		private val soundSheetManager: SoundSheetManager)
: SoundActivityContract.Model {

	// no unit test
	override fun startNotificationService() {
		NotificationService.start(this.context)
	}

	// no unit test
	override fun loadSoundSheets(): Observable<List<SoundSheet>> {
		return RxNewSoundSheetManager.soundSheetsChanged(this.soundSheetManager)
				.observeOn(AndroidSchedulers.mainThread())
	}

	override fun getSoundSheets(): List<SoundSheet> = this.soundSheetManager.soundSheets

	override fun getNameForNewSoundSheet(): String = this.soundSheetManager.suggestedName

	override fun saveData() {
		this.appDataRepository.save(this.soundLayoutManager.soundLayouts).subscribe()
	}
}