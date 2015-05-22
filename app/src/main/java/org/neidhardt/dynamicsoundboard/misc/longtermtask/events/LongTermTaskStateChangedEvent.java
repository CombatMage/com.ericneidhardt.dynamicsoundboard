package org.neidhardt.dynamicsoundboard.misc.longtermtask.events;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class LongTermTaskStateChangedEvent
{
	private final boolean isLongTermTaskInProgress;

	public LongTermTaskStateChangedEvent(boolean isLongTermTaskInProgress)
	{
		this.isLongTermTaskInProgress = isLongTermTaskInProgress;
	}

	public boolean isLongTermTaskInProgress()
	{
		return isLongTermTaskInProgress;
	}

	@Override
	public String toString()
	{
		return "LongTermTaskStateChangedEvent{" +
				"isLongTermTaskInProgress=" + isLongTermTaskInProgress +
				'}';
	}
}
