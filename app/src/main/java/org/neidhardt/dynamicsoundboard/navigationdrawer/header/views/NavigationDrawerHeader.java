package org.neidhardt.dynamicsoundboard.navigationdrawer.header.views;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

/**
 * Created by eric.neidhardt on 27.05.2015.
 */
public class NavigationDrawerHeader extends FrameLayout implements View.OnClickListener, Animator.AnimatorListener
{
	private NavigationDrawerHeaderPresenter presenter;

	private View avatarLayout;
	private View headerAvatar;
	private TextView currentSoundCount;

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
		this.presenter = new NavigationDrawerHeaderPresenter();

		LayoutInflater.from(context).inflate(R.layout.navigation_drawer_header, this, true);
		this.currentLayoutName = (TextView) this.findViewById(R.id.tv_current_sound_layout_name);
		this.currentSoundCount = (TextView) this.findViewById(R.id.tv_header_active_sound_counter);
		this.headerAvatar = this.findViewById(R.id.iv_header_avatar);
		this.avatarLayout = this.findViewById(R.id.layout_header_avatar);
		this.indicator = this.findViewById(R.id.iv_change_sound_layout_indicator);

		this.findViewById(R.id.layout_change_sound_layout).setOnClickListener(this);
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		this.presenter.setSoundDataModel(ServiceManagerFragment.getSoundDataModel());
		this.presenter.setSoundLayoutModel(SoundLayoutsManager.getInstance());
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
	public void onClick(View v)
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

	void setCurrentSoundCount(int count)
	{
		if (count > 0)
		{
			this.currentSoundCount.setText(Integer.toString(count));
			this.currentSoundCount.setVisibility(VISIBLE);
			this.headerAvatar.setVisibility(INVISIBLE);
		}
		else
		{
			this.headerAvatar.setVisibility(VISIBLE);
			this.currentSoundCount.setVisibility(INVISIBLE);
		}
	}

	void animateHeaderAvatarRotate()
	{
		this.avatarLayout.animate()
				.rotationYBy(360)
				.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime)).setListener(this)
				.start();
	}

	@Override
	public void onAnimationStart(Animator animation) {}

	@Override
	public void onAnimationEnd(Animator animation)
	{
		this.avatarLayout.setRotationX(0);
	}

	@Override
	public void onAnimationCancel(Animator animation)
	{
		this.avatarLayout.setRotationX(0);
	}

	@Override
	public void onAnimationRepeat(Animator animation) {}
}
