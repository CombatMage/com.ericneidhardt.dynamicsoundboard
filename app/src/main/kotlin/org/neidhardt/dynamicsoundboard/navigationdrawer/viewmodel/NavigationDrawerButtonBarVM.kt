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

	var deleteClickedCallback: () -> Unit = {}

	fun onDeleteClicked() {
		this.deleteClickedCallback()
	}

	var addClickedCallback: () -> Unit = {}

	fun onAddClicked() {
		this.addClickedCallback.invoke()
	}

	var deleteSelectedClickedCallback: () -> Unit = {}

	fun onDeleteSelectedClicked() {
		this.deleteSelectedClickedCallback.invoke()
	}
}