package org.neidhardt.dynamicsoundboard.splashactivity

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SplashActivityPresenter(
		private val view: SplashActivityContract.View,
		private val model: SplashActivityContract.Model)
	: SplashActivityContract.Presenter {

	override fun onCreated() {
		val toOpen = this.model.getActivityToStart()
		this.view.openActivity(toOpen)
	}
}