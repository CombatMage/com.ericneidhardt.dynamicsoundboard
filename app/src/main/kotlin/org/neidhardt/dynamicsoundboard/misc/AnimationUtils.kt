package org.neidhardt.dynamicsoundboard.misc

import android.animation.Animator
import android.annotation.TargetApi
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import org.neidhardt.utils.AndroidVersion

/**
 * File created by eric.neidhardt on 31.03.2015.
 */
object AnimationUtils
{

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	fun createCircularReveal(view: View, startX: Int, startY: Int, startRadius: Float, endRadius: Float): Animator?
	{
		if (AndroidVersion.IS_LOLLIPOP_AVAILABLE) {
			return ViewAnimationUtils.createCircularReveal(view,
					startX,
					startY,
					startRadius,
					endRadius)
		}
		else return null
	}

}
