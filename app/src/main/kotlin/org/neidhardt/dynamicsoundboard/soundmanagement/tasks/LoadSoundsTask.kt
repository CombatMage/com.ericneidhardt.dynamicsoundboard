package org.neidhardt.dynamicsoundboard.soundmanagement.tasks

import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerComparator
import org.neidhardt.dynamicsoundboard.misc.longtermtask.LongTermTask
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import java.util.*

/**
 * File created by eric.neidhardt on 10.04.2015.
 */
public class LoadSoundsTask
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
