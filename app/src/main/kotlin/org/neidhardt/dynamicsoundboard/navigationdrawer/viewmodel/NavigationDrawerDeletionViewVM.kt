package org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel

import android.databinding.BaseObservable

/**
 * @author eric.neidhardt on 24.06.2016.
 */
class NavigationDrawerDeletionViewVM : BaseObservable() {

	var title: String? = null
	set(value) {
		field = value
		this.notifyChange()
	}

	val subTitle: String
		get() {
			var countString = Integer.toString(this.selectionCount)
			if (countString.length == 1)
				countString = " " + countString
			countString = countString + "/" + this.maxCount
			return countString
		}

	var maxCount: Int = 0
		set(value) {
			field = value
			this.notifyChange()
		}

	var selectionCount: Int = 0
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onDoneClicked() {

	}
}