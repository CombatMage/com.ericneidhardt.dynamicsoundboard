package org.neidhardt.dynamicsoundboard.dialog.soundsettings;

import android.app.Dialog;
import android.os.Bundle;
import org.neidhardt.dynamicsoundboard.dialog.BaseDialog;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

/**
 * Created by eric.neidhardt on 12.04.2015.
 */
public abstract class SoundSettingsBaseDialog extends BaseDialog
{
	private static final String KEY_PLAYER_ID = "org.neidhardt.dynamicsoundboard.dialog.soundsettings.SoundSettingsBaseDialog.playerId";
	private static final String KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.dialog.soundsettings.SoundSettingsBaseDialog.fragmentTag";

	protected String fragmentTag;
	protected EnhancedMediaPlayer player;

	protected static void addArguments(BaseDialog dialog, String playerId, String fragmentTag)
	{
		Bundle args = new Bundle();
		args.putString(KEY_PLAYER_ID, playerId);
		args.putString(KEY_FRAGMENT_TAG, fragmentTag);

		dialog.setArguments(args);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle args = this.getArguments();
		if (args != null)
		{
			String playerId = args.getString(KEY_PLAYER_ID);
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG);
			this.player = this.getServiceManagerFragment().getSoundService().searchForId(this.fragmentTag, playerId);
		}
	}

	@Override
	public abstract Dialog onCreateDialog(Bundle savedInstanceState);
}
