package org.neidhardt.dynamicsoundboard.navigationdrawer.header

import android.animation.ValueAnimator
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View

/**
* Created by Eric.Neidhardt@GMail.com on 17.06.2016.
*/
class NavigationDrawerHeaderViewModel : BaseObservable() {

	var title: String? = null

	@Bindable
	var indicatorRotation: Int = 0
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onChangeLayoutClicked(view: View) {
		val animator = ValueAnimator.ofInt(0, 180)
		animator.duration = 10000
		animator.addUpdateListener { this.indicatorRotation = animator.animatedValue as Int }
		animator.start()

	}
}