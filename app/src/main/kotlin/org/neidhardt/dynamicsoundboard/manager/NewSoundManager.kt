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
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
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
			data.isInPlaylist = false
			data.isLoop = false
			return data
		}
	}

	private val eventBus = EventBus.getDefault()

	internal var onSoundListChangedListener = ArrayList<((Map<NewSoundSheet, List<MediaPlayerController>>) -> Unit)>()

	internal var mSoundSheets: MutableList<NewSoundSheet>? = null
	internal var mMediaPlayers: MutableMap<NewSoundSheet, MutableList<MediaPlayerController>>? = null

	val sounds: Map<NewSoundSheet, List<MediaPlayerController>> get() = this.mMediaPlayers as Map<NewSoundSheet, List<MediaPlayerController>>

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

	private fun add(soundSheet: NewSoundSheet, playerData: NewMediaPlayerData) {
		this.createPlayerAndAddToSounds(soundSheet, playerData)
		this.invokeListeners()
	}

	private fun createPlayerAndAddToSounds(soundSheet: NewSoundSheet, playerData: NewMediaPlayerData) {
		val soundsForSoundSheet = this.mMediaPlayers?.getOrPut(soundSheet, { ArrayList() })
				?: throw IllegalStateException("sound mangager is not init")

		if (soundsForSoundSheet.containsPlayerWithId(playerData.playerId))
			return

		val player = MediaPlayerFactory.createPlayer(this.context, this.eventBus, playerData)
		if (player == null) {
			soundSheet.mediaPlayers.remove(playerData)
			this.eventBus.post(CreatingPlayerFailedEvent(playerData))
		}
		else {
			if (!soundsForSoundSheet.contains(player))
				soundsForSoundSheet.add(player)
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
}

fun List<MediaPlayerController>.findById(playerId: String): MediaPlayerController? {
	return this.firstOrNull { it.mediaPlayerData.playerId == playerId }
}

fun List<MediaPlayerController>.containsPlayerWithId(playerId: String): Boolean {
	return this.findById(playerId) != null
}