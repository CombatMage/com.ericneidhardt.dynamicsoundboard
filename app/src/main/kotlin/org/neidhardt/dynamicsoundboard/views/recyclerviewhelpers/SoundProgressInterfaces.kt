package org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers

/**
 * File created by eric.neidhardt on 23.03.2015.
 */
interface SoundProgressViewHolder
{
	fun onProgressUpdate()
}

interface SoundProgressTimer
{
	fun startProgressUpdateTimer()

	fun stopProgressUpdateTimer()
}