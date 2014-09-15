package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;

import com.ericneidhardt.dynamicsoundboard.R;


public class DialogEditText extends CustomEditText
{
	public DialogEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.view_dialog_edittext;
	}
}
