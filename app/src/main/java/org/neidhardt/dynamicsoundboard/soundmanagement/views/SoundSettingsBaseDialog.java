package org.neidhardt.dynamicsoundboard.soundmanagement.views;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.views.BaseDialog;

/**
 * File created by eric.neidhardt on 12.04.2015.
 */
public abstract class SoundSettingsBaseDialog extends BaseDialog
{
	private static final String KEY_PLAYER_ID = "org.neidhardt.dynamicsoundboard.soundmanagement.views.SoundSettingsBaseDialog.playerId";
	private static final String KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.soundmanagement.views.SoundSettingsBaseDialog.fragmentTag";

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
			this.player = this.soundsDataAccess.getSoundById(this.fragmentTag, playerId);
		}
	}

	@NonNull
	@Override
	public abstract Dialog onCreateDialog(Bundle savedInstanceState);
}
