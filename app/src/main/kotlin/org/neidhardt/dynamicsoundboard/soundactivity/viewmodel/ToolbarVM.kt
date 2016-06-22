package org.neidhardt.dynamicsoundboard.soundactivity.viewmodel

import android.databinding.BaseObservable

/**
 * @author eric.neidhardt on 21.06.2016.
 */
class ToolbarVM : BaseObservable() {

	var isSoundSheetActionsEnable: Boolean = false
		set(value) {
			field = value
			this.notifyChange()
		}

	// TODO title with 2way binding

	var onAddSoundSheetClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onAddSoundSheetClicked() {
		this.onAddSoundSheetClicked.invoke()
	}

	var onAddSoundClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onAddSoundClicked() {
		this.onAddSoundClicked.invoke()
	}

	var onAddSoundFromDirectoryClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onAddSoundFromDirectoryClicked() {
		this.onAddSoundFromDirectoryClicked.invoke()
	}
}