package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.ericneidhardt.dynamicsoundboard.R;

import java.util.List;


public class CustomSpinner extends LinearLayout implements View.OnClickListener
{
	private Spinner spinner;

	public CustomSpinner(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.view_spinner, this, true);
		this.spinner = (Spinner)this.findViewById(R.id.spinner);
		this.setOnClickListener(this);
	}

	public void setItems(List<String> items)
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.spinner.setAdapter(adapter);
	}

	public int getSelectedItemPosition()
	{
		return this.spinner.getSelectedItemPosition();
	}

	@Override
	public void onClick(View v)
	{
		this.spinner.performClick();
	}
}
