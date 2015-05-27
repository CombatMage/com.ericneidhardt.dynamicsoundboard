package org.neidhardt.dynamicsoundboard.presenter;

import android.view.View;
import de.greenrobot.event.EventBus;

/**
 * Created by eric.neidhardt on 21.05.2015.
 */
public abstract class BaseViewPresenter<T extends View> implements ViewPresenter<T>
{
	private T view;
	EventBus bus;

	public BaseViewPresenter()
	{
		this.bus = EventBus.getDefault();
	}

	public EventBus getBus()
	{
		return this.bus;
	}

	public void setBus(EventBus bus)
	{
		this.bus = bus;
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
		if (this.bus != null && !this.bus.isRegistered(this) && this.isEventBusSubscriber())
			this.bus.registerSticky(this);
	}

	@Override
	public void onDetachedFromWindow()
	{
		if (this.bus != null && this.bus.isRegistered(this) && this.isEventBusSubscriber())
			this.bus.unregister(this);
	}

	/**
	 * @return true if this presenter should register on eventBus, else fals
	 */
	protected abstract boolean isEventBusSubscriber();
}
