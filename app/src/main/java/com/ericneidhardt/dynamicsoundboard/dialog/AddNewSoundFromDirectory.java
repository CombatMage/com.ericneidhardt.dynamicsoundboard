package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Project created by Eric Neidhardt on 30.09.2014.
 */
public class AddNewSoundFromDirectory
		extends
			DialogFragment
		implements
			View.OnClickListener
{
	private static final String TAG = AddNewSoundFromDirectory.class.getSimpleName();

	public static void showInstance(FragmentManager manager)
	{
		AddNewSoundFromDirectory dialog = new AddNewSoundFromDirectory();

		dialog.show(manager, TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
			View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound_from_directory, null);
			view.findViewById(R.id.b_add).setOnClickListener(this);
			view.findViewById(R.id.b_cancel).setOnClickListener(this);

			AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
			builder.setView(view);

			return builder.create();
	}

	@Override
	public void onClick(View v)
	{
		// TODO
	}
}
