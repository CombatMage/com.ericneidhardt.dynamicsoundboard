package org.neidhardt.dynamicsoundboard.soundsheetmanagement.views;

import android.app.FragmentManager;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.model.SoundDataModel;
import org.neidhardt.dynamicsoundboard.views.BaseConfirmDeleteDialog;

import java.util.List;

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
public class ConfirmDeleteAllSoundSheetsDialog extends BaseConfirmDeleteDialog
{
	private static final String TAG = ConfirmDeleteAllSoundSheetsDialog.class.getName();

	public static void showInstance(FragmentManager manager)
	{
		ConfirmDeleteAllSoundSheetsDialog dialog = new ConfirmDeleteAllSoundSheetsDialog();
		dialog.show(manager, TAG);
	}

	@Override
	protected int getInfoTextResource()
	{
		return R.string.dialog_confirm_delete_all_soundsheets_message;
	}

	@Override
	protected void delete()
	{
		List<SoundSheet> soundSheets = this.soundSheetsDataAccess.getSoundSheets();
		this.getSoundActivity().removeSoundFragments(soundSheets);

		SoundDataModel model = ServiceManagerFragment.getSoundDataModel();
		for (SoundSheet soundSheet : soundSheets)
		{
			List<EnhancedMediaPlayer> soundsInSoundSheet = model.getSoundsInFragment(soundSheet.getFragmentTag());
			model.removeSounds(soundsInSoundSheet);
		}

		this.soundSheetsDataStorage.removeAllSoundSheets();
	}
}
