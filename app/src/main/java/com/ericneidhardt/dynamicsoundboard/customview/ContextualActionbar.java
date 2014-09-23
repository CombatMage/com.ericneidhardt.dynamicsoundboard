package com.ericneidhardt.dynamicsoundboard.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ericneidhardt.dynamicsoundboard.R;


public class ContextualActionbar extends RelativeLayout{

	private TextView numberOfSelectedItems;
	private View delete;

	@SuppressWarnings("unused")
	public ContextualActionbar(Context context)
	{
		super(context);
		this.inflate();
	}

	@SuppressWarnings("unused")
	public ContextualActionbar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.inflate();
	}

	@SuppressWarnings("unused")
	public ContextualActionbar(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.inflate();
	}

	private void inflate()
	{
		LayoutInflater.from(this.getContext()).inflate(R.layout.contextual_actionbar, this, true);
		this.numberOfSelectedItems = (TextView)this.findViewById(R.id.tv_number_selected_items);
		this.delete = this.findViewById(R.id.action_delete_selected);
	}

	public void setDeleteAction(View.OnClickListener listener)
	{
		this.delete.setOnClickListener(listener);
	}

	public void setSelectAllAction(View.OnClickListener listener)
	{
		this.numberOfSelectedItems.setOnClickListener(listener);
	}

	public void setNumberOfSelectedItems(int count, int max)
	{
		String countString = Integer.toString(count);
		if (countString.length() == 1)
			countString = " " + countString;

		this.numberOfSelectedItems.setText(countString + "/" + max);
	}

}
