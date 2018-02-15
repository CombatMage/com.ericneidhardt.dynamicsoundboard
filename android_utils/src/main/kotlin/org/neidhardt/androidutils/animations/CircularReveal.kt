package org.neidhardt.androidutils.animations

import android.animation.Animator
import android.annotation.TargetApi
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import org.neidhardt.androidutils.AndroidVersion

/**
 * Created by eric.neidhardt on 30.11.2016.
 */
object AnimationUtils {

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	fun createCircularReveal(view: View, startX: Int, startY: Int, startRadius: Float, endRadius: Float): Animator? {
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