package org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.databinding.BaseObservable
import android.databinding.BindingAdapter
import android.view.View

/**
* @author Eric.Neidhardt@GMail.com on 17.06.2016.
*/
//private val INTERPOLATOR = FastOutSlowInInterpolator()

class NavigationDrawerButtonBarVM : BaseObservable() {

	companion object {
		@BindingAdapter("animateVisibleSlide")
		@JvmStatic
		fun slideView(view: View, visible: Boolean) {
			view.animate().cancel();
			if (visible) {
				view.visibility = View.VISIBLE;
				view.translationX = -1 * view.width.toFloat()
				view.animate().withLayer().translationX(0f).setListener(object : AnimatorListenerAdapter() {
					override fun onAnimationEnd(animation: Animator) {
						view.translationX = 0f
					}
				})
			}
			else {
				view.visibility = View.VISIBLE;
				view.translationX = 0f
				view.animate().withLayer().translationX(-1 * view.width.toFloat()).setListener(object : AnimatorListenerAdapter() {
					override fun onAnimationEnd(animation: Animator) {
						view.visibility = View.GONE;
					}
				})
			}
		}

		@BindingAdapter("animateVisibleFade")
		@JvmStatic
		fun fadeView(view: View, visible: Boolean) {
			view.animate().cancel();
			if (visible) {
				view.visibility = View.VISIBLE;
				view.alpha = 0f
				view.animate().withLayer().alpha(1f).setListener(object : AnimatorListenerAdapter() {
					override fun onAnimationEnd(animation: Animator) {
						view.alpha = 1f
					}
				})
			}
			else {
				view.visibility = View.VISIBLE;
				view.alpha = 1f
				view.animate().withLayer().translationX(0f).setListener(object : AnimatorListenerAdapter() {
					override fun onAnimationEnd(animation: Animator) {
						view.alpha = 0f
						view.visibility = View.GONE;
					}
				})
			}
		}
	}

	var enableDeleteSelected: Boolean = false
		set(value) {
			field = value
			this.notifyChange()
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