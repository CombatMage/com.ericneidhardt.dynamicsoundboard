package org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.databinding.BindingAdapter;
import android.view.View;

/**
 * Created by Eric.Neidhardt@GMail.com on 20.06.2016.
 */
public class NavigationDrawerButtonBarAdapter {

	@BindingAdapter("animateVisibleSlide")
	public static void slideView(final View view, Boolean visible) {
		view.animate().cancel();
		if (visible) {
			view.setVisibility(View.VISIBLE);
			view.setTranslationX(-1f * view.getWidth());
			view.animate().withLayer().translationX(0f).setListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animation) {
					view.setTranslationX(0f);
				}
			});
		}
		/*else {
			view.visibility = View.VISIBLE;
			view.translationX = 0f
			view.animate().withLayer().translationX(-1 * view.width.toFloat()).setListener(object : AnimatorListenerAdapter() {
				override fun onAnimationEnd(animation: Animator) {
					view.visibility = View.GONE;
				}
			})
		}*/
	}

	@BindingAdapter("animateVisibleFade")
	public static void fadeView(final View view, Boolean visible) {
		/*view.animate().cancel();
		if (visible) {
			view.visibility = View.VISIBLE;
			view.alpha = 0f
			view.animate().withLayer().alpha(1f).setDuration(400).setListener(object : AnimatorListenerAdapter() {
				override fun onAnimationEnd(animation: Animator) {
					view.alpha = 1f
				}
			})
		}
		else {
			view.visibility = View.VISIBLE;
			view.alpha = 1f
			view.animate().withLayer().alpha(0f).setListener(object : AnimatorListenerAdapter() {
				override fun onAnimationEnd(animation: Animator) {
					view.alpha = 0f
					view.visibility = View.GONE;
				}
			})
		}*/
	}
}
