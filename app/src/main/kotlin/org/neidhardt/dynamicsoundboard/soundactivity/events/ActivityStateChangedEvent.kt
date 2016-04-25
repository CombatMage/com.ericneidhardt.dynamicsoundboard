package org.neidhardt.dynamicsoundboard.soundactivity.events

/**
 * File created by eric.neidhardt on 17.06.2015.
 */
class ActivityStateChangedEvent(val isActivityResumed: Boolean) {

	val isActivityClosed: Boolean
		get() = !this.isActivityResumed
}
