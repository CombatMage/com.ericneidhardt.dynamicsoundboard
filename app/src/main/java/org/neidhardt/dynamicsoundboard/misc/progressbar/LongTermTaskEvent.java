package org.neidhardt.dynamicsoundboard.misc.progressbar;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class LongTermTaskEvent
{
	private final boolean isTaskStarted;

	public LongTermTaskEvent(boolean isTaskFinished)
	{
		this.isTaskStarted = isTaskFinished;
	}

	public boolean isTaskStarted()
	{
		return isTaskStarted;
	}

	@Override
	public String toString()
	{
		return "LongTermTaskEvent{" +
				"isTaskStarted=" + isTaskStarted +
				'}';
	}
}
