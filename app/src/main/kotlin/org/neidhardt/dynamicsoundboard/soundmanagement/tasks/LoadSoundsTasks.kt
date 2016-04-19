package org.neidhardt.dynamicsoundboard.soundmanagement.tasks

import android.net.Uri
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.longtermtask.LoadListTask
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerComparator
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import java.io.File
import java.util.*

/**
 * File created by eric.neidhardt on 10.04.2015.
 */
class LoadSoundsFromDatabaseTask
(
		private val daoSession: DaoSession,
		private val soundsDataStorage: SoundsDataStorage
) :
		LoadListTask<MediaPlayerData>()
{
    override val TAG: String = javaClass.name

    @Throws(Exception::class)
	override fun call(): List<MediaPlayerData>
	{
		val mediaPlayersData = this.daoSession.mediaPlayerDataDao.queryBuilder().list()
		Collections.sort(mediaPlayersData, MediaPlayerComparator())

		for (mediaPlayerData in mediaPlayersData)
			this.postUpdatToMainThread({ this.soundsDataStorage.createSoundAndAddToManager(mediaPlayerData) })

		return mediaPlayersData
	}

}

class LoadPlaylistFromDatabaseTask
(
		private val daoSession: DaoSession,
		private val soundsDataStorage: SoundsDataStorage
) :
		LoadListTask<MediaPlayerData>()
{
    override val TAG: String = javaClass.name

	@Throws(Exception::class)
	override fun call(): List<MediaPlayerData>
	{
		var mediaPlayersData = this.daoSession.mediaPlayerDataDao.queryBuilder().list()
		Collections.sort(mediaPlayersData, MediaPlayerComparator())

		for (mediaPlayerData in mediaPlayersData)
			this.postUpdatToMainThread({ this.soundsDataStorage.createPlaylistSoundAndAddToManager(mediaPlayerData) })
		return mediaPlayersData
	}
}

class LoadSoundsFromFileListTask
(
		private val filesToLoad: List<File>,
		private val fragmentTag: String,
		private val soundsDataStorage: SoundsDataStorage
) :
		LoadListTask<File>()
{
    override val TAG: String = javaClass.name

	@Throws(Exception::class)
	override fun call(): List<File>
	{
		for (file in this.filesToLoad)
		{
			val data = getMediaPlayerDataFromFile(file, this.fragmentTag)
			this.postUpdatToMainThread({ this.soundsDataStorage.createSoundAndAddToManager(data) })
		}

		return filesToLoad
	}

	private fun getMediaPlayerDataFromFile(file: File, fragmentTag: String): MediaPlayerData
	{
		val soundUri = Uri.parse(file.absolutePath)
		val soundLabel = FileUtils.stripFileTypeFromName(
				FileUtils.getFileNameFromUri(SoundboardApplication.context, soundUri))
		return MediaPlayerData.getNewMediaPlayerData(fragmentTag, soundUri, soundLabel)
	}

}