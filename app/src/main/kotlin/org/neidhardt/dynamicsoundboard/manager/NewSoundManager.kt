package org.neidhardt.dynamicsoundboard.manager

import android.content.Context
import android.net.Uri
import de.greenrobot.common.ListMap
import org.greenrobot.eventbus.EventBus
import org.neidhardt.android_utils.misc.getCopyList
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerFactory
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.persistance.model.NewMediaPlayerData
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.dynamicsoundboard.soundmanagement.events.CreatingPlayerFailedEvent
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.add
import java.util.*

/**
* @author Eric.Neidhardt@GMail.com on 19.12.2016.
*/
open class NewSoundManager(private val context: Context) {

	private val TAG = javaClass.name

	companion object {
		fun getNewMediaPlayerData(fragmentTag: String, uri: Uri, label: String): NewMediaPlayerData {
			val data = NewMediaPlayerData()
			data.playerId = Integer.toString((uri.toString() + SoundboardApplication.randomNumber).hashCode())
			data.fragmentTag = fragmentTag
			data.label = label
			data.uri = uri.toString()
			data.isLoop = false
			return data
		}
	}

	private val eventBus = EventBus.getDefault()

	internal var onSoundListChangedListener = ArrayList<((Map<NewSoundSheet, List<MediaPlayerController>>) -> Unit)>()
	internal var onSoundMovedListener = ArrayList<(MoveEvent) -> Unit>()

	internal var mSoundSheets: MutableList<NewSoundSheet>? = null
	internal var mMediaPlayers: MutableMap<NewSoundSheet, MutableList<MediaPlayerController>>? = null

	val sounds: Map<NewSoundSheet, List<MediaPlayerController>> get() =
			this.mMediaPlayers as Map<NewSoundSheet, List<MediaPlayerController>>

	fun set(soundSheets: MutableList<NewSoundSheet>) {
		this.mMediaPlayers?.values?.forEach { it.forEach { it.destroy(false) } }
		this.mSoundSheets = soundSheets
		this.mMediaPlayers = ListMap()
		// copy list to prevent concurrent modification exception
		val copyList = soundSheets.getCopyList()
		SoundboardApplication.taskCounter.value += 1
		Observable.just(copyList)
				.flatMapIterable { it }
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({ soundSheet ->
					if (soundSheet.mediaPlayers == null)
						soundSheet.mediaPlayers = ArrayList()

					val soundCopyList = soundSheet.mediaPlayers.getCopyList()
					soundCopyList.forEach { this.createPlayerAndAddToSounds(soundSheet, it) }
					this.invokeListeners()
				}, { error ->
					Logger.e(TAG, "Error while loading playlist: ${error.message}")
					SoundboardApplication.taskCounter.value -= 1
				}, {
					Logger.d(TAG, "Loading playlist completed")
					SoundboardApplication.taskCounter.value -= 1
				})
	}

	fun add(soundSheet: NewSoundSheet, playerData: NewMediaPlayerData) {
		this.createPlayerAndAddToSounds(soundSheet, playerData)
		this.invokeListeners()
	}

	fun remove(soundSheet: NewSoundSheet, playerList: List<MediaPlayerController>) {
		playerList.forEach { player ->
			player.destroy(false)
			soundSheet.mediaPlayers.remove(player.mediaPlayerData)
		}
		val playerOfSoundSheet = this.mMediaPlayers?.get(soundSheet)
		playerOfSoundSheet?.removeAll(playerList)
		this.invokeListeners()
	}

	fun move(soundSheet: NewSoundSheet, from: Int, to: Int) {
		val size = soundSheet.mediaPlayers.size
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


		val playerData = soundSheet.mediaPlayers.removeAt(indexFrom)
		soundSheet.mediaPlayers.add(indexTo, playerData)

		val soundsInSoundSheet = this.mMediaPlayers?.get(soundSheet)
		val player = soundsInSoundSheet?.removeAt(indexFrom) ?: throw IllegalStateException("no player was found in soundSheet list")
		soundsInSoundSheet?.add(indexTo, player)

		this.onSoundMovedListener.forEach { it.invoke(MoveEvent(player, from, to)) }
	}

	fun notifyHasChanged(player: MediaPlayerController) {
		if (this.mSoundSheets == null)
			throw IllegalStateException("sound manager init not done")
		this.invokeListeners()
	}

	private fun createPlayerAndAddToSounds(soundSheet: NewSoundSheet, playerData: NewMediaPlayerData) {
		val soundsForSoundSheet = this.mMediaPlayers?.getOrPut(soundSheet, { ArrayList() })
				?: throw IllegalStateException("sound manager is not init")

		if (soundsForSoundSheet.containsPlayerWithId(playerData.playerId))
			return

		val player = MediaPlayerFactory.createPlayer(this.context, this.eventBus, playerData)
		if (player == null) {
			soundSheet.mediaPlayers.remove(playerData)
			this.eventBus.post(CreatingPlayerFailedEvent(playerData))
		}
		else {
			soundsForSoundSheet.add(player)
			if (soundSheet.mediaPlayers == null)
				soundSheet.mediaPlayers = ArrayList()
			if (soundSheet.mediaPlayers.firstOrNull { it.playerId == playerData.playerId } == null)
				soundSheet.mediaPlayers.add(playerData)
		}
	}

	private fun invokeListeners() {
		this.onSoundListChangedListener.forEach { it.invoke(this.sounds) }
	}
}

object RxSoundManager {
	fun changesSoundList(manager: NewSoundManager): Observable<Map<NewSoundSheet, List<MediaPlayerController>>> {
		return Observable.create { subscriber ->
			val listener: (Map<NewSoundSheet, List<MediaPlayerController>>) -> Unit = {
				subscriber.onNext(manager.sounds)
			}
			subscriber.add {
				manager.onSoundListChangedListener.remove(listener)
			}
			manager.mMediaPlayers?.let { subscriber.onNext(it) }
			manager.onSoundListChangedListener.add(listener)
		}
	}

	fun movesSoundInList(manager: NewSoundManager): Observable<MoveEvent> {
		return Observable.create { subscriber ->
			val listener: (MoveEvent) -> Unit = {
				subscriber.onNext(it)
			}
			subscriber.add {
				manager.onSoundMovedListener.remove(listener)
			}
			manager.onSoundMovedListener.add(listener)
		}
	}
}

class MoveEvent(val player: MediaPlayerController, val from: Int, val to: Int)

fun List<MediaPlayerController>.findById(playerId: String): MediaPlayerController? {
	return this.firstOrNull { it.mediaPlayerData.playerId == playerId }
}

fun List<MediaPlayerController>.containsPlayerWithId(playerId: String): Boolean {
	return this.findById(playerId) != null
}

fun Map<NewSoundSheet, List<MediaPlayerController>>.findById(playerId: String): MediaPlayerController? {
	this.entries.forEach { entry ->
		val player = entry.value.findById(playerId)
		if (player != null)
			return player
	}
	return null
}
