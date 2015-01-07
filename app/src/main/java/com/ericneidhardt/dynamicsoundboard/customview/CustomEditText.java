package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ericneidhardt.dynamicsoundboard.R;


public abstract class CustomEditText
		extends
			LinearLayout
		implements
			TextView.OnEditorActionListener,
			EditTextBackEvent.EditTextImeBackListener,
			View.OnFocusChangeListener
{
	EditTextBackEvent input;
	OnTextEditedListener callback;

	public CustomEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.inflateLayout(context, this.getLayoutId());
		this.input.setOnEditorActionListener(this);
		this.input.setOnEditTextImeBackListener(this);
		this.input.setOnFocusChangeListener(this);

		View divider = this.findViewById(R.id.v_divider);

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText, 0, 0);
		float size = array.getDimension(R.styleable.CustomEditText_text_size, 0);
		if (array.hasValue(R.styleable.CustomEditText_text_size))
			this.input.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

		int color = array.getColor(R.styleable.CustomEditText_text_color, 0);
		if (array.hasValue(R.styleable.CustomEditText_text_color))
			this.input.setTextColor(color);

		boolean showUnderScore = array.getBoolean(R.styleable.CustomEditText_show_underscore, true);
		if (!showUnderScore)
			divider.setVisibility(GONE);

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

	@Override
	public void onImeBack(EditTextBackEvent ctrl, String text)
	{
		if (this.callback != null)
			this.callback.onTextEdited(this.input.getText().toString());
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus)
	{
		if (this.callback != null)
			this.callback.onTextEdited(this.input.getText().toString());
	}

	protected abstract int getLayoutId();

	void inflateLayout(Context context, int layoutToInflate)
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
		if (!userInput.isEmpty())
			return userInput;
		return this.getHint().toString();
	}

	public void setHint(String hint)
	{
		if (this.input != null)
			this.input.setHint(hint);
	}

	CharSequence getHint()
	{
		if (this.input != null)
			return this.input.getHint();
		return null;
	}

	@Override
	protected Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		return new SavedState(superState, this.input.getText().toString());
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		this.input.setText(savedState.getValue());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void dispatchSaveInstanceState(@NonNull SparseArray container)
	{
		// As we save our own instance state, ensure our children don't save
		// and restore their state as well.
		super.dispatchFreezeSelfOnly(container);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void dispatchRestoreInstanceState(@NonNull SparseArray container)
	{
		/** See comment in {@link #dispatchSaveInstanceState(android.util.SparseArray)} */
		super.dispatchThawSelfOnly(container);
	}

	protected static class SavedState extends BaseSavedState
	{
		private String value;

		public String getValue()
		{
			return this.value;
		}

		public SavedState(Parcelable superState, String value)
		{
			super(superState);
			this.value = value;
		}

		@Override
		public void writeToParcel(@NonNull Parcel destination, int flags)
		{
			super.writeToParcel(destination, flags);
			destination.writeString(this.value);
		}
	}

	public static interface OnTextEditedListener
	{
		public void onTextEdited(String text);
	}

}
