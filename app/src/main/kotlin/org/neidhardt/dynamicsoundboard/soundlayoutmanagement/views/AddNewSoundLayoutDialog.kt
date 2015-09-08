package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views

import android.app.FragmentManager
import android.os.Bundle
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutAddedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsManager

/**
 * File created by eric.neidhardt on 12.03.2015.
 */
public class AddNewSoundLayoutDialog : SoundLayoutDialog()
{
	private var suggestedName: String? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		val args = this.getArguments()
		if (args != null)
			this.suggestedName = args.getString(KEY_SUGGESTED_NAME)
	}

	override fun getLayoutId(): Int
	{
		return R.layout.dialog_add_new_sound_layout
	}

	override fun getHintForName(): String
	{
		return this.suggestedName ?: ""
	}

	override fun deliverResult()
	{
		var name = super.soundLayoutName?.getText().toString()
		if (name.length() == 0)
			name = this.getHintForName()

		val layout = SoundLayout()
		layout.setIsSelected(false)
		layout.setDatabaseId(SoundLayoutsManager.getNewDatabaseIdForLabel(name))
		layout.setLabel(name)

		DynamicSoundboardApplication.getSoundLayoutsStorage().addSoundLayout(layout)

		EventBus.getDefault().post(SoundLayoutAddedEvent(layout))
	}

	public interface OnSoundLayoutAddedEventListener
	{
		/**
		 * This is called by greenRobot EventBus in case a new SoundLayout was renamed.
		 * @param event delivered SoundLayoutRenamedEvent
		 */
		SuppressWarnings("unused")
		public fun onEvent(event: SoundLayoutAddedEvent)
	}

	companion object
	{
		private val TAG = javaClass<AddNewSoundLayoutDialog>().getName()

		private val KEY_SUGGESTED_NAME = "org.neidhardt.dynamicsoundboard.dialog.AddNewSoundLayoutDialog.suggestedName"

		public fun showInstance(manager: FragmentManager, suggestedName: String) {
			val dialog = AddNewSoundLayoutDialog()

			val args = Bundle()
			args.putString(KEY_SUGGESTED_NAME, suggestedName)
			dialog.setArguments(args)

			dialog.show(manager, TAG)
		}
	}
}