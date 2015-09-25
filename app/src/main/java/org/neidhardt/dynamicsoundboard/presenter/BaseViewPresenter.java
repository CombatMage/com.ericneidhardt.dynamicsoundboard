package org.neidhardt.dynamicsoundboard.presenter;

import android.view.View;
import de.greenrobot.event.EventBus;

/**
 * File created by eric.neidhardt on 21.05.2015.
 */
public abstract class BaseViewPresenter<T extends View> implements ViewPresenter<T>
{
	private T view;
	EventBus eventBus;

	public BaseViewPresenter()
	{
		this.eventBus = EventBus.getDefault();
	}

	public EventBus getEventBus()
	{
		return this.eventBus;
	}

	public void setEventBus(EventBus bus)
	{
		this.eventBus = bus;
	}

	@Override
	public T getView()
	{
		return view;
	}

	@Override
	public void setView(T view)
	{
		this.view = view;
	}

	@Override
	public void onAttachedToWindow()
	{
		if (this.eventBus != null && !this.eventBus.isRegistered(this) && this.isEventBusSubscriber())
			this.eventBus.registerSticky(this);
	}

	@Override
	public void onDetachedFromWindow()
	{
		if (this.eventBus != null && this.eventBus.isRegistered(this) && this.isEventBusSubscriber())
			this.eventBus.unregister(this);
	}

	/**
	 * @return true if this presenter should register on eventBus, else false
	 */
	protected abstract boolean isEventBusSubscriber();
}
