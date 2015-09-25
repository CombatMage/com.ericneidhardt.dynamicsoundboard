package org.neidhardt.dynamicsoundboard.soundactivity.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 06.02.2015.
 */
public class IntroductionListItem extends RelativeLayout
{
	private ImageView icon;
	private TextView title;
	private TextView summary;

	public IntroductionListItem(Context context)
	{
		super(context);
		this.init(context);
	}

	public IntroductionListItem(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context);
		this.readAttributes(context, attrs);
	}

	public IntroductionListItem(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context);
		this.readAttributes(context, attrs);
	}

	private void init(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.view_introduction_list_item, this, true);
		this.icon = (ImageView) this.findViewById(R.id.iv_introduction_icon);
		this.title = (TextView) this.findViewById(R.id.tv_introduction_title);
		this.summary = (TextView) this.findViewById(R.id.tv_introduction_summary);
	}

	private void readAttributes(Context context, AttributeSet attributeSet)
	{
		TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.IntroductionListItem, 0, 0);
		Drawable icon = array.getDrawable(R.styleable.IntroductionListItem_action_icon);
		if (icon != null)
			this.icon.setImageDrawable(icon);

		String title = array.getString(R.styleable.IntroductionListItem_action_title);
		if (title != null)
			this.title.setText(title);

		String summary = array.getString(R.styleable.IntroductionListItem_action_summary);
		if (title != null)
			this.summary.setText(summary);

		array.recycle();
	}

}
