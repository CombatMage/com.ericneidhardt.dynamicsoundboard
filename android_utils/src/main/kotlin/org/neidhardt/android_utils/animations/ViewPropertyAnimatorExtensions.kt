package org.neidhardt.android_utils.animations

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.ViewPropertyAnimator

/**
 * Created by eric.neidhardt@gmail.com on 04.09.2017.
 */
fun ViewPropertyAnimator.setOnAnimationEndedListener(listener: (ViewPropertyAnimator) -> Unit) {
	this.setListener(object : AnimatorListenerAdapter() {
		override fun onAnimationEnd(animation: Animator?) {
			super.onAnimationEnd(animation)
			listener.invoke(this@setOnAnimationEndedListener)
		}
	})
}