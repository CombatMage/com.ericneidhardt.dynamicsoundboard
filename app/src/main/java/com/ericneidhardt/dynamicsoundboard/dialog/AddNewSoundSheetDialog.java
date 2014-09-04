package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.ericneidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 04.09.2014.
 */
public class AddNewSoundSheetDialog
{
	public static void show(Context context, String suggestedName, final OnAddSoundSheetListener listener)
	{
		final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_new_sound_layout, null);
		((EditText)dialogView.findViewById(R.id.et_input)).setText(suggestedName);

		AlertDialog.Builder inputNameDialog = new AlertDialog.Builder(context);
		inputNameDialog.setView(dialogView);
		final AlertDialog dialog = inputNameDialog.create();

		dialogView.findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
			}
		});
		dialogView.findViewById(R.id.b_ok).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String label = ((EditText)dialogView.findViewById(R.id.et_input)).getText().toString();
				listener.onAddSoundSheet(label);
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	public interface OnAddSoundSheetListener
	{
		public void onAddSoundSheet(String label);
	}
}
