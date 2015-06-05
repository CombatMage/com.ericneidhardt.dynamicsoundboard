package org.neidhardt.dynamicsoundboard.soundsheetmanagement.views;

import android.app.FragmentManager;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.views.BaseConfirmDeleteDialog;

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
public class ConfirmDeleteSoundSheetDialog extends BaseConfirmDeleteDialog
{
	private static final String TAG = ConfirmDeleteSoundSheetDialog.class.getName();

	public static void showInstance(FragmentManager manager)
	{
		ConfirmDeleteSoundSheetDialog dialog = new ConfirmDeleteSoundSheetDialog();
		dialog.show(manager, TAG);
	}

	@Override
	protected int getInfoTextResource()
	{
		return R.string.dialog_confirm_delete_soundsheet_message;
	}

	@Override
	protected void delete()
	{
		SoundSheetFragment fragment = SoundActivity.getCurrentSoundFragment(this.getFragmentManager());
		if (fragment != null)
		{
			fragment.removeAllSounds();
			SoundSheet soundSheet = SoundActivity.getSoundSheetsDataAccess().getSoundSheetForFragmentTag(fragment.getFragmentTag());
			SoundActivity.getSoundSheetsDataStorage().removeSoundSheet(soundSheet);

			this.getSoundActivity().removeSoundFragment(soundSheet);
		}
	}
}
