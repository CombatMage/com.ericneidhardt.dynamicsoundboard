package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

/**
 * Created by eric.neidhardt on 05.09.2014.
 */
public class CustomSpinner extends Spinner
{
	public CustomSpinner(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void setItems(List<String> items)
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.setAdapter(adapter);
	}

}
