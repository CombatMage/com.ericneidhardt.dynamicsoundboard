package org.neidhardt.dynamicsoundboard.misc

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import java.io.File
import java.io.IOException
import java.util.*

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
@Throws(IOException::class)
fun writeToFile(file: File, soundSheets: List<SoundSheet>,
		playlist: List<MediaPlayerController>, sounds: Map<String, List<MediaPlayerController>>)
{
	val mapper = ObjectMapper()

	val pojo = JsonPojo()

	pojo.soundSheets = soundSheets
	pojo.addPlayList(playlist)
	pojo.addSounds(sounds)

	mapper.writeValue(file, pojo)
}

@Throws(IOException::class)
fun readFromFile(file: File): JsonPojo = ObjectMapper().readValues(JsonFactory().createParser(file), JsonPojo::class.java).next()

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unused")
class JsonPojo
{
	var soundSheets: List<SoundSheet> = ArrayList()

	var playList: MutableList<MediaPlayerData> = ArrayList()
		private set

	var sounds: MutableMap<String, List<MediaPlayerData>> = HashMap()
        private set

	fun addPlayList(playlist: List<MediaPlayerController>)
	{
		this.playList = ArrayList<MediaPlayerData>(playlist.size).apply {
			for (player in playlist)
				this.add(player.mediaPlayerData)
		}
	}

	fun addSounds(sounds: Map<String, List<MediaPlayerController>>)
	{
		this.sounds = HashMap<String, List<MediaPlayerData>>(sounds.size).apply {
			for (key in sounds.keys)
			{
				val soundsPerSoundSheet = ArrayList<MediaPlayerData>()
				for (player in sounds[key].orEmpty())
					soundsPerSoundSheet.add(player.mediaPlayerData)
				this.put(key, soundsPerSoundSheet)
			}
		}
	}
}
