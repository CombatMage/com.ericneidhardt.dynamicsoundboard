package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 02.09.2014.
 */
public class ActionbarEditText extends CustomEditText implements TextView.OnEditorActionListener
{
	private View divider;
	private OnTextEditedListener callback;

	public ActionbarEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		super.input.setOnEditorActionListener(this);
		this.divider = this.findViewById(R.id.v_divider);
		this.divider.setVisibility(INVISIBLE);
	}

	public void setOnTextEditedListener(OnTextEditedListener listener)
	{
		this.callback = listener;
	}

	@Override
	public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
	{
		if (this.callback != null)
			this.callback.onTextEdited(super.input.getText().toString());
		return false;
	}

	public static interface OnTextEditedListener
	{
		public void onTextEdited(String text);
	}
}
