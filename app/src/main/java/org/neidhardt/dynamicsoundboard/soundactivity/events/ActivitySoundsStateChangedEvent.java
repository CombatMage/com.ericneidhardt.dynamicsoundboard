package org.neidhardt.dynamicsoundboard.soundactivity.events;

/**
 * Created by eric.neidhardt on 21.05.2015.
 */
public class ActivitySoundsStateChangedEvent
{
	private final boolean isAnySoundPlaying;

	public ActivitySoundsStateChangedEvent(boolean isAnySoundPlaying)
	{
		this.isAnySoundPlaying = isAnySoundPlaying;
	}

	public boolean isAnySoundPlaying()
	{
		return isAnySoundPlaying;
	}

	@Override
	public String toString()
	{
		return "ActivitySoundsStateChangedEvent{" +
				"isAnySoundPlaying=" + isAnySoundPlaying +
				'}';
	}
}
