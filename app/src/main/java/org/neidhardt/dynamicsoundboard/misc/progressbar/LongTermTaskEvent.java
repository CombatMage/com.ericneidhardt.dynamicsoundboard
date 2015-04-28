package org.neidhardt.dynamicsoundboard.misc.progressbar;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class LongTermTaskEvent
{
	private final boolean isTaskFinished;

	public LongTermTaskEvent(boolean isTaskFinished)
	{
		this.isTaskFinished = isTaskFinished;
	}

	public boolean isTaskFinished()
	{
		return isTaskFinished;
	}

	@Override
	public String toString()
	{
		return "LongTermTaskEvent{" +
				"isTaskFinished=" + isTaskFinished +
				'}';
	}
}
