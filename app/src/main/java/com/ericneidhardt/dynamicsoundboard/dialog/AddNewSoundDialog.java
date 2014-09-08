package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 08.09.2014.
 */
public class AddNewSoundDialog extends DialogFragment
{
	public static final String TAG = AddNewSoundDialog.class.getSimpleName();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}
}
