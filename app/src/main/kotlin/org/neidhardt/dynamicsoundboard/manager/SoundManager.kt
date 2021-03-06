package org.neidhardt.dynamicsoundboard.manager

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import de.greenrobot.common.ListMap
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerFactory
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.logger.Logger
import org.neidhardt.dynamicsoundboard.model.MediaPlayerData
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.app_utils.getCopyList
import java.util.concurrent.TimeUnit

/**
* @author Eric.Neidhardt@GMail.com on 19.12.2016.
*/
class SoundManager(private val context: Context) {

	private val logTag = javaClass.name

	companion object {
		fun getNewMediaPlayerData(fragmentTag: String, uri: Uri, label: String): MediaPlayerData {
			val data = MediaPlayerData()
			data.playerId = (uri.toString() + SoundboardApplication.randomNumber).hashCode().toString()
			data.fragmentTag = fragmentTag
			data.label = label
			data.uri = uri.toString()
			data.isLoop = false
			return data
		}
	}

	private val eventBus = EventBus.getDefault()

	internal var onSoundListChangedListener = ArrayList<((Map<SoundSheet, List<MediaPlayerController>>) -> Unit)>()
	internal var onSoundMovedListener = ArrayList<(MoveEvent) -> Unit>()

	internal var mSoundSheets: MutableList<SoundSheet>? = null
	internal var mMediaPlayers: MutableMap<SoundSheet, MutableList<MediaPlayerController>>? = null

	val sounds: Map<SoundSheet, List<MediaPlayerController>> get() =
			this.mMediaPlayers as Map<SoundSheet, List<MediaPlayerController>>

	@SuppressLint("CheckResult")
	fun set(soundSheets: MutableList<SoundSheet>) {
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

					val soundCopyList = soundSheet.mediaPlayers?.getCopyList()!!
					soundCopyList.forEach { this.createPlayerAndAddToSounds(soundSheet, it) }
					this.invokeListeners()
				}, { error ->
					Logger.e(logTag, "Error while loading playlist: ${error.message}")
					SoundboardApplication.taskCounter.value -= 1
				}, {
					Logger.d(logTag, "Loading playlist completed")
					SoundboardApplication.taskCounter.value -= 1
				})
	}

	fun add(soundSheet: SoundSheet, playerData: MediaPlayerData) {
		this.createPlayerAndAddToSounds(soundSheet, playerData)
		this.invokeListeners()
	}

	@SuppressLint("CheckResult")
	fun add(soundSheet: SoundSheet, playerData: List<MediaPlayerData>) {
		SoundboardApplication.taskCounter.value++
		Observable.just(playerData)
				.flatMapIterable { it }
				.subscribeOn(AndroidSchedulers.mainThread())
				.doOnNext {
					this.createPlayerAndAddToSounds(soundSheet, it)
				}
				.sample(50, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe({
					this.invokeListeners()
				}, { error ->
					Logger.e(logTag, "Error while adding players $error")
					SoundboardApplication.taskCounter.value--
					this.invokeListeners()
				}, {
					Logger.d(logTag, "Adding players finished")
					SoundboardApplication.taskCounter.value--
					this.invokeListeners()
				})
	}

	fun remove(soundSheet: SoundSheet, playerList: List<MediaPlayerController>) {
		playerList.forEach { player ->
			player.destroy(false)
			soundSheet.mediaPlayers?.remove(player.mediaPlayerData)
		}
		val playerOfSoundSheet = this.mMediaPlayers?.get(soundSheet)
		playerOfSoundSheet?.removeAll(playerList)
		this.invokeListeners()
	}

	fun move(soundSheet: SoundSheet, from: Int, to: Int) {
		val players = soundSheet.mediaPlayers ?: return

		val size = players.size
		var indexFrom = from
		var indexTo = to

		if (indexFrom >= size)
			indexFrom = size - 1
		else if (indexFrom < 0)
			indexFrom = 0

		if (indexTo >= size)
			indexTo = size - 1
		else if (indexTo < 0)
			indexTo = 0


		val playerData = players.removeAt(indexFrom)
		players.add(indexTo, playerData)

		val soundsInSoundSheet = this.mMediaPlayers?.get(soundSheet)
		val player = soundsInSoundSheet?.removeAt(indexFrom) ?: throw IllegalStateException("no player was found in soundSheet list")
		soundsInSoundSheet.add(indexTo, player)

		this.onSoundMovedListener.forEach { it.invoke(MoveEvent(player, from, to)) }
	}

	fun notifyHasChanged(player: MediaPlayerController) {
		if (this.mSoundSheets == null)
			throw IllegalStateException("Cannot update player: $player, sound manager init not done")
		this.invokeListeners()
	}

	private fun createPlayerAndAddToSounds(soundSheet: SoundSheet, playerData: MediaPlayerData) {
		val soundsForSoundSheet = this.mMediaPlayers?.getOrPut(soundSheet) { ArrayList() }
				?: throw IllegalStateException("sound manager is not init")

		if (soundsForSoundSheet.containsPlayerWithId(playerData.playerId))
			return

		val player = MediaPlayerFactory.createPlayer(this.context, this.eventBus, playerData)
		if (player == null) {
			soundSheet.mediaPlayers?.remove(playerData)
			val message = this.context.getString(R.string.music_service_loading_sound_failed) +
					" " +
					FileUtils.getFileNameFromUri(this.context, playerData.uri)
			Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
		}
		else {
			soundsForSoundSheet.add(player)
			if (soundSheet.mediaPlayers == null)
				soundSheet.mediaPlayers = ArrayList()

			if (soundSheet.mediaPlayers!!.firstOrNull { it.playerId == playerData.playerId } == null)
				soundSheet.mediaPlayers!!.add(playerData)
		}
	}

	private fun invokeListeners() {
		this.onSoundListChangedListener.forEach { it.invoke(this.sounds) }
	}
}

object RxSoundManager {
	fun changesSoundList(manager: SoundManager): Observable<Map<SoundSheet, List<MediaPlayerController>>> {
		return Observable.create { emitter ->
			val listener: (Map<SoundSheet, List<MediaPlayerController>>) -> Unit = {
				emitter.onNext(manager.sounds)
			}

			emitter.setCancellable {
				manager.onSoundListChangedListener.remove(listener)
			}

			manager.mMediaPlayers?.let { emitter.onNext(it) }
			manager.onSoundListChangedListener.add(listener)
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

fun Map<SoundSheet, List<MediaPlayerController>>.findById(playerId: String): MediaPlayerController? {
	this.entries.forEach { entry ->
		val player = entry.value.findById(playerId)
		if (player != null)
			return player
	}
	return null
}
