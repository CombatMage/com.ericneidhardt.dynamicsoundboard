package org.neidhardt.dynamicsoundboard.misc.longtermtask.events;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class LongTermTaskStateChangedEvent
{
	private final boolean isLongTermTaskInProgress;
	private final int nrOngoingTasks;

	public LongTermTaskStateChangedEvent(boolean isLongTermTaskInProgress, int nrOngoingTasks)
	{
		this.isLongTermTaskInProgress = isLongTermTaskInProgress;
		this.nrOngoingTasks = nrOngoingTasks;
	}

	public boolean isLongTermTaskInProgress()
	{
		return isLongTermTaskInProgress;
	}

	public int getNrOngoingTasks()
	{
		return nrOngoingTasks;
	}

	@Override
	public String toString() {
		return "LongTermTaskStateChangedEvent{" +
				"isLongTermTaskInProgress=" + isLongTermTaskInProgress() +
				", nrOngoingTasks=" + getNrOngoingTasks() +
				'}';
	}
}
