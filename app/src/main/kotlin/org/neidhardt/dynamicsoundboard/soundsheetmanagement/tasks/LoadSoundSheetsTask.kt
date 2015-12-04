package org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks

import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.longtermtask.LongTermTask
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
	override val TAG = javaClass.name

	@Throws(Exception::class)
	override fun call(): List<SoundSheet>
	{
		return this.daoSession.soundSheetDao.queryBuilder().list()
	}

	@Throws(Exception::class)
	override fun onSuccess(result: List<SoundSheet>)
	{
		super.onSuccess(result)
		for (soundSheet in result)
			this.soundSheetsDataStorage.addSoundSheetToManager(soundSheet)
	}


}
