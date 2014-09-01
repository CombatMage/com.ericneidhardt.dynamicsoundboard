package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by Eric Neidhardt on 31.08.2014.
 */
public class TabBar extends LinearLayout implements RadioGroup.OnCheckedChangeListener
{
	private RadioGroup tabBar;
	private OnCheckedChangedListener callback;

	public TabBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.view_tab_bar, this, true);

		this.tabBar = (RadioGroup)this.findViewById(R.id.rg_tab_bar);
		this.tabBar.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		View clickedButton = group.findViewById(checkedId);
		int index = this.tabBar.indexOfChild(clickedButton);
		boolean wasUserInteraction = false;

		Object tag = this.tabBar.getTag(R.id.pos);
		Integer lastSelectedItem = tag != null ? (Integer)tag : null;
		if (lastSelectedItem == null)
			wasUserInteraction = true;
		else if (!lastSelectedItem.equals(index))
			wasUserInteraction = true;

		if (this.callback != null)
			this.callback.onCheckedChanged(clickedButton, index, wasUserInteraction);

		this.tabBar.setTag(R.id.pos, index);
	}

	public void setSelection(int position) {
		this.tabBar.setTag(R.id.pos, position);
		((RadioButton)this.tabBar.getChildAt(position)).setChecked(true);
	}

	public void setOnCheckedChangedListener(OnCheckedChangedListener listener) {
		this.callback = listener;
	}

	public static interface OnCheckedChangedListener {
		public void onCheckedChanged(View button, int position, boolean wasUserAction);
	}
}
