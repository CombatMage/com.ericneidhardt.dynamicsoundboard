package org.neidhardt.dynamicsoundboard.dialog.soundlayouts;

import android.app.FragmentManager;
import android.os.Bundle;
import org.neidhardt.dynamicsoundboard.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManager;

/**
 * Created by eric.neidhardt on 12.03.2015.
 */
public class SoundLayoutSettingsDialog extends SoundLayoutDialog
{
	private static final String TAG = SoundLayoutSettingsDialog.class.getName();

	private static final String KEY_DATABASE_ID = "org.neidhardt.dynamicsoundboard.dialog.SoundLayoutSettingsDialog.databaseId";

	private String databaseId;

	public static void showInstance(FragmentManager manager, String databaseId)
	{
		SoundLayoutSettingsDialog dialog = new SoundLayoutSettingsDialog();

		Bundle args = new Bundle();
		args.putString(KEY_DATABASE_ID, databaseId);
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = this.getArguments();
		if (args != null)
			this.databaseId = args.getString(KEY_DATABASE_ID);
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.dialog_sound_layout_settings;
	}

	@Override
	protected String getHintForName()
	{
		return SoundLayoutsManager.getInstance().getSoundLayoutById(this.databaseId).getLabel();
	}

	@Override
	protected void deliverResult()
	{
		String name = super.soundLayoutName.getDisplayedText();
		SoundLayoutsManager.getInstance().updateSoundLayoutById(this.databaseId, name);

		NavigationDrawerFragment fragment = this.getNavigationDrawerFragment();
		if (fragment != null)
			fragment.triggerSoundLayoutUpdate();
	}
}
