package org.neidhardt.dynamicsoundboard.soundsheetmanagement.views;

import android.app.FragmentManager;
import android.os.Bundle;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.views.BaseConfirmDeleteDialog;

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
public class ConfirmDeleteSoundSheetDialog extends BaseConfirmDeleteDialog
{
	private static final String TAG = ConfirmDeleteSoundSheetDialog.class.getName();

	private static final String KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.soundmanagement.views.ConfirmDeleteSoundsDialog.fragmentTag";
	private String fragmentTag;

	public static void showInstance(FragmentManager manager, String fragmentTag)
	{
		ConfirmDeleteSoundSheetDialog dialog = new ConfirmDeleteSoundSheetDialog();

		Bundle args = new Bundle();
		args.putString(KEY_FRAGMENT_TAG, fragmentTag);
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = this.getArguments();
		if (args != null)
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG);
	}

	@Override
	protected int getInfoTextResource()
	{
		return R.string.dialog_confirm_delete_soundsheet_message;
	}

	@Override
	protected void delete()
	{
		this.soundsDataStorage.removeSounds(this.soundsDataAccess.getSoundsInFragment(this.fragmentTag));

		SoundSheet soundSheet = this.soundSheetsDataAccess.getSoundSheetForFragmentTag(this.fragmentTag);
		this.soundSheetsDataStorage.removeSoundSheet(soundSheet);

		this.getSoundActivity().removeSoundFragment(soundSheet);
	}
}
