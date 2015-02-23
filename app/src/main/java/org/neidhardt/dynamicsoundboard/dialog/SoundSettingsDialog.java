package org.neidhardt.dynamicsoundboard.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;

/**
 * Created by eric.neidhardt on 23.02.2015.
 */
public class SoundSettingsDialog extends BaseDialog
{
	private static final String TAG = SoundSettingsDialog.class.getName();

	private static final String KEY_PLAYER_ID = "org.neidhardt.dynamicsoundboard.dialog.SoundSettingsDialog.playerId";
	private static final String KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.dialog.SoundSettingsDialog.fragmentTag";

	private String playerId;
	private String fragmentTag;

	public static void showInstance(FragmentManager manager, MediaPlayerData playerData)
	{
		SoundSettingsDialog dialog = new SoundSettingsDialog();

		Bundle args = new Bundle();
		args.putString(KEY_PLAYER_ID, playerData.getPlayerId());
		args.putString(KEY_FRAGMENT_TAG, playerData.getFragmentTag());
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle args = this.getArguments();
		if (args != null)
		{
			this.playerId = args.getString(KEY_PLAYER_ID);
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_sound_settings_layout, null);

		// TODO findViewsById

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

}
