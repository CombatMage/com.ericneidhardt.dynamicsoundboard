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
	private val TAG = javaClass.getName()

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
		if (!this.isInitDone)
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

	override fun addSoundSheetToManager(newSoundSheet: SoundSheet)
	{
		this.soundSheets.add(newSoundSheet)

		val isSelected = newSoundSheet.getIsSelected()

		if (isSelected)
			this.setSoundSheetSelected(newSoundSheet)

		newSoundSheet.insertItemInDatabaseAsync()

		this.eventBus.post(SoundSheetAddedEvent(newSoundSheet))
	}

	override fun removeSoundSheets(soundSheets: MutableList<SoundSheet>)
	{
		val copyList = ArrayList<SoundSheet>(soundSheets.size());
		copyList.addAll(soundSheets); // this is done to prevent concurrent modification exception

		val dao = this.getDbSoundSheets().getSoundSheetDao()
		for (soundSheetToRemove in copyList)
		{
			this.soundSheets.remove(soundSheetToRemove)
			if (soundSheetToRemove.getIsSelected())
				this.setSoundSheetSelected(this.soundSheets.get(0))

			if (soundSheetToRemove.getId() != null)
				dao.delete(soundSheetToRemove)
			else
			{
				val list = dao.queryBuilder().where(SoundSheetDao.Properties.FragmentTag.eq(soundSheetToRemove.getFragmentTag())).list()
				dao.deleteInTx(list)
			}
		}

		this.eventBus.post(SoundSheetsRemovedEvent(copyList))
	}

	override fun getSoundSheetForFragmentTag(fragmentTag: String): SoundSheet?
	{
		val results = this.soundSheets.filter { soundSheet -> soundSheet.getFragmentTag().equals(fragmentTag) }
		return if (results.size() > 0) results.get(0) else null
	}

	override fun setSoundSheetSelected(soundSheetToSelect: SoundSheet)
	{
		if (!this.soundSheets.contains(soundSheetToSelect))
			throw UnsupportedOperationException(TAG + ": can not select SoundSheet " + soundSheetToSelect + " because it is not loaded")

		for (soundSheet in this.soundSheets)
		{
			if (soundSheet != soundSheetToSelect && soundSheet.getIsSelected()) // make sure only one item is selected
			{
				soundSheet.setIsSelected(false)
				soundSheet.updateItemInDatabaseAsync()
				this.eventBus.post(SoundSheetChangedEvent(soundSheet))
			}
		}
		soundSheetToSelect.setIsSelected(true)
		this.eventBus.post(SoundSheetChangedEvent(soundSheetToSelect))
		this.eventBus.post(OpenSoundSheetEvent(soundSheetToSelect))
	}

	override fun getSelectedItem(): SoundSheet?
	{
		val results = this.soundSheets.filter { soundSheet -> soundSheet.getIsSelected() }
		return if (results.size() > 0) results.get(0) else null
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

}