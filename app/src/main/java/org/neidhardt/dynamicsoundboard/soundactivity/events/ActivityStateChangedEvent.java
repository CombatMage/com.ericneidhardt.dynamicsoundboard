package org.neidhardt.dynamicsoundboard.soundactivity.events;

/**
 * File created by eric.neidhardt on 17.06.2015.
 */
public class ActivityStateChangedEvent
{
	private boolean isResumed;

	public ActivityStateChangedEvent(boolean isResumed)
	{
		this.isResumed = isResumed;
	}

	public boolean isActivityResumed()
	{
		return this.isResumed;
	}

	public boolean isActivityClosed()
	{
		return !this.isResumed;
	}
}
