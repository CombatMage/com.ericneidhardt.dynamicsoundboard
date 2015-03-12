package org.neidhardt.dynamicsoundboard.dialog.soundlayouts;

import android.app.FragmentManager;
import org.neidhardt.dynamicsoundboard.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 12.03.2015.
 */
public class SoundLayoutSettingsDialog extends SoundLayoutDialog
{
	private static final String TAG = SoundLayoutSettingsDialog.class.getName();

	public static void showInstance(FragmentManager manager)
	{
		SoundLayoutSettingsDialog dialog = new SoundLayoutSettingsDialog();
		dialog.show(manager, TAG);
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.dialog_sound_layout_settings;
	}

	@Override
	protected String getHintForName()
	{
		return null;
	}

	@Override
	protected void deliverResult()
	{
		String name = super.soundLayoutName.getDisplayedText();
		// TODO update according sound layout entry


		NavigationDrawerFragment fragment = this.getNavigationDrawerFragment();
		if (fragment != null)
			fragment.triggerSoundLayoutUpdate();
	}
}
