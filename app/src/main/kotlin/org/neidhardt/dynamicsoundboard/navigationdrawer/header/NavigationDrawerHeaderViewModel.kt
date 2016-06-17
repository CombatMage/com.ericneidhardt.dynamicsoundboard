package org.neidhardt.dynamicsoundboard.navigationdrawer.header

import android.animation.Animator
import android.animation.ValueAnimator
import android.databinding.BaseObservable
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent

/**
* Created by Eric.Neidhardt@GMail.com on 17.06.2016.
*/
private val ANIMATION_TIME_ARROW: Long = 400

class NavigationDrawerHeaderViewModel(
		private val eventBus: EventBus,
		title: String?
) : BaseObservable() {

	var title: String? = title
		set(value) {
			field = value
			this.notifyChange()
		}

	var indicatorRotation: Int = 0
		set(value) {
			field = value
			this.notifyChange()
		}

	var openSoundLayouts = true
		set(value) {
			field = value
			this.indicatorRotation = if (value) 0 else 180
		}

	fun onChangeLayoutClicked() {

		this.eventBus.post(OpenSoundLayoutsRequestedEvent(!openSoundLayouts))

		val animator =
				if (this.openSoundLayouts)
					ValueAnimator.ofInt(0, 180)
				else
					ValueAnimator.ofInt(180, 0)

		animator.let {
			it.duration = ANIMATION_TIME_ARROW
			it.addUpdateListener { this.indicatorRotation = it.animatedValue as Int }
			it.addListener(object : Animator.AnimatorListener {

				override fun onAnimationEnd(animation: Animator?) {
					openSoundLayouts = !openSoundLayouts
				}

				override fun onAnimationCancel(animation: Animator?) {
					openSoundLayouts = !openSoundLayouts
				}

				override fun onAnimationRepeat(animation: Animator?) {}
				override fun onAnimationStart(animation: Animator?) {}
			})
			it.start()
		}
	}
}