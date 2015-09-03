package org.neidhardt.dynamicsoundboard.fileexplorer;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;

/**
 * Project created by Eric Neidhardt on 30.09.2014.
 */
public class AddNewSoundFromFileDialog
		extends
			AddNewSoundFromDirectoryDialog
		implements
			View.OnClickListener
{
	private static final String TAG = AddNewSoundFromFileDialog.class.getName();

	public static void showInstance(FragmentManager manager, String callingFragmentTag)
	{
		AddNewSoundFromFileDialog dialog = new AddNewSoundFromFileDialog();

		Bundle args = new Bundle();
		args.putString(KEY_CALLING_FRAGMENT_TAG, callingFragmentTag);
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	protected boolean canSelectDirectory()
	{
		return false;
	}

	@Override
	protected boolean canSelectFile()
	{
		return true;
	}

}
