package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 08.09.2014.
 */
public class AddNewSoundDialog extends DialogFragment implements View.OnClickListener
{
	public static final String TAG = AddNewSoundDialog.class.getSimpleName();

	private ViewGroup soundsToAdd;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound, null);

		view.findViewById(R.id.b_ok).setOnClickListener(this);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_add_another_sound).setOnClickListener(this);

		this.soundsToAdd = (ViewGroup)view.findViewById(R.id.layout_sounds_to_add);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.b_ok:
				// TODO trigger callback
				this.dismiss();
				break;
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_add_another_sound:
				break;
		}
	}

	private void addNewSoundToLoad()
	{

	}

}
