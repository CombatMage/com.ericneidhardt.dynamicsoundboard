package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 02.09.2014.
 */
public class ActionbarEditText extends CustomEditText
{
	private View divider

	public ActionbarEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.divider = this.findViewById(R.id.v_divider);
	}
}
