package org.neidhardt.dynamicsoundboard.soundmanagement.model

import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.misc.Util
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.Playlist
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadPlaylistFromDatabaseTask
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsFromDatabaseTask
import roboguice.util.SafeAsyncTask

import java.io.IOException
import java.util.*

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class SoundsManager : SoundsDataAccess, SoundsDataStorage, SoundsDataUtil
{
	private val TAG = javaClass.getName()
	private val eventBus = EventBus.getDefault()

	private val soundLayoutsAccess = DynamicSoundboardApplication.getSoundLayoutsAccess()
	private val soundSheetsDataUtil = DynamicSoundboardApplication.getSoundSheetsDataUtil()

	private var dbPlaylist: DaoSession? = null
	private var dbSounds: DaoSession? = null

	private val sounds: MutableMap<String, MutableList<EnhancedMediaPlayer>> = HashMap<String, MutableList<EnhancedMediaPlayer>>()
	private val playlist: MutableList<EnhancedMediaPlayer> = ArrayList<EnhancedMediaPlayer>()
	private val currentlyPlayingSounds: MutableSet<EnhancedMediaPlayer> = HashSet<EnhancedMediaPlayer>()

	private var isInitDone: Boolean = false

	init
	{
		this.initIfRequired()
	}

	override fun getDbSounds(): DaoSession
	{
		if (this.dbSounds == null)
			this.dbSounds = Util.setupDatabase(DynamicSoundboardApplication.getContext(), getDatabaseNameSounds(this.soundLayoutsAccess))
		return this.dbSounds as DaoSession
	}


	override fun getDbPlaylist(): DaoSession
	{
		if (this.dbPlaylist == null)
			this.dbPlaylist = Util.setupDatabase(DynamicSoundboardApplication.getContext(), getDatabaseNamePlayList(this.soundLayoutsAccess))
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

	override fun release()
	{
		this.isInitDone = false

		this.releaseMediaPlayers()
	}

	private fun releaseMediaPlayers()
	{
		this.playlist.map { player-> player.destroy(false) }

		val allPlayers = this.sounds.values()
		for (players in allPlayers)
			players.map { player-> player.destroy(false) }

		this.playlist.clear()
		this.sounds.clear()

		this.eventBus.post(PlaylistChangedEvent())
		this.eventBus.post(SoundsRemovedEvent())
	}

	override fun isPlaylistPlayer(playerData: MediaPlayerData): Boolean = this.soundSheetsDataUtil.isPlaylistSoundSheet(playerData.getFragmentTag())

	override fun getCurrentlyPlayingSounds(): Set<EnhancedMediaPlayer> = this.currentlyPlayingSounds

	override fun getPlaylist(): List<EnhancedMediaPlayer> = this.playlist

	override fun getSounds(): Map<String, List<EnhancedMediaPlayer>> = this.sounds

	override fun getSoundsInFragment(fragmentTag: String): List<EnhancedMediaPlayer>
	{
		var soundsInFragment: List<EnhancedMediaPlayer>? = this.sounds.get(fragmentTag)
		if (soundsInFragment == null)
		{
			soundsInFragment = ArrayList<EnhancedMediaPlayer>()
			this.sounds.put(fragmentTag, soundsInFragment)
		}
		return soundsInFragment
	}

	override fun getSoundById(fragmentTag: String, playerId: String): EnhancedMediaPlayer?
	{
		if (this.soundSheetsDataUtil.isPlaylistSoundSheet(fragmentTag))
			return searchInListForId(playerId, playlist)
		else
			return searchInListForId(playerId, this.sounds.get(fragmentTag).orEmpty())
	}

	override fun createSoundAndAddToManager(data: MediaPlayerData)
	{
		if (this.getSoundById(data.getFragmentTag(), data.getPlayerId()) != null)
		{
			Logger.d(TAG, "player: " + data + " is already loaded")
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
		if (this.getSoundById(data.getFragmentTag(), data.getPlayerId()) != null)
		{
			Logger.d(TAG, "player: " + data + " is already loaded")
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
				player.setIsInPlaylist(true)

				val playerForPlaylist = createPlaylistSound(player.getMediaPlayerData())
				if (playerForPlaylist == null)
				{
					this.removePlaylistDataFromDatabase(player.getMediaPlayerData())
					this.eventBus.post(CreatingPlayerFailedEvent(player.getMediaPlayerData()))
				}
				else
					this.addSoundToPlayList(playerForPlaylist)
			}
		}
		else
		{
			if (playerInPlaylist == null)
				return

			player?.setIsInPlaylist(false)

			this.playlist.remove(playerInPlaylist)

			this.removePlayerDataFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), playerInPlaylist.getMediaPlayerData())
			playerInPlaylist.destroy(true)
		}
	}

	private fun addSoundToPlayList(player: EnhancedMediaPlayer)
	{
		this.playlist.add(player)

		val data = player.getMediaPlayerData()
		data.insertItemInDatabaseAsync()

		this.eventBus.post(PlaylistChangedEvent())
	}

	private fun addSoundToSounds(player: EnhancedMediaPlayer)
	{
		val data = player.getMediaPlayerData()
		val fragmentTag = data.getFragmentTag()
		if (this.sounds[fragmentTag] == null)
			this.sounds[fragmentTag] = ArrayList<EnhancedMediaPlayer>()

		this.sounds[fragmentTag]?.add(player)

		data.insertItemInDatabaseAsync()

		this.eventBus.post(SoundAddedEvent(player))
	}

	override fun removeSounds(soundsToRemove: List<EnhancedMediaPlayer>)
	{
		if (soundsToRemove.size() > 0)
		{
			val copyList = ArrayList<EnhancedMediaPlayer>(soundsToRemove.size())
			copyList.addAll(soundsToRemove) // this is done to prevent concurrent modification exception

			for (playerToRemove in copyList)
			{
				val data = playerToRemove.getMediaPlayerData()
				this.sounds.get(data.getFragmentTag())?.remove(playerToRemove)

				if (data.getIsInPlaylist())
				{
					val correspondingPlayerInPlaylist = searchInListForId(data.getPlayerId(), this.playlist)
					if (correspondingPlayerInPlaylist != null)
					{
						this.playlist.remove(correspondingPlayerInPlaylist)

						this.removePlayerDataFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(),
								correspondingPlayerInPlaylist.getMediaPlayerData())
						correspondingPlayerInPlaylist.destroy(true)
					}
				}
				this.removePlayerDataFromDatabase(this.getDbSounds().getMediaPlayerDataDao(), playerToRemove.getMediaPlayerData())
				playerToRemove.destroy(true)
			}
			this.eventBus.post(SoundsRemovedEvent(copyList))
		}
	}

	override fun removeSoundsFromPlaylist(soundsToRemove: List<EnhancedMediaPlayer>)
	{
		soundsToRemove.map { player -> this.toggleSoundInPlaylist(player.getMediaPlayerData().getPlayerId(), false) }
		this.eventBus.post(SoundsRemovedEvent(soundsToRemove))
	}

	override fun moveSoundInFragment(fragmentTag: String, from: Int, to: Int)
	{
		val soundsInFragment = this.sounds.get(fragmentTag)
		if (soundsInFragment != null)
		{
			val playerToMove = soundsInFragment.remove(from)
			soundsInFragment.add(to, playerToMove)

			this.eventBus.post(SoundMovedEvent(playerToMove, from, to))
		}
	}

	private fun createPlaylistSound(playerData: MediaPlayerData): EnhancedMediaPlayer?
	{
		try
		{
			return EnhancedMediaPlayer.getInstanceForPlayList(playerData)
		} catch (e: IOException)
		{
			Logger.d(TAG, playerData.toString() + " " + e.getMessage())
			this.removePlaylistDataFromDatabase(playerData)
			return null
		}

	}

	private fun createSound(playerData: MediaPlayerData): EnhancedMediaPlayer?
	{
		try
		{
			return EnhancedMediaPlayer(playerData, this)
		}
		catch (e: IOException) {
			Logger.d(TAG, e.getMessage())
			this.removeSoundDataFromDatabase(playerData)
			return null
		}

	}

	override fun removeSoundDataFromDatabase(playerData: MediaPlayerData)
	{
		this.removePlayerDataFromDatabase(this.getDbSounds().getMediaPlayerDataDao(), playerData)
	}

	override fun removePlaylistDataFromDatabase(playerData: MediaPlayerData)
	{
		this.removePlayerDataFromDatabase(this.getDbPlaylist().getMediaPlayerDataDao(), playerData)
	}

	private fun removePlayerDataFromDatabase(dao: MediaPlayerDataDao, playerData: MediaPlayerData)
	{
		if (playerData.getId() != null)
			dao.delete(playerData)
		else {
			val playersInDatabase = dao.queryBuilder().where(MediaPlayerDataDao.Properties.PlayerId.eq(playerData.getPlayerId())).list()
			dao.deleteInTx(playersInDatabase)
		}
	}

}
