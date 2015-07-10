package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model

import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.dao.SoundSheetDao
import org.neidhardt.dynamicsoundboard.misc.Util
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.events.SoundSheetRemovedEvent
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks.LoadSoundSheetsTask
import java.util.ArrayList

/**
 * File created by eric.neidhardt on 06.07.2015.
 */
public class SoundSheetsManager :
		SoundSheetsDataAccess,
		SoundSheetsDataStorage,
		SoundSheetsDataUtil
{

	private val DB_SOUND_SHEETS_DEFAULT = "org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment.db_sound_sheets"
	private val DB_SOUND_SHEETS = "db_sound_sheets"

	private var daoSession: DaoSession? = null
	private var isInitDone: Boolean = false

	private val soundSheets: MutableList<SoundSheet> = ArrayList()
	private val eventBus: EventBus = EventBus.getDefault()

	init
	{
		this.init()
	}

	override fun init()
	{
		if (this.isInitDone)
		{
			this.eventBus.post(SoundSheetsChangedEvent())
			this.eventBus.post(SoundSheetsLoadedEvent(this.soundSheets))
		}
		else
		{
			this.isInitDone = true

			this.soundSheets.clear()
			this.daoSession = Util.setupDatabase(DynamicSoundboardApplication.getSoundboardContext(), this.getDatabaseName())

			val task = LoadSoundSheetsTask(this.getDbSoundSheets(), this)
			task.execute()
		}
	}

	override fun isInit(): Boolean
	{
		return this.isInitDone
	}

	private fun getDatabaseName(): String
	{
		val baseName = SoundLayoutsManager.getInstance().getActiveSoundLayout().getDatabaseId()
		if (baseName == SoundLayoutsManager.DB_DEFAULT)
			return DB_SOUND_SHEETS_DEFAULT
		return baseName + DB_SOUND_SHEETS
	}

	override fun getDbSoundSheets(): DaoSession
	{
		return this.daoSession as DaoSession
	}

	override fun getSoundSheets(): List<SoundSheet>
	{
		return this.soundSheets
	}

	override fun addOrUpdateSoundSheet(soundSheet: SoundSheet): String
	{
		var existingSoundSheet: SoundSheet? = null
		val index = this.soundSheets.indexOf(soundSheet)
		if (index == -1)
			return this.addNewSoundSheet(soundSheet)
		else
			return this.updateExistingSoundSheet(existingSoundSheet as SoundSheet, soundSheet)
	}

	private fun addNewSoundSheet(soundSheet: SoundSheet) : String
	{
		this.soundSheets.add(soundSheet)
		this.daoSession!!.getSoundSheetDao().insert(soundSheet)

		this.eventBus.post(SoundSheetsChangedEvent())
		this.eventBus.post(OpenSoundSheetEvent(soundSheet))

		return soundSheet.getFragmentTag()
	}

	private fun updateExistingSoundSheet(existingSoundSheet: SoundSheet, updateSoundSheet: SoundSheet) : String
	{
		existingSoundSheet.setFragmentTag(updateSoundSheet.getFragmentTag())
		existingSoundSheet.setLabel(updateSoundSheet.getLabel())
		existingSoundSheet.updateItemInDatabaseAsync()

		this.eventBus.post(SoundSheetsChangedEvent())
		this.eventBus.post(OpenSoundSheetEvent(existingSoundSheet))

		return existingSoundSheet.getFragmentTag()
	}

	override fun getSoundSheetForFragmentTag(fragmentTag: String): SoundSheet?
	{
		for (soundSheet in this.soundSheets) {
			if (soundSheet.getFragmentTag() == fragmentTag)
				return soundSheet
		}
		return null
	}

	override fun setSelectedItem(position: Int)
	{
		val size = this.soundSheets.size()
		for (i in 0..size - 1) {
			val isSelected = i == position
			this.soundSheets.get(i).setIsSelected(isSelected)
			this.soundSheets.get(i).updateItemInDatabaseAsync()
		}
	}

	override fun getSelectedItem(): SoundSheet?
	{
		for (soundSheet in this.soundSheets)
		{
			if (soundSheet.getIsSelected())
				return soundSheet
		}
		return null
	}

	override fun getSuggestedName(): String
	{
		return DynamicSoundboardApplication.getSoundboardContext()
				.getResources().getString(R.string.suggested_sound_sheet_name) + this.soundSheets.size()
	}

	override fun getNewSoundSheet(label: String): SoundSheet
	{
		val tag = Integer.toString((label + DynamicSoundboardApplication.getRandomNumber()).hashCode())
		return SoundSheet(null, tag, label, false)
	}

	override fun removeSoundSheet(soundSheet: SoundSheet)
	{
		this.soundSheets.remove(soundSheet)
		val soundSheetDao = this.daoSession!!.getSoundSheetDao()
		if (soundSheet.getId() != null)
			soundSheetDao.delete(soundSheet)
		else
		{
			val playersInDatabase = soundSheetDao.queryBuilder()
					.where(SoundSheetDao.Properties.FragmentTag.eq(soundSheet.getFragmentTag())).list()
			soundSheetDao.deleteInTx(playersInDatabase)
		}

		this.eventBus.post(SoundSheetRemovedEvent(soundSheet))
	}

	override fun removeAllSoundSheets()
	{
		this.soundSheets.clear()
		this.daoSession!!.getSoundSheetDao().deleteAll()
		this.eventBus.post(SoundSheetsChangedEvent())
	}

	override fun addLoadedSoundSheets(soundSheetList: List<SoundSheet>)
	{
		this.soundSheets.addAll(soundSheetList)
		this.findSelectionAndDeselectOthers()
		this.eventBus.post(SoundSheetsChangedEvent())
		this.eventBus.post(SoundSheetsLoadedEvent(soundSheetList))
	}

	private fun findSelectionAndDeselectOthers()
	{
		var selected: SoundSheet? = null
		for (soundSheet in this.soundSheets)
		{
			if (soundSheet.getIsSelected() && selected == null)
				selected = soundSheet
			else
			{
				soundSheet.setIsSelected(false)
				soundSheet.updateItemInDatabaseAsync()
			}
		}
	}

}