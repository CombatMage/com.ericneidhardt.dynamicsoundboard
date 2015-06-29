package org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers

/**
 * File created by eric.neidhardt on 23.03.2015.
 */
public interface SoundProgressViewHolder
{
	public fun onProgressUpdate()
}

public interface SoundProgressTimer
{
	public fun startProgressUpdateTimer()

	public fun stopProgressUpdateTimer()
}