package org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable

/**
* @author Eric.Neidhardt@GMail.com on 17.06.2016.
*/
class NavigationDrawerButtonBarVM : BaseObservable() {

	@Bindable
	var enableDeleteSelected: Boolean = false
		set(value) {
			field = value
			this.notifyChange()
		}

	var onDeleteClicked: () -> Unit = {}

	fun onDeleteClicked() {
		this.onDeleteClicked.invoke()
	}


	var onAddClicked: () -> Unit = {}

	fun onAddClicked() {
		this.onAddClicked.invoke()
	}


	var onDeleteSelectedClicked: () -> Unit = {}

	fun onDeleteSelectedClicked() {
		this.onDeleteSelectedClicked.invoke()
	}
}