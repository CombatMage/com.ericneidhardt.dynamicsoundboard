package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by eric.neidhardt on 08.09.2014.
 */
public class EditTextBackEvent extends EditText
{
	private EditTextImeBackListener onImeBackListener;

	public EditTextBackEvent(Context context)
	{
		super(context);
	}

	public EditTextBackEvent(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public EditTextBackEvent(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public void setOnEditTextImeBackListener(EditTextImeBackListener listener)
	{
		this.onImeBackListener = listener;
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event)
	{
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
		{
			if (onImeBackListener != null)
				onImeBackListener.onImeBack(this, this.getText().toString());
		}
		return super.onKeyPreIme(keyCode, event);
	}

	public interface EditTextImeBackListener {
		public abstract void onImeBack(EditTextBackEvent ctrl, String text);
	}
}


