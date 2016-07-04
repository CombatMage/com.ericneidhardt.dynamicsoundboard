package org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable

/**
 * @author eric.neidhardt on 24.06.2016.
 */
class NavigationDrawerDeletionViewVM : BaseObservable() {

	var isEnable: Boolean = false
		set(value) {
			field = value
			this.notifyChange()
		}
		@Bindable
		get

	var title: String? = null
		set(value) {
			field = value
			this.notifyChange()
		}
		@Bindable
		get

	val subTitle: String = ""
		@Bindable
		get() {
			var countString = Integer.toString(this.selectionCount)
			if (countString.length == 1)
				countString = " " + countString
			countString = countString + "/" + this.maxCount

			field = countString
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

	var doneClickedCallback: () -> Unit = {}

	fun onDoneClicked() {
		this.doneClickedCallback()
	}

	var selectAllClickedCallback: () -> Unit = {}

	fun onSelectAllClicked() {
		this.selectAllClickedCallback()
	}
}