package org.neidhardt.dynamicsoundboard.splashactivity

import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SplashActivityPresenter(
		private val view: SplashActivityContract.View)
	: SplashActivityContract.Presenter {

	override fun onCreated() {
		this.view.openActivity(SoundActivity::class.java)
	}
}