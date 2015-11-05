package org.neidhardt.dynamicsoundboard.navigationdrawer.events

import android.support.v7.view.ActionMode

/**
 * File created by eric.neidhardt on 05.11.2015.
 */
data class ActionModeChangeRequestedEvent(val actionModeCallback: ActionMode.Callback, val requestedAction: ActionModeChangeRequestedEvent.REQUEST)
{
	enum class REQUEST
	{
		START,
		STOP,
		INVALIDATE,
		STOPPED
	}
}

interface OnActionModeChangeRequestedEventListener
{
	/**
	 * This is called by greenRobot EventBus in case a request to change the current contextual action mode has benn submitted.
	 * @param event delivered OpenSoundSheetEvent
	 */
	fun onEvent(event: ActionModeChangeRequestedEvent)
}
