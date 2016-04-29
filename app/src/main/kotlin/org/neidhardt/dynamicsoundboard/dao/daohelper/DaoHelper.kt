package org.neidhardt.dynamicsoundboard.dao.daohelper

import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.*
import roboguice.util.SafeAsyncTask

/**
 * File created by eric.neidhardt on 30.06.2015.
 */
fun insertIntoDatabaseAsync(data: MediaPlayerData)
{
	val soundsDataUtil = SoundboardApplication.soundsDataUtil
	val soundsDataStorage = SoundboardApplication.soundsDataStorage
	val daoSession =
			if (soundsDataUtil.isPlaylistPlayer(data))
				soundsDataStorage.getDbPlaylist()
			else
				soundsDataStorage.getDbSounds()

	InsertPlayerAsyncTask(data, daoSession.mediaPlayerDataDao, daoSession).execute()
}

private class InsertPlayerAsyncTask
(
		private val data: MediaPlayerData,
		private val dao: MediaPlayerDataDao,
		private val daoSession: DaoSession
) : SafeAsyncTask<Void>()
{
	override fun call(): Void?
	{
		this.daoSession.runInTx {
			if (dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(data.playerId)).list().size == 0)
				dao.insert(data)
		}
		return null
	}
}

fun updateDatabaseAsync(data: MediaPlayerData)
{
	val soundsDataUtil = SoundboardApplication.soundsDataUtil
	val soundsDataStorage = SoundboardApplication.soundsDataStorage

	val daoSession =
			if (soundsDataUtil.isPlaylistPlayer(data))
				soundsDataStorage.getDbPlaylist()
			else
				soundsDataStorage.getDbSounds()

	UpdatePlayerAsyncTask(data, daoSession.mediaPlayerDataDao, daoSession).execute()
}

private class UpdatePlayerAsyncTask
(
		private val data: MediaPlayerData,
		private val dao: MediaPlayerDataDao,
		private val daoSession: DaoSession
) : SafeAsyncTask<Void>()
{
	override fun call(): Void?
	{
		this.daoSession.runInTx {
			if (dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(data.playerId)).list().size != 0)
				dao.update(data) // do not update if item was not added before
		}
		return null
	}
}

fun insertIntoDatabaseAsync(data: SoundSheet)
{
	val soundSheetsDataStorage = SoundboardApplication.soundSheetsDataStorage
	val daoSession = soundSheetsDataStorage.getDbSoundSheets()

	InsertSoundSheetAsyncTask(data, daoSession.soundSheetDao, daoSession).execute()
}

private class InsertSoundSheetAsyncTask
(
		private val data: SoundSheet,
		private val dao: SoundSheetDao,
		private val daoSession: DaoSession
) : SafeAsyncTask<Void>()
{
	override fun call(): Void?
	{
		this.daoSession.runInTx {
			if (dao.queryBuilder().where(SoundSheetDao.Properties.FragmentTag.eq(data.fragmentTag)).list().size == 0)
				dao.insert(data)
		}
		return null
	}
}

fun updateDatabaseAsync(data: SoundSheet)
{
	val soundSheetsDataStorage = SoundboardApplication.soundSheetsDataStorage
	val daoSession = soundSheetsDataStorage.getDbSoundSheets()

	UpdateSoundSheetsAsyncTask(data, daoSession.soundSheetDao, daoSession).execute()
}

private class UpdateSoundSheetsAsyncTask
(
		private val data: SoundSheet,
		private val dao: SoundSheetDao,
		private val daoSession: DaoSession
) : SafeAsyncTask<Void>()
{
	override fun call(): Void?
	{
		this.daoSession.runInTx {
			if (dao.queryBuilder().where(SoundSheetDao.Properties.FragmentTag.eq(data.fragmentTag)).list().size != 0)
				dao.update(data) // do not update if item was not added before
		}
		return null
	}
}

fun updateDatabaseAsync(data: SoundLayout)
{
	val soundLayoutsStorage = SoundboardApplication.soundLayoutsStorage
	val daoSession = soundLayoutsStorage.getDbSoundLayouts()

	UpdateSoundLayoutAsyncTask(data, daoSession.soundLayoutDao, daoSession).execute()
}

private class UpdateSoundLayoutAsyncTask
(
		private val data: SoundLayout,
		private val dao: SoundLayoutDao,
		private val daoSession: DaoSession
) : SafeAsyncTask<Void>()
{
	override fun call(): Void?
	{
		this.daoSession.runInTx {
			dao.update(data) // do not update if item was not added before
		}
		return null
	}
}