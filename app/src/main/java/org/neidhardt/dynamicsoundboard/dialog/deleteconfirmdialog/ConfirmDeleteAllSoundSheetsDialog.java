package org.neidhardt.dynamicsoundboard.dialog.deleteconfirmdialog;

import android.app.FragmentManager;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 16.02.2015.
 */
public class ConfirmDeleteAllSoundSheetsDialog extends ConfirmDeleteDialog
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
		this.getSoundSheetManagerFragment().deleteAllSoundSheets();
	}
}
