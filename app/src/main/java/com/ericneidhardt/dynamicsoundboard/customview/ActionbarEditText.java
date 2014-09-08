package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 02.09.2014.
 */
public class ActionbarEditText extends CustomEditText implements TextView.OnEditorActionListener, View.OnClickListener
{
	private View divider;
	private OnTextEditedListener callback;
	private KeyListener editTextKeyListener;

	public ActionbarEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		super.input.setOnEditorActionListener(this);
		super.input.setOnClickListener(this);

		this.divider = this.findViewById(R.id.v_divider);
		this.disableEditText();
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.view_actionbar_edittext;
	}

	public void setOnTextEditedListener(OnTextEditedListener listener)
	{
		this.callback = listener;
	}

	@Override
	public void onClick(View v)
	{
		this.enableEditText();
		super.input.setText("");
	}

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
	{
		if (actionId == EditorInfo.IME_ACTION_DONE)
		{
			if (this.callback != null)
				this.callback.onTextEdited(super.input.getText().toString());
			this.disableEditText();
		}
		return false;
	}

	private void disableEditText()
	{
		this.divider.setVisibility(INVISIBLE);

		this.editTextKeyListener = super.input.getKeyListener();
		super.input.setCursorVisible(false);
		super.input.setKeyListener(null);
	}

	private void enableEditText()
	{
		this.divider.setVisibility(VISIBLE);

		super.input.setKeyListener(this.editTextKeyListener);
		super.input.setCursorVisible(true);
		InputMethodManager lManager = (InputMethodManager)this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		lManager.showSoftInput(super.input, 0);
	}

	public static interface OnTextEditedListener
	{
		public void onTextEdited(String text);
	}
}
