package org.neidhardt.dynamicsoundboard.customview;

import android.view.View;
import de.greenrobot.event.EventBus;

/**
 * Created by eric.neidhardt on 21.05.2015.
 */
public abstract class BaseViewPresenter<T extends View>
{
	private T view;
	EventBus bus;

	public T getView()
	{
		return view;
	}

	public void setView(T view)
	{
		this.view = view;
	}

	public EventBus getBus()
	{
		return bus;
	}

	public void setBus(EventBus bus)
	{
		this.bus = bus;
	}

	public void onAttachToWindow()
	{
		if (this.bus != null && !this.bus.isRegistered(this))
			this.bus.registerSticky(this);
	}

	public void onDetachedFromWindow()
	{
		if (this.bus != null && this.bus.isRegistered(this))
			this.bus.unregister(this);
	}
}
