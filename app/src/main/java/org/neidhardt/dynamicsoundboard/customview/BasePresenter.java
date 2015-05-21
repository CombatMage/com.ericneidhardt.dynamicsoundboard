package org.neidhardt.dynamicsoundboard.customview;

import android.view.View;

/**
 * Created by eric.neidhardt on 21.05.2015.
 */
public abstract class BasePresenter<T extends View>
{

	private T view;

	public T getView()
	{
		return view;
	}

	public void setView(T view)
	{
		this.view = view;
	}
}
