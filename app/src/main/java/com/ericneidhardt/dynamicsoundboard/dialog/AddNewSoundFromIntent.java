package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;

import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.CustomEditText;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;

import java.util.List;

/**
 * Created by eric.neidhardt on 04.09.2014.
 */
public class AddNewSoundFromIntent
{
	public static void show(Context context, Uri uri, String suggestedName, final OnAddSoundFromIntentListener listener)
	{
		final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_new_sound_from_intent, null);
		final CustomEditText soundName = (CustomEditText)dialogView.findViewById(R.id.et_name_file);
		final CustomEditText soundSheetName = (CustomEditText)dialogView.findViewById(R.id.et_name_new_sound_sheet);

		soundName.setText(uri.toString());
		soundSheetName.setText(suggestedName);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setView(dialogView);
		final AlertDialog dialog = dialogBuilder.create();

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
				String name = soundName.getText().toString();
				String sheet = soundSheetName.getText().toString();
				listener.onAddSoundFromIntent(name, sheet, true);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public static void show(Context context, Uri uri, String suggestedName, List<SoundSheet> availableSoundSheets, final OnAddSoundFromIntentListener listener)
	{
		final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_new_sound_from_intent_to_sound_sheet, null);
		final CustomEditText soundName = (CustomEditText)dialogView.findViewById(R.id.et_name_file);
		final Spinner soundSheetSpinner = (Spinner)dialogView.findViewById(R.id.s_sound_sheets);

		soundName.setText(uri.toString());

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setView(dialogView);
		final AlertDialog dialog = dialogBuilder.create();

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
				// TODO trigger listener
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public interface OnAddSoundFromIntentListener
	{
		public void onAddSoundFromIntent(String soundName, String soundSheet, boolean addNewSoundSheet);
	}
}
