package org.neidhardt.dynamicsoundboard.misc;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by eric.neidhardt on 31.03.2015.
 */
public class AnimationUtils
{

	public static Animator createSlowCircularRevealIfAvailable(View view, int startX, int startY, float startRadius, float endRadius)
	{
		Animator animator = createCircularRevealIfAvailable(view, startX, startY, startRadius, endRadius);
		if (animator != null)
			animator.setDuration(view.getResources().getInteger(android.R.integer.config_mediumAnimTime));

		return animator;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static Animator createCircularRevealIfAvailable(View view, int startX, int startY, float startRadius, float endRadius)
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
