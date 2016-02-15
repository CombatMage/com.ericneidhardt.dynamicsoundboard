package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model

import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.dao.SoundSheetDao
import org.neidhardt.dynamicsoundboard.misc.GreenDaoHelper
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.Playlist
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsManager
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OpenSoundSheetEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetAddedEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetChangedEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetsRemovedEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.LoadSoundSheetsTask
import java.util.*

/**
 * File created by eric.neidhardt on 06.07.2015.
 */
class SoundSheetsManager :
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

	private var isInitDone: Boolean = false

	private val soundLayoutsAccess: SoundLayoutsAccess = SoundboardApplication.getSoundLayoutsAccess()

	init
	{
		this.initIfRequired()
	}

	override fun initIfRequired(): Boolean
	{
		if (!this.isInitDone)
		{
			this.isInitDone = true

			this.soundSheets.clear()
			this.daoSession = GreenDaoHelper.setupDatabase(SoundboardApplication.context, this.getDatabaseName())

			val task = LoadSoundSheetsTask(this.getDbSoundSheets(), this)
			task.execute()

			return true
		}
		else
			return false
	}

	override fun releaseAll()
	{
		this.isInitDone = false

		val copyList = ArrayList<SoundSheet>(soundSheets.size)
		copyList.addAll(soundSheets)

		this.soundSheets.clear()

		this.eventBus.post(SoundSheetsRemovedEvent(copyList))
	}

	private fun getDatabaseName(): String
	{
		val baseName = this.soundLayoutsAccess.getActiveSoundLayout().databaseId
		if (baseName == SoundLayoutsManager.DB_DEFAULT)
			return DB_SOUND_SHEETS_DEFAULT
		return baseName + DB_SOUND_SHEETS
	}

	override fun isPlaylistSoundSheet(fragmentTag: String): Boolean = fragmentTag.equals(Playlist.TAG)

	override fun getDbSoundSheets(): DaoSession = this.daoSession as DaoSession

	override fun getSoundSheets(): List<SoundSheet> = this.soundSheets

	override fun addSoundSheetToManager(soundSheet: SoundSheet)
	{
		this.soundSheets.add(soundSheet)

		val isSelected = soundSheet.isSelected

		if (isSelected)
			this.setSoundSheetSelected(soundSheet)

		soundSheet.insertItemInDatabaseAsync()

		this.eventBus.post(SoundSheetAddedEvent(soundSheet))
	}

	override fun removeSoundSheets(soundSheets: List<SoundSheet>)
	{
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

	override fun getSoundSheetForFragmentTag(fragmentTag: String): SoundSheet?
	{
		val results = this.soundSheets.filter { soundSheet -> soundSheet.fragmentTag.equals(fragmentTag) }
		return if (results.size > 0) results[0] else null
	}

	override fun setSoundSheetSelected(soundSheetToSelect: SoundSheet)
	{
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

	override fun getSelectedItem(): SoundSheet?
	{
		val results = this.soundSheets.filter { soundSheet -> soundSheet.isSelected }
		return if (results.size > 0) results[0] else null
	}

	override fun getSuggestedName(): String
	{
		return SoundboardApplication.context
				.resources.getString(R.string.suggested_sound_sheet_name) + this.soundSheets.size
	}

	override fun getNewSoundSheet(label: String): SoundSheet
	{
		val tag = Integer.toString((label + SoundboardApplication.getRandomNumber()).hashCode())
		return SoundSheet(null, tag, label, false)
	}

}