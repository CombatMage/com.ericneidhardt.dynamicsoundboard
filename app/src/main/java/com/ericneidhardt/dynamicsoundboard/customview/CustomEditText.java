package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ericneidhardt.dynamicsoundboard.R;


public abstract class CustomEditText extends LinearLayout implements TextView.OnEditorActionListener
{
	protected EditTextBackEvent input;
	protected OnTextEditedListener callback;

	public CustomEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.inflateLayout(context, this.getLayoutId());
		this.input.setOnEditorActionListener(this);

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText, 0, 0);
		float size = array.getDimension(R.styleable.CustomEditText_textSize, 0);
		if (array.hasValue(R.styleable.CustomEditText_textSize))
			this.input.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

		int color = array.getColor(R.styleable.CustomEditText_textColor, 0);
		if (array.hasValue(R.styleable.CustomEditText_textColor))
			this.input.setTextColor(color);

		array.recycle();
	}

	public void setOnTextEditedListener(OnTextEditedListener listener)
	{
		this.callback = listener;
	}

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
	{
		if (actionId == EditorInfo.IME_ACTION_DONE)
		{
			if (this.callback != null)
				this.callback.onTextEdited(this.input.getText().toString());
		}
		return false;
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

	public static interface OnTextEditedListener
	{
		public void onTextEdited(String text);
	}

}
