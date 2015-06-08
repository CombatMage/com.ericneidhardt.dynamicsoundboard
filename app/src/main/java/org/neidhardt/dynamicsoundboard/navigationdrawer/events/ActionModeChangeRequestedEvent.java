package org.neidhardt.dynamicsoundboard.navigationdrawer.events;

import android.support.v7.view.ActionMode;

/**
 * File created by eric.neidhardt on 24.05.2015.
 */
public class ActionModeChangeRequestedEvent
{
	public enum REQUEST
	{
		START,
		STOP,
		INVALIDATE,
		STOPPED
	}

	private android.support.v7.view.ActionMode.Callback actionModeCallback;
	private REQUEST requestedAction;

	public ActionModeChangeRequestedEvent(ActionMode.Callback actionModeCallback, REQUEST requestedAction)
	{
		this.actionModeCallback = actionModeCallback;
		this.requestedAction = requestedAction;
	}

	public ActionMode.Callback getActionModeCallback()
	{
		return actionModeCallback;
	}

	public REQUEST getRequestedAction()
	{
		return requestedAction;
	}
}
