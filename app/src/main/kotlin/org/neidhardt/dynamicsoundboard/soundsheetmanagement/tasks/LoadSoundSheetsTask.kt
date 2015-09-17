package org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks

import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.misc.longtermtask.LongTermTask
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage

/**
 * File created by eric.neidhardt on 08.05.2015.
 */
public class LoadSoundSheetsTask
(
		private val daoSession: DaoSession,
		private val soundSheetsDataStorage: SoundSheetsDataStorage
)
: LongTermTask<List<SoundSheet>>()
{
	private val TAG = javaClass.name

	override fun getTag(): String
	{
		return TAG
	}

	@Throws(Exception::class)
	override fun call(): List<SoundSheet>
	{
		return this.daoSession.soundSheetDao.queryBuilder().list()
	}

	@Throws(Exception::class)
	override fun onSuccess(loadedSoundSheets: List<SoundSheet>)
	{
		super.onSuccess(loadedSoundSheets)
		for (soundSheet in loadedSoundSheets)
			this.soundSheetsDataStorage.addSoundSheetToManager(soundSheet)
	}


}
