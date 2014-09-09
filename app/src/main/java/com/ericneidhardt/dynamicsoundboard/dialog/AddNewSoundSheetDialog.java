package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.CustomEditText;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment;

/**
 * Created by eric.neidhardt on 04.09.2014.
 */
public class AddNewSoundSheetDialog extends DialogFragment implements View.OnClickListener
{
	public static final String TAG = AddNewSoundSheetDialog.class.getSimpleName();

	private static final String KEY_SUGGESTED_NAME = "com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundSheetDialog.suggestedName";

	private CustomEditText soundSheetName;
	private String suggestedName;

	public static void showInstance(FragmentManager manager, String suggestedName)
	{
		AddNewSoundSheetDialog dialog = new AddNewSoundSheetDialog();

		Bundle args = new Bundle();
		args.putString(KEY_SUGGESTED_NAME, suggestedName);
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = this.getArguments();
		if (args != null)
			this.suggestedName = args.getString(KEY_SUGGESTED_NAME);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound_layout, null);
		this.soundSheetName = (CustomEditText)view.findViewById(R.id.et_name_new_sound_sheet);
		this.soundSheetName.setHint(this.suggestedName);

		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_ok).setOnClickListener(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_ok:
				this.deliverResult();
				this.dismiss();
		}
	}

	private void deliverResult()
	{
		String label = this.soundSheetName.getDisplayedText();

		SoundSheetManagerFragment caller = (SoundSheetManagerFragment)this.getFragmentManager().findFragmentByTag(SoundSheetManagerFragment.TAG);
		if (caller == null)
			return;

		caller.onAddSoundSheet(label);
	}

	public interface OnAddSoundSheetListener
	{
		public void onAddSoundSheet(String label);
	}

}
