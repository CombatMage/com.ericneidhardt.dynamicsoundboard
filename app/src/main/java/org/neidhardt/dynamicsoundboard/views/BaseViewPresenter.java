package org.neidhardt.dynamicsoundboard.views;

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
	public void onAttachToWindow()
	{
		if (this.bus != null && !this.bus.isRegistered(this))
			this.bus.registerSticky(this);
	}

	@Override
	public void onDetachedFromWindow()
	{
		if (this.bus != null && this.bus.isRegistered(this))
			this.bus.unregister(this);
	}
}
