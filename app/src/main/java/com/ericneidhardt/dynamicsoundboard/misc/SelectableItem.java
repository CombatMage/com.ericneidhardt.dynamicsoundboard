package com.ericneidhardt.dynamicsoundboard.misc;

public class SelectableItem<T>
{
	private boolean isSelected = false;
	private T data;

	public boolean isSelected()
	{
		return isSelected;
	}

	public void setSelected(boolean isSelected)
	{
		this.isSelected = isSelected;
	}

	public T getData()
	{
		return data;
	}

	public SelectableItem(T data)
	{
		this.data = data;
	}
}
