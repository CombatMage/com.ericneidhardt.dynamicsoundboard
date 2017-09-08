package org.neidhardt.viewpager_dialog

/**
 * Created by eric.neidhardt@gmail.com on 08.09.2017.
 */
interface ViewPagerDialogContract {
	interface View<T> {
		fun closeDialog()
		fun setDisplayedContent(viewData: Array<T>)
	}
	interface Presenter {
		fun onCreateDialog()
	}
}