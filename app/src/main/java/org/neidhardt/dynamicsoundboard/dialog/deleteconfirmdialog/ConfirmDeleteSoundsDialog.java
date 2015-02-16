package org.neidhardt.dynamicsoundboard.dialog.deleteconfirmdialog;

import android.app.FragmentManager;
import org.neidhardt.dynamicsoundboard.BaseActivity;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 16.02.2015.
 */
public class ConfirmDeleteSoundsDialog extends ConfirmDeleteDialog
{
	private static final String TAG = ConfirmDeleteSoundsDialog.class.getName();

	public static void showInstance(FragmentManager manager)
	{
		ConfirmDeleteSoundsDialog dialog = new ConfirmDeleteSoundsDialog();
		dialog.show(manager, TAG);
	}

	@Override
	protected int getInfoTextResource()
	{
		return R.string.dialog_confirm_delete_sounds_in_soundheet_message;
	}

	@Override
	protected void delete()
	{
		BaseActivity.getCurrentFragment(this.getFragmentManager()).deleteAllSoundsInSoundSheet();
	}
}
