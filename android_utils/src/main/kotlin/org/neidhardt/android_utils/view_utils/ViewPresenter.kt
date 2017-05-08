package org.neidhardt.android_utils.view_utils

/**
 * @author eric.neidhardt on 11.06.2016.
 */
interface ViewPresenter {
	/**
	 * Usually overridden by implementing classes. References to required view should be passed in this method.
	 */
	fun init() {}

	/**
	 * Notifies the presenter, that corresponding view has been attached to main window.
	 */
	fun start() {}

	/**
	 * Notifies the presenter, that corresponding view has been detached from main window.
	 */
	fun stop() {}
}