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
public abstract class CustomEditText extends LinearLayout
{
	protected EditTextBackEvent input;

	public CustomEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.inflateLayout(context, this.getLayoutId());
	}



	protected abstract int getLayoutId();

	protected void inflateLayout(Context context, int layoutToInflate)
	{
		LayoutInflater.from(context).inflate(layoutToInflate, this, true);
		this.input = (EditTextBackEvent) this.findViewById(R.id.edittext);
	}

	public void setText(String text)
	{
		if (this.input != null)
			this.input.setText(text);
	}

	public Editable getText()
	{
		if (this.input != null)
			return this.input.getText();
		return null;
	}

	public String getDisplayedText()
	{
		String userInput = this.getText().toString();
		if (userInput != null && !userInput.isEmpty())
			return userInput;
		return this.getHint().toString();
	}

	public void setHint(String hint)
	{
		if (this.input != null)
			this.input.setHint(hint);
	}

	public CharSequence getHint()
	{
		if (this.input != null)
			return this.input.getHint();
		return null;
	}

}
