package org.neidhardt.dynamicsoundboard.views.binding_adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.databinding.BindingAdapter;
import android.view.View;

/**
 * @author Eric.Neidhardt@GMail.com on 20.06.2016.
 */
public class NavigationDrawerButtonBarAdapter {

	@BindingAdapter("animateVisibleRotateX")
	public static void rotateXView(final View view, Boolean rotateUp) {
		view.animate().cancel();
		if (!rotateUp) {
			view.setRotationX(0f);
			view.animate().withLayer().rotationX(1f).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					view.setRotationX(180f);
				}
			});
		} else {
			view.setRotationX(180f);
			view.animate().withLayer().rotationX(0f).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					view.setRotationX(0f);
				}
			});
		}
	}

	@BindingAdapter("animateVisibleSlide")
	public static void slideView(final View view, Boolean visible) {
		view.animate().cancel();
		if (visible) {
			view.setTranslationX(-1f * view.getWidth());
			view.animate().withLayer().translationX(0f).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					view.setTranslationX(0f);
				}
			});
		}
		else {
			view.setTranslationX(0f);
			view.animate().withLayer().translationX(-1f * view.getWidth()).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					view.setTranslationX(-1f * view.getWidth());
				}
			});
		}
	}

	@BindingAdapter("animateVisibleFade")
	public static void fadeView(final View view, Boolean visible) {
		view.animate().cancel();
		if (visible) {
			view.setAlpha(0f);
			view.animate().withLayer().alpha(1f).setDuration(400).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					view.setAlpha(1f);
				}
			});
		}
		else {
			view.setAlpha(1f);
			view.animate().withLayer().alpha(0f).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					view.setAlpha(0f);
				}
			});
		}
	}
}
