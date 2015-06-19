package org.neidhardt.dynamicsoundboard.misc;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unused")
public class JsonPojo
{
	private List<SoundSheet> soundSheets;

	private List<MediaPlayerData> playList;

	private Map<String, List<MediaPlayerData>> sounds;

	public List<SoundSheet> getSoundSheets()
	{
		return soundSheets;
	}

	public void setSoundSheets(List<SoundSheet> soundSheets)
	{
		this.soundSheets = soundSheets;
	}

	public List<MediaPlayerData> getPlayList()
	{
		return playList;
	}

	public void setPlayList(List<MediaPlayerData> playList)
	{
		this.playList = playList;
	}

	public Map<String, List<MediaPlayerData>> getSounds()
	{
		return sounds;
	}

	public void setSounds(Map<String, List<MediaPlayerData>> sounds)
	{
		this.sounds = sounds;
	}

	public void addPlayList(List<EnhancedMediaPlayer> playlist)
	{
		if (playlist == null)
			return;
		this.playList = new ArrayList<>(playlist.size());
		for (EnhancedMediaPlayer player : playlist)
			this.playList.add(player.getMediaPlayerData());
	}

	public void addSounds(Map<String, List<EnhancedMediaPlayer>> sounds)
	{
		if (sounds == null)
			return;

		this.sounds = new HashMap<>(sounds.size());

		for (String key : sounds.keySet())
		{
			List<MediaPlayerData> soundsPerSoundSheet = new ArrayList<>();
			for (EnhancedMediaPlayer player : sounds.get(key))
				soundsPerSoundSheet.add(player.getMediaPlayerData());

			this.sounds.put(key, soundsPerSoundSheet);
		}
	}

	public static void writeToFile(File file, List<SoundSheet> soundSheets,
								   List<EnhancedMediaPlayer> playlist, Map<String, List<EnhancedMediaPlayer>> sounds) throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();

		JsonPojo pojo = new JsonPojo();

		pojo.setSoundSheets(soundSheets);
		pojo.addPlayList(playlist);
		pojo.addSounds(sounds);

		mapper.writeValue(file, pojo);
	}

	public static JsonPojo readFromFile(File file) throws IOException
	{
		return new ObjectMapper().readValues(new JsonFactory().createParser(file), JsonPojo.class).next();
	}
}
