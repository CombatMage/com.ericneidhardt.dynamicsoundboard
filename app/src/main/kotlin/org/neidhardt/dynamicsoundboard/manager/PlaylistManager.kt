package org.neidhardt.dynamicsoundboard.manager

import android.content.Context
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerFactory
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.persistance.model.NewMediaPlayerData
import org.neidhardt.utils.getCopyList

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */
class PlaylistManager(private val context: Context) {

	private val TAG = javaClass.name
	private val eventBus = EventBus.getDefault()

	internal var onPlaylistChangedListener = ArrayList<((List<MediaPlayerController>) -> Unit)>()

	internal var mMediaPlayersData: MutableList<NewMediaPlayerData>? = null
	internal var mMediaPlayers: MutableList<MediaPlayerController>? = null

	val playlist: List<MediaPlayerController> get() = this.mMediaPlayers as List<MediaPlayerController>

	fun set(mediaPlayerData: MutableList<NewMediaPlayerData>) {
		this.mMediaPlayers?.forEach { it.destroy(false) }
		this.mMediaPlayersData = mediaPlayerData
		this.mMediaPlayers = ArrayList()
		// copy list to prevent concurrent modification exception
		val copyList = mediaPlayerData.getCopyList()
		SoundboardApplication.taskCounter.value += 1
		Observable.just(copyList)
				.flatMapIterable { it -> it }
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({ playerData ->
					this.createPlayerAndAddToPlaylist(playerData)
					this.invokeListeners()
				}, { error ->
					Logger.e(TAG, "Error while loading playlist: ${error.message}")
					SoundboardApplication.taskCounter.value -= 1
				}, {
					Logger.d(TAG, "Loading playlist completed")
					SoundboardApplication.taskCounter.value -= 1
				})
	}

	fun remove(playerList: List<MediaPlayerController>) {
		playerList.forEach { player ->
			player.destroy(false)
			this.mMediaPlayers?.remove(player)
			this.mMediaPlayersData?.remove(player.mediaPlayerData)
		}
		this.invokeListeners()
	}

	fun add(mediaPlayerData: NewMediaPlayerData) {
		this.createPlayerAndAddToPlaylist(mediaPlayerData)
		this.invokeListeners()
	}

	fun togglePlaylistSound(mediaPlayerData: NewMediaPlayerData, addToPlaylist: Boolean) {
		if (addToPlaylist) {
			if (this.playlist.findById(mediaPlayerData.playerId) != null)
				throw IllegalArgumentException("player is already part of the playlist")

			val newPlayerData = NewMediaPlayerData().apply {
				this.playerId = mediaPlayerData.playerId
				this.fragmentTag = PlaylistTAG
				this.isLoop = false
				this.label = mediaPlayerData.label
				this.uri = mediaPlayerData.uri
			}
			this.add(newPlayerData)
		}
		else {
			val existingPlayer = this.playlist.findById(mediaPlayerData.playerId)
					?: throw IllegalArgumentException("player is not part of the playlist")
			this.remove(listOf(existingPlayer))
		}
		this.invokeListeners()
	}

	private fun createPlayerAndAddToPlaylist(playerData: NewMediaPlayerData) {
		if (this.playlist.containsPlayerWithId(playerData.playerId))
			return

		playerData.isLoop = false
		val player = MediaPlayerFactory.createPlayer(this.context, this.eventBus, playerData)
		if (player == null) {
			this.mMediaPlayersData?.remove(playerData)
			val message = this.context.getString(R.string.music_service_loading_sound_failed) +
					" " +
					FileUtils.getFileNameFromUri(this.context, playerData.uri)
			Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
		}
		else {
			if (this.mMediaPlayersData?.contains(playerData) == false)
				this.mMediaPlayersData?.add(playerData)

			this.mMediaPlayers?.add(player)
		}
	}

	fun notifyHasChanged(player: MediaPlayerController) {
		if (this.mMediaPlayers == null)
			throw IllegalStateException("playlist manager init not done, can not notify of $player")
		this.invokeListeners()
	}

	private fun invokeListeners() {
		this.onPlaylistChangedListener.forEach { it.invoke(this.playlist) }
	}
}

object RxNewPlaylistManager {
	fun playlistChanges(manager: PlaylistManager): Observable<List<MediaPlayerController>> {
		return Observable.create { subscriber ->
			val listener: (List<MediaPlayerController>) -> Unit = {
				subscriber.onNext(it)
			}
			//subscriber.add(Subscriptions.create {
			//	manager.onPlaylistChangedListener.remove(listener)
			//})
			manager.mMediaPlayers?.let { subscriber.onNext(it) }
			manager.onPlaylistChangedListener.add(listener)
		}
	}
}