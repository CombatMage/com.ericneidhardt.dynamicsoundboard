package org.neidhardt.dynamicsoundboard.navigationdrawer.header.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
public class NavigationDrawerHeader extends FrameLayout implements View.OnClickListener
{

	private NavigationDrawerHeaderPresenter presenter = new NavigationDrawerHeaderPresenter(DynamicSoundboardApplication.getStorage().getSoundLayoutsAccess());

	private TextView currentLayoutName;
	private View indicator;

	@SuppressWarnings("unused")
	public NavigationDrawerHeader(Context context)
	{
		super(context);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public NavigationDrawerHeader(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init(context);
	}

	@SuppressWarnings("unused")
	public NavigationDrawerHeader(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		this.init(context);
	}

	private void init(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.navigation_drawer_header, this, true);
		this.currentLayoutName = (TextView) this.findViewById(R.id.tv_current_sound_layout_name);
		this.indicator = this.findViewById(R.id.iv_change_sound_layout_indicator);

		this.findViewById(R.id.layout_change_sound_layout).setOnClickListener(this);
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		this.presenter.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow()
	{
		this.presenter.onDetachedFromWindow();
		super.onDetachedFromWindow();
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		this.presenter.setView(this);
	}

	@Override
	public void onClick(@NonNull View v)
	{
		this.presenter.onChangeLayoutClicked();
	}

	void showCurrentLayoutName(String name)
	{
		this.currentLayoutName.setText(name);
	}

	void animateLayoutChanges()
	{
		this.indicator.animate()
				.rotationXBy(180)
				.setDuration(this.getResources().getInteger(android.R.integer.config_shortAnimTime))
				.start();
	}
}
