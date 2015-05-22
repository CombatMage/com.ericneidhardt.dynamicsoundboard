package org.neidhardt.dynamicsoundboard.views.edittext;

import android.content.Context;
import android.util.AttributeSet;
import org.neidhardt.dynamicsoundboard.R;


public class NoUnderscoreEditText extends CustomEditText
{
	public NoUnderscoreEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.view_dialog_edittext;
	}
}
