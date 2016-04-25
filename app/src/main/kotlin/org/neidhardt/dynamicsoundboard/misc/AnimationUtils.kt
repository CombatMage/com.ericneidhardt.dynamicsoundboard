package org.neidhardt.dynamicsoundboard.misc

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils

/**
 * File created by eric.neidhardt on 31.03.2015.
 */
object AnimationUtils
{

	fun createSlowCircularReveal(view: View, startX: Int, startY: Int, startRadius: Float, endRadius: Float): Animator?
	{
		if (!AndroidVersion.IS_LOLLIPOP_AVAILABLE)
			return null

		val animationDuration = view.resources.getInteger(android.R.integer.config_longAnimTime)
		val animatorSet = AnimatorSet()

		val revealAnimator = createCircularReveal(view, startX, startY, startRadius, endRadius) ?: return null

		revealAnimator.duration = animationDuration.toLong()

		view.alpha = 1f
		val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 0f)
		fadeOut.duration = animationDuration.toLong()

		animatorSet.play(revealAnimator).with(fadeOut)
		animatorSet.addListener(object : Animator.AnimatorListener {
			override fun onAnimationStart(animation: Animator) {
				view.visibility = View.VISIBLE
			}

			override fun onAnimationEnd(animation: Animator) {
				view.visibility = View.GONE
			}

			override fun onAnimationCancel(animation: Animator) {
				view.visibility = View.GONE
			}

			override fun onAnimationRepeat(animation: Animator) {
			}
		})

		return animatorSet
	}

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
