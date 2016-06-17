package org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel

import android.databinding.BaseObservable

/**
* Created by Eric.Neidhardt@GMail.com on 17.06.2016.
*/
class NavigationDrawerButtonBarVM : BaseObservable() {

	var enableDeleteSelected: Boolean = false
		set(value) {
			field = value
			this.notifyChange()
		}

	var onDeleteClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onDeleteClicked() { this.onDeleteClicked.invoke() }

	var onAddClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onAddClicked() { this.onAddClicked.invoke() }

	var onDeleteSelectedClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onDeleteSelectedClicked() { this.onDeleteSelectedClicked.invoke() }
}