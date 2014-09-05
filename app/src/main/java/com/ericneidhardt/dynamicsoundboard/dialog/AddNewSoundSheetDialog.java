package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.CustomEditText;

/**
 * Created by eric.neidhardt on 04.09.2014.
 */
public class AddNewSoundSheetDialog
{
	public static Dialog create(Context context, String suggestedName, final OnAddSoundSheetListener listener)
	{
		final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_new_sound_layout, null);
		((CustomEditText)dialogView.findViewById(R.id.et_name_new_sound_sheet)).setHint(suggestedName);

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
				String label = ((CustomEditText)dialogView.findViewById(R.id.et_name_new_sound_sheet)).getDisplayedText();
				listener.onAddSoundSheet(label);
				dialog.dismiss();
			}
		});

		return dialog;
	}

	public interface OnAddSoundSheetListener
	{
		public void onAddSoundSheet(String label);
	}
}
