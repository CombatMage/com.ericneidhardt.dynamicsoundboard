package org.neidhardt.dynamicsoundboard.dialog.deleteconfirmdialog;

import android.app.FragmentManager;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 16.02.2015.
 */
public class ConfirmDeleteSoundSheetDialog extends ConfirmDeleteDialog
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
		this.getSoundSheetManagerFragment().deleteCurrentActiveSoundSheet();
	}
}
