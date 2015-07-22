package org.neidhardt.dynamicsoundboard.soundmanagement.tasks

import android.net.Uri
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerComparator
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.longtermtask.LongTermTask
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import java.io.File
import java.util.Collections

/**
 * File created by eric.neidhardt on 10.04.2015.
 */
public class LoadSoundsFromDatabaseTask
(
		private val daoSession: DaoSession,
		private val soundsDataStorage: SoundsDataStorage
) :
		LongTermTask<List<MediaPlayerData>>()
{
	private val TAG = javaClass.getName()


	throws(Exception::class)
	override fun call(): List<MediaPlayerData>
	{
		val mediaPlayersData = this.daoSession.getMediaPlayerDataDao().queryBuilder().list()
		Collections.sort(mediaPlayersData, MediaPlayerComparator())

		for (mediaPlayerData in mediaPlayersData)
			this.soundsDataStorage.createSoundAndAddToManager(mediaPlayerData)

		return mediaPlayersData
	}

	override fun getTag(): String
	{
		return TAG
	}
}

public class LoadPlaylistFromDatabaseTask
(
		private val daoSession: DaoSession,
		private val soundsDataStorage: SoundsDataStorage
) :
		LongTermTask<List<MediaPlayerData>>()
{
	private val TAG = javaClass.getName()

	throws(Exception::class)
	override fun call(): List<MediaPlayerData>
	{
		var mediaPlayersData = this.daoSession.getMediaPlayerDataDao().queryBuilder().list()
		Collections.sort(mediaPlayersData, MediaPlayerComparator())

		for (mediaPlayerData in mediaPlayersData)
			this.soundsDataStorage.createPlaylistSoundAndAddToManager(mediaPlayerData)
		return mediaPlayersData
	}

	override fun getTag(): String
	{
		return TAG
	}
}

public class LoadSoundsFromFileListTask
(
		private val filesToLoad: List<File>,
		private val fragmentTag: String,
		private val soundsDataStorage: SoundsDataStorage
) :
		LongTermTask<List<File>>()
{
	private val TAG = javaClass.getName()

	throws(Exception::class)
	override fun call(): List<File>
	{
		for (file in this.filesToLoad)
		{
			val data = getMediaPlayerDataFromFile(file, this.fragmentTag)
			this.soundsDataStorage.createSoundAndAddToManager(data)
		}

		return filesToLoad
	}

	throws(Exception::class)
	override fun onSuccess(files: List<File>)
	{
		super.onSuccess(files)
	}

	override fun getTag(): String
	{
		return TAG
	}

	private fun getMediaPlayerDataFromFile(file: File, fragmentTag: String): MediaPlayerData
	{
		val soundUri = Uri.parse(file.getAbsolutePath())
		val soundLabel = FileUtils.stripFileTypeFromName(
				FileUtils.getFileNameFromUri(DynamicSoundboardApplication.getContext(), soundUri))
		return EnhancedMediaPlayer.getMediaPlayerData(fragmentTag, soundUri, soundLabel)
	}

}