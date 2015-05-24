package org.neidhardt.dynamicsoundboard.navigationdrawer.events;

import android.support.v7.view.ActionMode;

/**
 * Created by eric.neidhardt on 24.05.2015.
 */
public class StartActionModeEvent
{
	// TODO make generic action mode event
	// TODO triggger invalidate and finish

	private final android.support.v7.view.ActionMode.Callback actionModeCallback;

	public StartActionModeEvent(ActionMode.Callback actionModeCallback)
	{
		this.actionModeCallback = actionModeCallback;
	}

	public ActionMode.Callback getActionModeCallback()
	{
		return actionModeCallback;
	}
}
