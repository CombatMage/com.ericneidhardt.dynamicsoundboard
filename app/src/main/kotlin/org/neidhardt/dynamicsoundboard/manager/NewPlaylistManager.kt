package org.neidhardt.dynamicsoundboard.manager

import android.content.Context
import android.net.Uri
import org.greenrobot.eventbus.EventBus
import org.neidhardt.android_utils.misc.getCopyList
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerFactory
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.misc.getFileForUri
import org.neidhardt.dynamicsoundboard.misc.isAudioFile
import org.neidhardt.dynamicsoundboard.persistance.model.NewMediaPlayerData
import org.neidhardt.dynamicsoundboard.soundmanagement.events.CreatingPlayerFailedEvent
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.add
import java.util.*

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */
class NewPlaylistManager(private val context: Context) {

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

	private fun createPlayerAndAddToPlaylist(playerData: NewMediaPlayerData) {
		if (this.playlist.containsPlayerWithId(playerData.playerId))
			return

		val player = MediaPlayerFactory.createPlayer(this.context, this.eventBus, playerData)
		if (player == null) {
			this.mMediaPlayersData?.remove(playerData)
			this.eventBus.post(CreatingPlayerFailedEvent(playerData))
		}
		else {
			if (this.mMediaPlayersData?.contains(playerData) == false)
				this.mMediaPlayersData?.add(playerData)

			this.mMediaPlayers?.add(player)
		}
	}

	private fun invokeListeners() {
		this.onPlaylistChangedListener.forEach { it.invoke(this.playlist) }
	}
}

object RxNewPlaylistManager {
	fun playlistChanges(manager: NewPlaylistManager): Observable<List<MediaPlayerController>> {
		return Observable.create { subscriber ->
			val listener: (List<MediaPlayerController>) -> Unit = {
				subscriber.onNext(it)
			}
			subscriber.add {
				manager.onPlaylistChangedListener.remove(listener)
			}
			manager.mMediaPlayers?.let { subscriber.onNext(it) }
			manager.onPlaylistChangedListener.add(listener)
		}
	}
}