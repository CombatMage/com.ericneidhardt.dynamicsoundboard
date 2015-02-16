package org.neidhardt.dynamicsoundboard.customview.edittext;

import android.content.Context;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;


public class ActionbarEditText
		extends
			CustomEditText
		implements
			TextView.OnEditorActionListener,
			View.OnClickListener,
			EditTextBackEvent.EditTextImeBackListener
{
	private View divider;
	private KeyListener editTextKeyListener;

	private String initialText;

	public ActionbarEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		super.input.setOnEditorActionListener(this);
		super.input.setOnClickListener(this);

		super.input.setOnEditTextImeBackListener(this);

		this.divider = this.findViewById(R.id.v_divider);
		this.disableEditText();
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.view_actionbar_edittext;
	}

	@Override
	public void onClick(View v)
	{
		this.enableEditText();
		this.initialText = super.input.getText().toString();
	}

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
	{
		if (actionId == EditorInfo.IME_ACTION_DONE)
		{
			if (this.onTextEditedListener != null)
				this.onTextEditedListener.onTextEdited(super.input.getText().toString());
			this.disableEditText();
		}
		return false;
	}

	@Override
	public void onImeBack(EditTextBackEvent ctrl, String text)
	{
		if (this.initialText != null)
			super.input.setText(this.initialText);
		this.disableEditText();
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

}
