package org.neidhardt.dynamicsoundboard.misc;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.List;
import java.util.Map;

/**
 * Created by eric.neidhardt@gmail.com on 14.12.2016.
 */

public class JsonPojo {
	public List<SoundSheet> soundSheets;

	public List<MediaPlayerData> playList;

	public Map<String, List<MediaPlayerData>> sounds;
}