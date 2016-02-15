package org.neidhardt.dynamicsoundboard.views.presenter

import org.greenrobot.eventbus.EventBus

/**
 * File created by eric.neidhardt on 22.05.2015.
 */
interface ViewPresenter<T>
{
	val eventBus: EventBus

	/**
	 * @return true if this presenter should register on eventBus, else false
	 */
	val isEventBusSubscriber: Boolean

	/**
	 * Retrieve reference of controlled ui component from presenter.
	 * @return controlled ui object
	 */
	var view: T

	/**
	 * Notifies the presenter, that corresponding view has been attached to main window. EventBus should be registered in
	 * this step.
	 */
	fun onAttachedToWindow()
	{
		if (this.isEventBusSubscriber && !this.eventBus.isRegistered(this))
			this.eventBus.register(this)
	}

	/**
	 * Notifies the presenter, that corresponding view has been detached from main window. EventBus should be unregistered in this step.
	 */
	fun onDetachedFromWindow()
	{
		this.eventBus.unregister(this)
	}

}
