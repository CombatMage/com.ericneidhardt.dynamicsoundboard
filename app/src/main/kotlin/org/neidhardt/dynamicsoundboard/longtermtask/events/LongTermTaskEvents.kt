package org.neidhardt.dynamicsoundboard.longtermtask.events

/**
 * File created by eric.neidhardt on 28.04.2015.
 */
data class LongTermTaskStateChangedEvent
(
	val isLongTermTaskInProgress: Boolean,
	val nrOngoingTasks: Int
)

interface LongTermTaskStateChangedEventListener
{
    /**
     * This is called by greenRobot EventBus in case a background task starts or finishes his execution
     * @param event delivered LongTermTaskStateChangedEvent
     */
	fun onEventMainThread(event: LongTermTaskStateChangedEvent)
}