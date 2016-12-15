package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model

import android.content.Context
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.dao.SoundSheetDao
import org.neidhardt.dynamicsoundboard.daohelper.GreenDaoHelper
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.*
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OpenSoundSheetEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetAddedEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetChangedEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetsRemovedEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.loadSoundSheets
import rx.android.schedulers.AndroidSchedulers
import java.util.*

/**
 * File created by eric.neidhardt on 06.07.2015.
 */
class SoundSheetsManager(private val context: Context, private val soundLayoutsManager: ISoundLayoutManager) :
		SoundSheetsDataAccess,
		SoundSheetsDataStorage,
		SoundSheetsDataUtil
{
	private val TAG = javaClass.name

	private val DB_SOUND_SHEETS_DEFAULT = "org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment.db_sound_sheets"
	private val DB_SOUND_SHEETS = "db_sound_sheets"

	private var daoSession: DaoSession? = null

	private val soundSheets: MutableList<SoundSheet> = ArrayList()
	private val eventBus = EventBus.getDefault()

	init {
		RxSoundLayoutManager.selectsLayout(this.soundLayoutsManager).subscribe { selectedLayout ->
			this.soundSheets.clear()
			this.daoSession = GreenDaoHelper.setupDatabase(this.context, this.getDatabaseName())
			this.daoSession?.let { dbSoundSheets ->
				SoundboardApplication.taskCounter.value += 1
				dbSoundSheets.loadSoundSheets()
						.flatMapIterable { it -> it }
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe( { soundSheet ->
							Logger.d(TAG, "Loaded: $soundSheet")
							this.addSoundSheetToManager(soundSheet)
						}, { error ->
							Logger.e(TAG, "Error while loading sound sheets: ${error.message}")
							SoundboardApplication.taskCounter.value -= 1
						}, {
							Logger.d(TAG, "Loading sound sheets completed")
							SoundboardApplication.taskCounter.value -= 1
						} )
			}
		}
	}

	override fun releaseAll() {
		val copyList = ArrayList<SoundSheet>(soundSheets.size)
		copyList.addAll(soundSheets)

		this.soundSheets.clear()

		this.eventBus.post(SoundSheetsRemovedEvent(copyList))
	}

	private fun getDatabaseName(): String {
		val baseName = this.soundLayoutsManager.soundLayouts.activeLayout.databaseId
		if (baseName == SoundLayoutManager.DB_DEFAULT)
			return DB_SOUND_SHEETS_DEFAULT
		return baseName + DB_SOUND_SHEETS
	}

	override fun isPlaylistSoundSheet(fragmentTag: String): Boolean = fragmentTag == PlaylistTAG

	override fun getDbSoundSheets(): DaoSession = this.daoSession as DaoSession

	override fun getSoundSheets(): List<SoundSheet> = this.soundSheets

	override fun addSoundSheetToManager(soundSheet: SoundSheet) {
		this.soundSheets.add(soundSheet)

		val isSelected = soundSheet.isSelected

		if (isSelected)
			this.setSoundSheetSelected(soundSheet)

		soundSheet.insertItemInDatabaseAsync()

		this.eventBus.post(SoundSheetAddedEvent(soundSheet))
	}

	override fun removeSoundSheets(soundSheets: List<SoundSheet>) {
		val copyList = ArrayList<SoundSheet>(soundSheets.size)
		copyList.addAll(soundSheets) // this is done to prevent concurrent modification exception

		val dao = this.getDbSoundSheets().soundSheetDao
		for (soundSheetToRemove in copyList)
		{
			this.soundSheets.remove(soundSheetToRemove)
			if (soundSheetToRemove.isSelected)
			{
				if (this.soundSheets.size > 0)
					this.setSoundSheetSelected(this.soundSheets[0])

			}
			if (soundSheetToRemove.id != null)
				dao.delete(soundSheetToRemove)
			else
			{
				val list = dao.queryBuilder().where(SoundSheetDao.Properties.FragmentTag.eq(soundSheetToRemove.fragmentTag)).list()
				dao.deleteInTx(list)
			}
		}

		this.eventBus.post(SoundSheetsRemovedEvent(copyList))
	}

	override fun getSoundSheetForFragmentTag(fragmentTag: String): SoundSheet? {
		val results = this.soundSheets.filter { soundSheet -> soundSheet.fragmentTag == fragmentTag }
		return if (results.isNotEmpty()) results[0] else null
	}

	override fun setSoundSheetSelected(soundSheetToSelect: SoundSheet) {
		if (!this.soundSheets.contains(soundSheetToSelect))
			throw UnsupportedOperationException("$TAG: can not select SoundSheet $soundSheetToSelect because it is not loaded")

		for (soundSheet in this.soundSheets)
		{
			if (soundSheet != soundSheetToSelect && soundSheet.isSelected) // make sure only one item is selected
			{
				soundSheet.isSelected = false
				soundSheet.updateItemInDatabaseAsync()
				this.eventBus.post(SoundSheetChangedEvent(soundSheet))
			}
		}
		soundSheetToSelect.isSelected = true
		this.eventBus.post(SoundSheetChangedEvent(soundSheetToSelect))
		this.eventBus.post(OpenSoundSheetEvent(soundSheetToSelect))
	}

	override fun getSelectedItem(): SoundSheet? {
		val results = this.soundSheets.filter { soundSheet -> soundSheet.isSelected }
		return if (results.isNotEmpty()) results[0] else null
	}

	override fun getSuggestedName(): String = this.context.resources.getString(R.string.suggested_sound_sheet_name) + this.soundSheets.size

	override fun getNewSoundSheet(label: String): SoundSheet {
		val tag = Integer.toString((label + SoundboardApplication.randomNumber).hashCode())
		return SoundSheet(null, tag, label, false)
	}

}