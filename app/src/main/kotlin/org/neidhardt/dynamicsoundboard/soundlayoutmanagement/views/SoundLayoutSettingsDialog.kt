package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views

import android.app.FragmentManager
import android.os.Bundle
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRenamedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsManager

/**
 * File created by eric.neidhardt on 12.03.2015.
 */
public class SoundLayoutSettingsDialog : SoundLayoutDialog()
{

	private var databaseId: String? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		val args = this.getArguments()
		if (args != null)
			this.databaseId = args.getString(KEY_DATABASE_ID)
	}

	override fun getLayoutId(): Int
	{
		return R.layout.dialog_sound_layout_settings
	}

	override fun getHintForName(): String
	{
		return this.soundLayoutsAccess.getSoundLayoutById(this.databaseId!!)?.getLabel() ?: ""
	}

	override fun deliverResult()
	{
		if (this.databaseId != null)
		{
			val name = super.soundLayoutName!!.getDisplayedText()
			val layout = this.soundLayoutsAccess.getSoundLayoutById(this.databaseId!!)
			layout?.setLabel(name)
			layout?.updateItemInDatabaseAsync()

			EventBus.getDefault().post(SoundLayoutRenamedEvent(layout))
		}
	}

	public interface OnSoundLayoutRenamedEventListener {
		/**
		 * This is called by greenRobot EventBus in case the SoundLayout was renamed.
		 * @param event delivered SoundLayoutRenamedEvent
		 */
		SuppressWarnings("unused")
		public fun onEvent(event: SoundLayoutRenamedEvent)
	}

	companion object {
		private val TAG = javaClass<SoundLayoutSettingsDialog>().getName()

		private val KEY_DATABASE_ID = "org.neidhardt.dynamicsoundboard.dialog.SoundLayoutSettingsDialog.databaseId"

		public fun showInstance(manager: FragmentManager, databaseId: String) {
			val dialog = SoundLayoutSettingsDialog()

			val args = Bundle()
			args.putString(KEY_DATABASE_ID, databaseId)
			dialog.setArguments(args)

			dialog.show(manager, TAG)
		}
	}
}
