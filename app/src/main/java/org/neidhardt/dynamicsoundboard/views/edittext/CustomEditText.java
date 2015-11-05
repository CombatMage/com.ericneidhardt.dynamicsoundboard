package org.neidhardt.dynamicsoundboard.views.edittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.misc.Logger;


public abstract class CustomEditText
		extends
			LinearLayout
		implements
			TextView.OnEditorActionListener,
			EditTextBackEvent.EditTextImeBackListener,
			View.OnFocusChangeListener
{
	private static final String TAG = CustomEditText.class.getName();

	EditTextBackEvent input;
	OnTextEditedListener onTextEditedListener;
	private OnFocusChangeListener onFocusChangeListener;

	public CustomEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.inflateLayout(context, this.getLayoutId());
		this.input.setOnEditorActionListener(this);
		this.input.setOnEditTextImeBackListener(this);
		this.input.setOnFocusChangeListener(this);

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText, 0, 0);
		float size = array.getDimension(R.styleable.CustomEditText_text_size, 0);
		if (array.hasValue(R.styleable.CustomEditText_text_size))
			this.input.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

		int color = array.getColor(R.styleable.CustomEditText_text_color, 0);
		if (array.hasValue(R.styleable.CustomEditText_text_color))
			this.input.setTextColor(color);

		array.recycle();
	}

	public void setOnTextEditedListener(OnTextEditedListener listener)
	{
		this.onTextEditedListener = listener;
	}

	@SuppressWarnings("unused")
	public void addTextChangedListener(TextWatcher listener)
	{
		this.input.addTextChangedListener(listener);
	}

	@Override
	public boolean hasFocus()
	{
		return this.input.hasFocus();
	}

	@Override
	public void clearFocus()
	{
		this.input.clearFocus();
	}

	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener)
	{
		this.onFocusChangeListener = onFocusChangeListener;
	}

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
	{
		if (actionId == EditorInfo.IME_ACTION_DONE)
		{
			if (this.onTextEditedListener != null)
				this.onTextEditedListener.onTextEdited(this.input.getText().toString());
		}
		return false;
	}

	@Override
	public void onImeBack(EditTextBackEvent ctrl, String text)
	{
		if (this.onTextEditedListener != null)
			this.onTextEditedListener.onTextEdited(this.input.getText().toString());
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus)
	{
		if (this.onTextEditedListener != null && !hasFocus)
			this.onTextEditedListener.onTextEdited(this.input.getText().toString());
		if (this.onFocusChangeListener != null)
			this.onFocusChangeListener.onFocusChange(this, hasFocus);
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

	@SuppressWarnings("unused")
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

	@NonNull
	@Override
	protected Parcelable onSaveInstanceState()
	{
		Logger.d(TAG, "onSaveInstanceState");
		Parcelable superState = super.onSaveInstanceState();
		return new SavedCustomEditTextState(superState, this.input.getText().toString());
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		Logger.d(TAG, "onRestoreInstanceState " + state);
		SavedCustomEditTextState savedState = (SavedCustomEditTextState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		if (savedState.getValue() != null)
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

	public static class SavedCustomEditTextState extends BaseSavedState
	{
		private String value;

		public String getValue()
		{
			return this.value;
		}

		public SavedCustomEditTextState(Parcelable superState, String value)
		{
			super(superState);
			this.value = value;
		}

		private SavedCustomEditTextState(Parcel superState)
		{
			super(superState);
			this.value = superState.readString();
		}

		@Override
		public void writeToParcel(@NonNull Parcel destination, int flags)
		{
			super.writeToParcel(destination, flags);
			destination.writeString(this.value);
		}

		public static final Parcelable.Creator<SavedCustomEditTextState> CREATOR = new
				Parcelable.Creator<SavedCustomEditTextState>()
				{
					public SavedCustomEditTextState createFromParcel(@NonNull Parcel in)
					{
						return new SavedCustomEditTextState(in);
					}

					@NonNull
					public SavedCustomEditTextState[] newArray(int size)
					{
						return new SavedCustomEditTextState[size];
					}
				};
	}

	public interface OnTextEditedListener
	{
		void onTextEdited(String text);
	}

}
