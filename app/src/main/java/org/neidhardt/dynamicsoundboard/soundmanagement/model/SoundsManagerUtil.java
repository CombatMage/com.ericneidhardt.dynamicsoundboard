package org.neidhardt.dynamicsoundboard.soundmanagement.model;

import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class SoundsManagerUtil
{
	static final String DB_SOUNDS_DEFAULT = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds";
	static final String DB_SOUNDS_PLAYLIST_DEFAULT = "org.neidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds_playlist";

	private static final String DB_SOUNDS = "db_sounds";
	private static final String DB_SOUNDS_PLAYLIST = "db_sounds_playlist";

	static String getDatabaseNameSounds(SoundLayoutsAccess soundLayoutsAccess)
	{
		SoundLayout activeLayout = soundLayoutsAccess.getActiveSoundLayout();
		if (activeLayout.isDefaultLayout())
			return DB_SOUNDS_DEFAULT;
		String baseName = activeLayout.getDatabaseId();
		return baseName + DB_SOUNDS;
	}

	static String getDatabaseNamePlayList(SoundLayoutsAccess soundLayoutsAccess)
	{
		SoundLayout activeLayout = soundLayoutsAccess.getActiveSoundLayout();
		if (activeLayout.isDefaultLayout())
			return DB_SOUNDS_PLAYLIST_DEFAULT;
		String baseName = activeLayout.getDatabaseId();
		return baseName + DB_SOUNDS_PLAYLIST;
	}

	public static EnhancedMediaPlayer searchInListForId(String playerId, List<EnhancedMediaPlayer> sounds)
	{
		if (sounds == null)
			return null;
		for (EnhancedMediaPlayer player : sounds)
		{
			if (player.getMediaPlayerData().getPlayerId().equals(playerId))
				return player;
		}
		return null;
	}

	public static EnhancedMediaPlayer searchInMapForId(String playerId, Map<String, List<EnhancedMediaPlayer>> sounds)
	{
		Set<String> soundSheets = sounds.keySet();
		for (String soundSheet : soundSheets)
		{
			List<EnhancedMediaPlayer> playersInSoundSheet = sounds.get(soundSheet);
			EnhancedMediaPlayer player = searchInListForId(playerId, playersInSoundSheet);
			if (player != null)
				return player;
		}
		return null;
	}
}
