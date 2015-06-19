package org.neidhardt.dynamicsoundboard.mediaplayer;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

import java.util.Comparator;

/**
 * File created by eric.neidhardt on 19.06.2015.
 */
public class MediaPlayerComparator implements Comparator<EnhancedMediaPlayer>
{
	@Override
	public int compare(EnhancedMediaPlayer lhs, EnhancedMediaPlayer rhs)
	{
		MediaPlayerData lhsData = lhs.getMediaPlayerData();
		if (lhsData.getSortOrder() == null)
			return -1;
		MediaPlayerData rhsData = rhs.getMediaPlayerData();
		if (rhsData.getSortOrder() == null)
			return 1;

		return lhsData.getSortOrder().compareTo(rhs.getMediaPlayerData().getSortOrder());
	}
}
