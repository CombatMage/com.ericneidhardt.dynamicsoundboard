package org.neidhardt.dynamicsoundboard.soundactivity

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
interface SoundActivityContract {
	interface View {
	}
	interface Presenter {
		fun onResumed()
		fun onPaused()
	}
	interface Model {
		fun saveData()
	}
}