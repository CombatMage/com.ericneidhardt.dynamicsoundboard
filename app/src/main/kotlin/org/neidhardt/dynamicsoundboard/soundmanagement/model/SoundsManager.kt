package org.neidhardt.dynamicsoundboard.soundmanagement.model

import android.content.Context
import android.net.Uri
import de.greenrobot.common.ListMap
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.mediaplayer.getNewMediaPlayerController
import org.neidhardt.ui_utils.utils.GreenDaoHelper
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.misc.getFileForUri
import org.neidhardt.dynamicsoundboard.misc.isAudioFile
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadPlaylistFromDatabaseTask
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsFromDatabaseTask
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
class SoundsManager
(
		private val context: Context,
		private val soundLayoutsAccess: SoundLayoutsAccess,
		private val soundSheetsDataUtil: SoundSheetsDataUtil
) : SoundsDataAccess, SoundsDataStorage, SoundsDataUtil
{
	private val TAG = javaClass.name
	private val eventBus = EventBus.getDefault()


	private var dbPlaylist: DaoSession? = null
	private var dbSounds: DaoSession? = null

	private var isInitDone: Boolean = false

	override val sounds: MutableMap<String, MutableList<MediaPlayerController>> = ListMap(HashMap<String, List<MediaPlayerController>>(), true)
	override val playlist: MutableList<MediaPlayerController> = CopyOnWriteArrayList()
	override val currentlyPlayingSounds: MutableSet<MediaPlayerController> = CopyOnWriteArraySet()

	init
	{
		this.initIfRequired()
	}

	override fun getDbSounds(): DaoSession
	{
		if (this.dbSounds == null)
			this.dbSounds = GreenDaoHelper.setupDatabase(this.context, getDatabaseNameSounds(this.soundLayoutsAccess))
		return this.dbSounds as DaoSession
	}

	override fun getDbPlaylist(): DaoSession
	{
		if (this.dbPlaylist == null)
			this.dbPlaylist = GreenDaoHelper.setupDatabase(this.context, getDatabaseNamePlayList(this.soundLayoutsAccess))
		return this.dbPlaylist as DaoSession
	}

	override fun initIfRequired()
	{
		if (!this.isInitDone)
		{
			this.isInitDone = true

			this.sounds.clear()
			this.playlist.clear()
			this.currentlyPlayingSounds.clear()

			this.dbPlaylist = null
			this.dbPlaylist = this.getDbPlaylist()

			this.dbSounds = null
			this.dbSounds = this.getDbSounds()

			LoadSoundsFromDatabaseTask(this.dbSounds as DaoSession, this).execute()
			LoadPlaylistFromDatabaseTask(this.dbPlaylist as DaoSession, this).execute()
		}
	}

	override fun releaseAll()
	{
		this.isInitDone = false

		this.playlist.forEach { player-> player.destroy(false) }
		this.sounds.values.forEach { list -> list.forEach { player-> player.destroy(false) } }

		this.playlist.clear()
		this.sounds.clear()

		this.eventBus.post(PlaylistChangedEvent())
		this.eventBus.post(SoundsRemovedEvent())
	}

	override fun isPlaylistPlayer(playerData: MediaPlayerData): Boolean = this.soundSheetsDataUtil.isPlaylistSoundSheet(playerData.fragmentTag)

	override fun getSoundsInFragment(fragmentTag: String): List<MediaPlayerController> = this.sounds.getOrPut(fragmentTag, { ArrayList<MediaPlayerController>() })

	override fun getSoundById(fragmentTag: String, playerId: String): MediaPlayerController?
	{
		if (this.soundSheetsDataUtil.isPlaylistSoundSheet(fragmentTag))
			return searchInListForId(playerId, playlist)
		else
			return searchInListForId(playerId, this.sounds[fragmentTag].orEmpty())
	}

	override fun createSoundAndAddToManager(data: MediaPlayerData)
	{
		if (this.getSoundById(data.fragmentTag, data.playerId) != null)
		{
			Logger.d(TAG, "player: $data is already loaded")
			return
		}
		val player = this.createSound(data)
		if (player == null)
		{
			this.removeSoundDataFromDatabase(data)
			this.eventBus.post(CreatingPlayerFailedEvent(data))
		}
		else
			this.addSoundToSounds(player)
	}

	override fun createPlaylistSoundAndAddToManager(data: MediaPlayerData)
	{
		if (this.getSoundById(data.fragmentTag, data.playerId) != null)
		{
			Logger.d(TAG, "player: $data is already loaded")
			return
		}

		val player = this.createPlaylistSound(data)
		if (player == null)
		{
			this.removePlaylistDataFromDatabase(data)
			this.eventBus.post(CreatingPlayerFailedEvent(data))
		}
		else
			this.addSoundToPlayList(player)
	}

	override fun toggleSoundInPlaylist(playerId: String, addToPlaylist: Boolean)
	{
		val player = searchInMapForId(playerId, this.sounds)
		val playerInPlaylist = searchInListForId(playerId, playlist)

		if (addToPlaylist)
		{
			if (playerInPlaylist != null)
				return

			if (player != null)
			{
				player.isInPlaylist = true

				val playerForPlaylist = createPlaylistSound(player.mediaPlayerData)
				if (playerForPlaylist == null)
				{
					this.removePlaylistDataFromDatabase(player.mediaPlayerData)
					this.eventBus.post(CreatingPlayerFailedEvent(player.mediaPlayerData))
				}
				else
					this.addSoundToPlayList(playerForPlaylist)
			}
		}
		else
		{
			if (playerInPlaylist == null)
				return

			player?.isInPlaylist = false

			this.playlist.remove(playerInPlaylist)

			this.removePlayerDataFromDatabase(this.getDbPlaylist().mediaPlayerDataDao, playerInPlaylist.mediaPlayerData)
			playerInPlaylist.destroy(true)
		}
	}

	private fun addSoundToPlayList(player: MediaPlayerController)
	{
		this.playlist.add(player)

		val data = player.mediaPlayerData
		data.insertItemInDatabaseAsync()

		this.eventBus.post(PlaylistChangedEvent())
	}

	private fun addSoundToSounds(player: MediaPlayerController)
	{
		val data = player.mediaPlayerData
		val fragmentTag = data.fragmentTag

		this.sounds.getOrPut(fragmentTag, { ArrayList<MediaPlayerController>() }).add(player)

		data.insertItemInDatabaseAsync()

		this.eventBus.post(SoundAddedEvent(player))
	}

	override fun addSoundToCurrentlyPlayingSounds(soundToAdd: MediaPlayerController)
	{
		this.currentlyPlayingSounds.add(soundToAdd)
	}

	override fun removeSoundFromCurrentlyPlayingSounds(soundToRemove: MediaPlayerController)
	{
		this.currentlyPlayingSounds.remove(soundToRemove)
	}

	override fun removeSounds(soundsToRemove: List<MediaPlayerController>)
	{
		if (soundsToRemove.size > 0)
		{
			val copyList = ArrayList<MediaPlayerController>(soundsToRemove.size)
			copyList.addAll(soundsToRemove) // this is done to prevent concurrent modification exception

			for (playerToRemove in copyList)
			{
				val data = playerToRemove.mediaPlayerData
				this.sounds[data.fragmentTag]?.remove(playerToRemove)

				if (data.isInPlaylist)
				{
					val correspondingPlayerInPlaylist = searchInListForId(data.playerId, this.playlist)
					if (correspondingPlayerInPlaylist != null)
					{
						this.playlist.remove(correspondingPlayerInPlaylist)

						this.removePlayerDataFromDatabase(this.getDbPlaylist().mediaPlayerDataDao,
								correspondingPlayerInPlaylist.mediaPlayerData)
						correspondingPlayerInPlaylist.destroy(true)
					}
				}
				this.removePlayerDataFromDatabase(this.getDbSounds().mediaPlayerDataDao, playerToRemove.mediaPlayerData)
				playerToRemove.destroy(true)
			}
			this.eventBus.post(SoundsRemovedEvent(copyList))
		}
	}

	override fun removeSoundsFromPlaylist(soundsToRemove: List<MediaPlayerController>)
	{
		soundsToRemove.map { player -> this.toggleSoundInPlaylist(player.mediaPlayerData.playerId, false) }
		this.eventBus.post(SoundsRemovedEvent(soundsToRemove))
	}

	override fun moveSoundInFragment(fragmentTag: String, from: Int, to: Int)
	{
		val soundsInFragment = this.sounds[fragmentTag]
		if (soundsInFragment != null)
		{
			val size = soundsInFragment.size
			var indexFrom = from
			var indexTo = to

			if (indexFrom > size)
				indexFrom = size - 1
			else if (indexFrom < 0)
				indexFrom = 0

			if (indexTo > size)
				indexTo = size - 1
			else if (indexTo < 0)
				indexTo = 0

			val playerToMove = soundsInFragment.removeAt(indexFrom)
			soundsInFragment.add(indexTo, playerToMove)

			this.eventBus.post(SoundMovedEvent(playerToMove, from, to))
		}
	}

	private fun createPlaylistSound(playerData: MediaPlayerData): MediaPlayerController?
	{
		try
		{
			val file = Uri.parse(playerData.uri).getFileForUri()
			if (file == null || !file.isAudioFile)
				throw Exception("cannot create create media player, given file is no audio file")

			val newPlayerData = MediaPlayerData()
			newPlayerData.id = playerData.id
			newPlayerData.isInPlaylist = true
			newPlayerData.playerId = playerData.playerId
			newPlayerData.fragmentTag = PlaylistTAG
			newPlayerData.isLoop = false
			newPlayerData.label = playerData.label
			newPlayerData.uri = playerData.uri

			return getNewMediaPlayerController (
					context = this.context,
					eventBus = EventBus.getDefault(),
					mediaPlayerData = newPlayerData,
					soundsDataStorage = this
			)
		}
		catch (e: Exception)
		{
			Logger.d(TAG, playerData.toString() + " " + e.message)
			this.removePlaylistDataFromDatabase(playerData)
			return null
		}
	}

	private fun createSound(playerData: MediaPlayerData): MediaPlayerController?
	{
		try
		{
			val file = Uri.parse(playerData.uri).getFileForUri()
			if (file == null || !file.isAudioFile)
				throw Exception("cannot create create media player, given file is no audio file")

			return getNewMediaPlayerController(
					context = this.context,
					eventBus = this.eventBus,
					mediaPlayerData = playerData,
					soundsDataStorage = this)
		}
		catch (e: Exception)
		{
			Logger.d(TAG, e.message)
			this.removeSoundDataFromDatabase(playerData)
			return null
		}
	}

	override fun removeSoundDataFromDatabase(playerData: MediaPlayerData)
	{
		this.removePlayerDataFromDatabase(this.getDbSounds().mediaPlayerDataDao, playerData)
	}

	override fun removePlaylistDataFromDatabase(playerData: MediaPlayerData)
	{
		this.removePlayerDataFromDatabase(this.getDbPlaylist().mediaPlayerDataDao, playerData)
	}

	private fun removePlayerDataFromDatabase(dao: MediaPlayerDataDao, playerData: MediaPlayerData)
	{
		if (playerData.id != null)
			dao.delete(playerData)
		else {
			val playersInDatabase = dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(playerData.playerId)).list()
			dao.deleteInTx(playersInDatabase)
		}
	}

}
