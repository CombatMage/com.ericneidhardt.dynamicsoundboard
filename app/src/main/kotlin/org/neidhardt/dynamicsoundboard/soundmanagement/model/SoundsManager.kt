package org.neidhardt.dynamicsoundboard.soundmanagement.model

import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.getNewMediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.GreenDaoHelper
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.Playlist
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadPlaylistFromDatabaseTask
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsFromDatabaseTask
import java.io.IOException
import java.util.*

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class SoundsManager : SoundsDataAccess, SoundsDataStorage, SoundsDataUtil
{
	private val TAG = javaClass.name
	private val eventBus = EventBus.getDefault()

	private val soundLayoutsAccess = DynamicSoundboardApplication.getSoundLayoutsAccess()
	private val soundSheetsDataUtil = DynamicSoundboardApplication.getSoundSheetsDataUtil()

	private var dbPlaylist: DaoSession? = null
	private var dbSounds: DaoSession? = null

	override val sounds: MutableMap<String, MutableList<MediaPlayerController>> = HashMap()
	override val playlist: MutableList<MediaPlayerController> = ArrayList()
	override val currentlyPlayingSounds: MutableSet<MediaPlayerController> = HashSet()

	private var isInitDone: Boolean = false

	init
	{
		this.initIfRequired()
	}

	override fun getDbSounds(): DaoSession
	{
		if (this.dbSounds == null)
			this.dbSounds = GreenDaoHelper.setupDatabase(DynamicSoundboardApplication.getContext(), getDatabaseNameSounds(this.soundLayoutsAccess))
		return this.dbSounds as DaoSession
	}

	override fun getDbPlaylist(): DaoSession
	{
		if (this.dbPlaylist == null)
			this.dbPlaylist = GreenDaoHelper.setupDatabase(DynamicSoundboardApplication.getContext(), getDatabaseNamePlayList(this.soundLayoutsAccess))
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

		this.playlist.map { player-> player.destroy(false) }

		val allPlayerLists = this.sounds.values
		for (players in allPlayerLists)
			players.map { player-> player.destroy(false) }

		this.playlist.clear()
		this.sounds.clear()

		this.eventBus.post(PlaylistChangedEvent())
		this.eventBus.post(SoundsRemovedEvent())
	}

	override fun isPlaylistPlayer(playerData: MediaPlayerData): Boolean = this.soundSheetsDataUtil.isPlaylistSoundSheet(playerData.fragmentTag)

	override fun getSoundsInFragment(fragmentTag: String): List<MediaPlayerController>
	{
		var soundsInFragment: List<MediaPlayerController>? = this.sounds[fragmentTag]
		if (soundsInFragment == null)
		{
			soundsInFragment = ArrayList<MediaPlayerController>()
			this.sounds.put(fragmentTag, soundsInFragment)
		}
		return soundsInFragment
	}

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
		if (this.sounds[fragmentTag] == null)
			this.sounds[fragmentTag] = ArrayList<MediaPlayerController>()

		this.sounds[fragmentTag]?.add(player)

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
			val newPlayerData = MediaPlayerData()
			newPlayerData.id = playerData.id
			newPlayerData.isInPlaylist = true
			newPlayerData.playerId = playerData.playerId
			newPlayerData.fragmentTag = Playlist.TAG
			newPlayerData.isLoop = false
			newPlayerData.label = playerData.label
			newPlayerData.uri = playerData.uri

			return getNewMediaPlayerController (
					context = DynamicSoundboardApplication.getContext(),
					eventBus = EventBus.getDefault(),
					mediaPlayerData = newPlayerData,
					soundsDataStorage = this
			)
		}
		catch (e: IOException)
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
			return getNewMediaPlayerController(
					context = DynamicSoundboardApplication.getContext(),
					eventBus = this.eventBus,
					mediaPlayerData = playerData,
					soundsDataStorage = this)
		}
		catch (e: IOException) {
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
