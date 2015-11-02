package org.neidhardt.dynamicsoundboard.misc;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by eric.neidhardt on 31.03.2015.
 */
public class AnimationUtils
{
	private static final String TAG = AnimationUtils.class.getName();

	public static Animator createSlowCircularReveal(final View view, int startX, int startY, float startRadius, float endRadius)
	{
		if (!Util.IS_LOLLIPOP_AVAILABLE)
			return null;

		int animationDuration = view.getResources().getInteger(android.R.integer.config_longAnimTime);
		AnimatorSet animatorSet = new AnimatorSet();

		Animator revealAnimator = createCircularReveal(view, startX, startY, startRadius, endRadius);
		if (revealAnimator == null)
			return null;

		revealAnimator.setDuration(animationDuration);

		view.setAlpha(1);
		ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 0f);
		fadeOut.setDuration(animationDuration);

		animatorSet.play(revealAnimator).with(fadeOut);
		animatorSet.addListener(new Animator.AnimatorListener()
		{
			@Override
			public void onAnimationStart(Animator animation)
			{
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation)
			{
				view.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(Animator animation)
			{
				view.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}
		});

		return animatorSet;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static Animator createCircularReveal(View view, int startX, int startY, float startRadius, float endRadius)
	{
		if (!Util.IS_LOLLIPOP_AVAILABLE)
			return null;

		return ViewAnimationUtils.createCircularReveal(view,
				startX,
				startY,
				startRadius,
				endRadius);
	}

}
