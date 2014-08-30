package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 29.08.2014.
 */
public class CustomEditText extends LinearLayout
{
	private EditText input;

	public CustomEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.inflateLayout(context);
	}

	private void inflateLayout(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.view_edittext, this, true);
		this.input = (EditText) this.findViewById(R.id.et_input);
	}

	public void setText(String text)
	{
		if (this.input != null)
			this.input.setText(text);
	}

	public Editable getText()
	{
		if (this.input == null)
			return null;
		return this.input.getText();
	}
}
