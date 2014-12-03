package com.ericneidhardt.dynamicsoundboard.misc;

import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

	public void addPlayList(List<EnhancedMediaPlayer> playList)
	{
		this.playList = new ArrayList<MediaPlayerData>(playList.size());
		for (EnhancedMediaPlayer player : playList)
			this.playList.add(player.getMediaPlayerData());
	}

	public void addSounds(Map<String, List<EnhancedMediaPlayer>> sounds)
	{
		this.sounds = new HashMap<String, List<MediaPlayerData>>(sounds.size());

		for (String key : sounds.keySet())
		{
			List<MediaPlayerData> soundsPerSoundSheet = new ArrayList<MediaPlayerData>();
			for (EnhancedMediaPlayer player : sounds.get(key))
				soundsPerSoundSheet.add(player.getMediaPlayerData());

			this.sounds.put(key, soundsPerSoundSheet);
		}
	}
}
