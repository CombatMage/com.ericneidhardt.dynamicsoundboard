package org.neidhardt.dynamicsoundboard.soundactivity.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import org.neidhardt.dynamicsoundboard.BR
import org.neidhardt.ui_utils.views.CustomEditText

/**
 * @author eric.neidhardt on 21.06.2016.
 */
class ToolbarVM : BaseObservable() {

	fun onAddSoundSheetClicked() = this.onAddSoundSheetClicked.invoke()

	fun onAddSoundClicked() = this.onAddSoundClicked.invoke()

	fun onAddSoundFromDirectoryClicked() = this.onAddSoundFromDirectoryClicked.invoke()

	val onTextEditedListener = object : CustomEditText.OnTextEditedListener {
		override fun onTextEdited(text: String) {
			title = text
			onTitleChanged(text)
		}
	}

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

	var onTitleChanged: (String) -> Unit = {}

	var onAddSoundSheetClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	var onAddSoundClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}

	var onAddSoundFromDirectoryClicked: () -> Unit = {}
		set(value) {
			field = value
			this.notifyChange()
		}


}