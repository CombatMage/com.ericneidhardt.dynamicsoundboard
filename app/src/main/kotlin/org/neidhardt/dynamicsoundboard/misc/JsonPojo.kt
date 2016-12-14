package org.neidhardt.dynamicsoundboard.misc

import com.google.gson.Gson
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import java.io.File
import java.io.IOException
import java.util.*

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
private val converter: Gson by lazy { Gson() }

@Throws(IOException::class)
fun writeToFile(file: File, soundSheets: List<SoundSheet>,
		playlist: List<MediaPlayerController>, sounds: Map<String, List<MediaPlayerController>>) {

	val pojo = JsonPojo()
	pojo.soundSheets = soundSheets
	pojo.playList = playlist.map { it.mediaPlayerData }
	pojo.sounds = HashMap<String, List<MediaPlayerData>>(sounds.size).apply {
		for (key in sounds.keys) {
			val soundsPerSoundSheet = ArrayList<MediaPlayerData>()
			for (player in sounds[key].orEmpty())
				soundsPerSoundSheet.add(player.mediaPlayerData)
			this.put(key, soundsPerSoundSheet)
		}
	}
	val json = converter.toJson(pojo)
	file.writeText(json)
}

@Throws(IOException::class)
fun readFromFile(file: File): JsonPojo {
	val json = file.readText()
	return converter.fromJson(json, JsonPojo::class.java)
}
