package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views

import android.os.Bundle
import android.support.v4.app.FragmentManager
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutManager
import rx.subscriptions.CompositeSubscription

/**
 * File created by eric.neidhardt on 12.03.2015.
 */
class AddNewSoundLayoutDialog : SoundLayoutDialog() {

	private var suggestedName: String? = null

	private val subscriptions = CompositeSubscription()
	private val manager = SoundboardApplication.newSoundLayoutManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val args = this.arguments
		if (args != null)
			this.suggestedName = this.getHintForName()
	}

	override fun deliverResult() {
		var name = super.soundLayoutName?.text.toString()
		if (name.isEmpty())
			name = this.getHintForName()

		val layout = NewSoundLayout().apply {
			this.isSelected = false
			this.databaseId = SoundLayoutManager.getNewDatabaseIdForLabel(name)
			this.label = name
		}
		this.manager.add(layout)
		this.dismiss()
	}

	override fun onDestroy() {
		super.onDestroy()
		this.subscriptions.unsubscribe()
	}

	override fun getLayoutId(): Int = R.layout.dialog_add_new_sound_layout

	override fun getHintForName(): String = this.manager.getSuggestedName()

    override fun getTitleId(): Int = R.string.dialog_add_new_sound_layout_title

	override fun getPositiveButtonId(): Int = R.string.dialog_add

	companion object
	{
		private val TAG = AddNewSoundLayoutDialog::class.java.name

		fun showInstance(manager: FragmentManager) {
			val dialog = AddNewSoundLayoutDialog()
			dialog.show(manager, TAG)
		}
	}
}
