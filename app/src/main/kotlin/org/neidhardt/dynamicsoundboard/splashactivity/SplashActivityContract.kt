package org.neidhardt.dynamicsoundboard.splashactivity

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
interface SplashActivityContract {
	interface View {
		fun openActivity(cls: Class<*>)
	}
	interface Presenter {
		fun onCreated()
	}
}