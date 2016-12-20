package org.neidhardt.dynamicsoundboard.manager

import android.content.Context
import android.net.Uri
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.getNewMediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.misc.getFileForUri
import org.neidhardt.dynamicsoundboard.misc.isAudioFile
import org.neidhardt.dynamicsoundboard.persistance.model.NewMediaPlayerData

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

	protected fun createPlaylistSound(playerData: NewMediaPlayerData): MediaPlayerController? {
		try {
			val file = Uri.parse(playerData.uri).getFileForUri()
			if (file == null || !file.isAudioFile)
				throw Exception("cannot create create media player, given file is no audio file")

			return getNewMediaPlayerController (
					context = this.context,
					eventBus = EventBus.getDefault(),
					mediaPlayerData = playerData,
					manager = SoundboardApplication.newSoundLayoutManager
			)
		}
		catch (e: Exception) {
			Logger.d(TAG, playerData.toString() + " " + e.message)
			return null
		}
	}
}

fun List<MediaPlayerController>.findById(playerId: String): MediaPlayerController? {
	return this.firstOrNull { it.mediaPlayerData.playerId == playerId }
}

fun List<MediaPlayerController>.containsPlayerWithId(playerId: String): Boolean {
	return this.findById(playerId) != null
}