package org.neidhardt.dynamicsoundboard.mediaplayer

import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData

import java.util.Comparator

/**
 * File created by eric.neidhardt on 19.06.2015.
 */
class MediaPlayerComparator : Comparator<MediaPlayerData>
{
	override fun compare(lhs: MediaPlayerData, rhs: MediaPlayerData): Int
	{
		if (lhs.sortOrder == null)
			return -1
		if (rhs.sortOrder == null)
			return 1

		return lhs.sortOrder!!.compareTo(rhs.sortOrder)
	}
}

