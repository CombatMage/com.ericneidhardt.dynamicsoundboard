package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views

import android.os.Bundle
import android.support.v4.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutRenamedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.findById

/**
 * File created by eric.neidhardt on 12.03.2015.
 */
class SoundLayoutSettingsDialog : SoundLayoutDialog()
{
	private val soundLayoutsManager = SoundboardApplication.soundLayoutManager

	private var databaseId: String? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		val args = this.arguments
		if (args != null)
			this.databaseId = args.getString(KEY_DATABASE_ID)
	}

	override fun getPositiveButtonId(): Int = R.string.dialog_rename

	override fun getLayoutId(): Int = R.layout.dialog_sound_layout_settings

	override fun getHintForName(): String = this.soundLayoutsManager.soundLayouts.findById(this.databaseId!!)?.label ?: ""

    override fun getTitleId(): Int = R.string.dialog_sound_layout_settings_title

	override fun deliverResult()
	{
		if (this.databaseId != null)
		{
			var name = super.soundLayoutName?.text.toString()
			if (name.isEmpty())
				name = this.getHintForName()

			val layout = this.soundLayoutsManager.soundLayouts.findById(this.databaseId!!) ?: return
			val updatedLayout = this.soundLayoutsManager.updateSoundLayout {
				layout.apply { this.label = name }
			}
			EventBus.getDefault().post(SoundLayoutRenamedEvent(updatedLayout))
		}
	}

	companion object {
		private val TAG = SoundLayoutSettingsDialog::class.java.name

		private val KEY_DATABASE_ID = "org.neidhardt.dynamicsoundboard.dialog.SoundLayoutSettingsDialog.databaseId"

		fun showInstance(manager: FragmentManager, databaseId: String) {
			val dialog = SoundLayoutSettingsDialog()

			val args = Bundle()
			args.putString(KEY_DATABASE_ID, databaseId)
			dialog.arguments = args

			dialog.show(manager, TAG)
		}
	}
}
