package org.neidhardt.dynamicsoundboard.mediaplayer;

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

import java.util.Comparator;

/**
 * File created by eric.neidhardt on 19.06.2015.
 */
public class MediaPlayerComparator implements Comparator<MediaPlayerData>
{
	@Override
	public int compare(MediaPlayerData lhs, MediaPlayerData rhs)
	{
		if (lhs.getSortOrder() == null)
			return -1;
		if (rhs.getSortOrder() == null)
			return 1;

		return lhs.getSortOrder().compareTo(rhs.getSortOrder());
	}
}