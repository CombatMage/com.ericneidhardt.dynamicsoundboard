package org.neidhardt.dynamicsoundboard.soundactivity.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import org.neidhardt.dynamicsoundboard.BR
import org.neidhardt.android_utils.views.CustomEditText

/**
 * @author eric.neidhardt on 21.06.2016.
 */
class ToolbarVM : BaseObservable() {

	fun onAddSoundSheetClicked() = this.addSoundSheetClickedCallback.invoke()

	fun onAddSoundClicked() = this.addSoundClickedCallback.invoke()

	fun onAddSoundFromDirectoryClicked() = this.addSoundFromDirectoryClickedCallback.invoke()

	fun onTitleClicked() = this.titleClickedCallback.invoke()

	var isSoundSheetActionsEnable: Boolean = false
		set(value) {
			field = value
			this.notifyPropertyChanged(BR.soundSheetActionsEnable)
		}
		@Bindable
		get

	var title: String? = null
		set(value) {
			if (field != value) {
				field = value
				this.notifyPropertyChanged(BR.title)
			}
		}
		@Bindable
		get

	var addSoundSheetClickedCallback: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	var addSoundClickedCallback: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	var addSoundFromDirectoryClickedCallback: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	var titleClickedCallback: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}
}