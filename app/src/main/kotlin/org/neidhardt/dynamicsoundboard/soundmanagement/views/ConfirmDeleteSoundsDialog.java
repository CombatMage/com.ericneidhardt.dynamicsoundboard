package org.neidhardt.dynamicsoundboard.soundmanagement.views;

import android.app.FragmentManager;
import android.os.Bundle;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.views.BaseConfirmDeleteDialog;

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
public class ConfirmDeleteSoundsDialog extends BaseConfirmDeleteDialog
{
	private static final String TAG = ConfirmDeleteSoundsDialog.class.getName();

	private static final String KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.soundmanagement.views.ConfirmDeleteSoundsDialog.fragmentTag";

	private String fragmentTag;

	public static void showInstance(FragmentManager manager, String fragmentTag)
	{
		ConfirmDeleteSoundsDialog dialog = new ConfirmDeleteSoundsDialog();

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
		return R.string.dialog_confirm_delete_sounds_in_soundheet_message;
	}

	@Override
	protected void delete()
	{
		this.getSoundsDataStorage().removeSounds(this.getSoundsDataAccess().getSoundsInFragment(this.fragmentTag));
	}
}
