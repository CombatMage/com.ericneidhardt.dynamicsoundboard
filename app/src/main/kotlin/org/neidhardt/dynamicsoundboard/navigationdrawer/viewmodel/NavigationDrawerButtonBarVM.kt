package org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel

import android.animation.Animator
import android.animation.ValueAnimator
import android.databinding.BaseObservable
import android.view.animation.DecelerateInterpolator

/**
 * Created by Eric.Neidhardt@GMail.com on 17.06.2016.
 */
private val ANIMATION_TIME_BUTTON: Long = 400
private val INTERPOLATOR = DecelerateInterpolator()

class NavigationDrawerButtonBarVM : BaseObservable() {

	var deleteSelectedTranslationX: Float = 0f
		set(value) {
			field = value
			this.notifyChange()
		}

	var enableDeleteSelected: Boolean = false
		set(value) {
			field = value
			this.notifyChange()

			if (value == true) {

				ValueAnimator.ofFloat(-width, 0).let {
					it.duration = ANIMATION_TIME_BUTTON
					it.addUpdateListener { this.deleteSelectedTranslationX = it.animatedValue as Float })
					it.interpolator = INTERPOLATOR
					it.start()
				}
			}
		}

	var onDeleteClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onDeleteClicked() {
		this.onDeleteClicked.invoke()
	}

	var onAddClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onAddClicked() {
		this.onAddClicked.invoke()
	}

	var onDeleteSelectedClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onDeleteSelectedClicked() {
		this.onDeleteSelectedClicked.invoke()
	}
}