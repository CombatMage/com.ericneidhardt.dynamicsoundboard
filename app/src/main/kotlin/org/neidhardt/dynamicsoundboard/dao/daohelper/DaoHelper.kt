package org.neidhardt.dynamicsoundboard.dao.daohelper

import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.*
import roboguice.util.SafeAsyncTask

/**
 * File created by eric.neidhardt on 30.06.2015.
 */
public fun updateDatabaseAsync(data: MediaPlayerData)
{
	val soundsDataUtil = DynamicSoundboardApplication.getApplicationComponent().provideSoundsDataUtil()
	val soundsDataStorage = DynamicSoundboardApplication.getApplicationComponent().provideSoundsDataStorage()

	val daoSession =
			if (soundsDataUtil.isPlaylistPlayer(data))
			{
				soundsDataStorage.getDbPlaylist()
			}
			else
				soundsDataStorage.getDbSounds()

	UpdatePlayerAsyncTask(data, daoSession.getMediaPlayerDataDao(), daoSession).execute()
}

private class UpdatePlayerAsyncTask(data: MediaPlayerData, dao: MediaPlayerDataDao, daoSession: DaoSession) : SafeAsyncTask<Void>()
{
	private val data = data
	private val dao = dao
	private val daoSession = daoSession

	override fun call(): Void?
	{
		this.daoSession.runInTx {
			if (dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(data.getPlayerId())).list().size() != 0)
				dao.update(data) // do not update if item was not added before
		}
		return null
	}
}


public fun updateDatabaseAsync(data: SoundSheet)
{
	val soundSheetsDataStorage = DynamicSoundboardApplication.getApplicationComponent().provideSoundSheetsDataStorage();
	val daoSession = soundSheetsDataStorage.getDbSoundSheets()

	UpdateSoundSheetsAsyncTask(data, daoSession.getSoundSheetDao(), daoSession).execute()
}

private class UpdateSoundSheetsAsyncTask(data: SoundSheet, dao: SoundSheetDao, daoSession: DaoSession) : SafeAsyncTask<Void>()
{
	private val data = data
	private val dao = dao
	private val daoSession = daoSession

	override fun call(): Void?
	{
		this.daoSession.runInTx {
			if (dao.queryBuilder().where(SoundSheetDao.Properties.FragmentTag.eq(data.getFragmentTag())).list().size() != 0)
				dao.update(data) // do not update if item was not added before
		}
		return null
	}
}