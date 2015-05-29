package org.neidhardt.dynamicsoundboard.soundmanagement.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.SoundSheetsManagerFragment;
import org.neidhardt.dynamicsoundboard.views.BaseDialog;
import org.neidhardt.dynamicsoundboard.views.edittext.CustomEditText;
import org.neidhardt.dynamicsoundboard.views.spinner.CustomSpinner;

import java.util.ArrayList;
import java.util.List;

public class AddNewSoundFromIntent extends BaseDialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
	private static final String TAG = AddNewSoundFromIntent.class.getName();

	private static final String KEY_SOUND_URI = "org.neidhardt.dynamicsoundboard.soundmanagement.views.AddNewSoundFromIntent.uri";
	private static final String KEY_SUGGESTED_NAME = "org.neidhardt.dynamicsoundboard.soundmanagement.views.AddNewSoundFromIntent.suggestedName";
	private static final String KEY_AVAILABLE_SOUND_SHEET_LABELS = "org.neidhardt.dynamicsoundboard.soundmanagement.views.AddNewSoundFromIntent.availableSoundSheetLabels";
	private static final String KEY_AVAILABLE_SOUND_SHEET_IDS = "org.neidhardt.dynamicsoundboard.soundmanagement.views.AddNewSoundFromIntent.availableSoundSheetIds";

	private CustomEditText soundName;
	private CustomEditText soundSheetName;
	private CustomSpinner soundSheetSpinner;
	private CheckBox addNewSoundSheet;

	private Uri uri;
	private String suggestedName;
	private List<String> availableSoundSheetLabels;
	private List<String> availableSoundSheetIds;

	private boolean soundSheetsAlreadyExists;

	public static void showInstance(FragmentManager manager, Uri uri, String suggestedName, List<SoundSheet> availableSoundSheets)
	{
		AddNewSoundFromIntent dialog = new AddNewSoundFromIntent();

		Bundle args = new Bundle();
		args.putString(KEY_SOUND_URI, uri.toString());
		args.putString(KEY_SUGGESTED_NAME, suggestedName);
		if (availableSoundSheets != null)
		{
			args.putStringArrayList(KEY_AVAILABLE_SOUND_SHEET_LABELS, getLabelsFromSoundSheets(availableSoundSheets));
			args.putStringArrayList(KEY_AVAILABLE_SOUND_SHEET_IDS, getIdsFromSoundSheets(availableSoundSheets));
		}
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = this.getArguments();
		if (args != null)
		{
			this.uri = Uri.parse(args.getString(KEY_SOUND_URI));
			this.suggestedName = args.getString(KEY_SUGGESTED_NAME);
			this.availableSoundSheetLabels = args.getStringArrayList(KEY_AVAILABLE_SOUND_SHEET_LABELS);
			this.availableSoundSheetIds = args.getStringArrayList(KEY_AVAILABLE_SOUND_SHEET_IDS);
		}
		this.soundSheetsAlreadyExists = this.availableSoundSheetLabels != null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		if (!this.soundSheetsAlreadyExists)
			return this.createDialogIfNoSheetsExists();
		else
			return this.createDialogToSelectSoundSheet();
	}

	private Dialog createDialogIfNoSheetsExists()
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound_from_intent, null);
		this.soundName = (CustomEditText)view.findViewById(R.id.et_name_file);
		this.soundSheetName = (CustomEditText)view.findViewById(R.id.et_name_new_sound_sheet);

		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_ok).setOnClickListener(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

	private Dialog createDialogToSelectSoundSheet()
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound_from_intent_to_sound_sheet, null);

		this.soundName = (CustomEditText)view.findViewById(R.id.et_name_file);
		this.soundSheetName = (CustomEditText)view.findViewById(R.id.et_name_new_sound_sheet);
		this.soundSheetSpinner = (CustomSpinner)view.findViewById(R.id.s_sound_sheets);
		this.addNewSoundSheet = (CheckBox)view.findViewById(R.id.cb_add_new_sound_sheet);

		this.addNewSoundSheet.setOnCheckedChangeListener(this);

		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_ok).setOnClickListener(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.soundName.setText(FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(this.getActivity(), this.uri)));
		this.soundSheetName.setHint(this.suggestedName);
		if (this.soundSheetsAlreadyExists)
		{
			this.soundSheetSpinner.setItems(this.availableSoundSheetLabels);
			this.soundSheetName.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (isChecked)
		{
			soundSheetSpinner.setVisibility(View.GONE);
			soundSheetName.setVisibility(View.VISIBLE);
		}
		else
		{
			soundSheetName.setVisibility(View.GONE);
			soundSheetSpinner.setVisibility(View.VISIBLE);
		}
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
				break;
		}
	}

	private void deliverResult()
	{
		SoundSheetsManagerFragment caller = this.getSoundSheetManagerFragment();
		if (caller == null)
			return;

		String soundLabel = this.soundName.getText().toString();
		String newSoundSheet = soundSheetName.getDisplayedText();
		if (!this.soundSheetsAlreadyExists)
			caller.addSoundToNewSoundSheet(this.uri, soundLabel, newSoundSheet);
		else
		{
			String selectedSoundSheetId = this.availableSoundSheetIds.get(this.soundSheetSpinner.getSelectedItemPosition());

			if (this.addNewSoundSheet.isChecked())
				caller.addSoundToNewSoundSheet(this.uri, soundLabel, newSoundSheet);
			else
			{
				SoundSheet existingSoundSheet = caller.get(selectedSoundSheetId);
				caller.addSoundToNewSoundSheet(this.uri, soundLabel, existingSoundSheet);
			}
		}
	}

	private static ArrayList<String> getLabelsFromSoundSheets(List<SoundSheet> soundSheets)
	{
		ArrayList<String> labels = new ArrayList<>();
		for (SoundSheet soundSheet : soundSheets)
			labels.add(soundSheet.getLabel());
		return labels;
	}

	private static ArrayList<String> getIdsFromSoundSheets(List<SoundSheet> soundSheets)
	{
		ArrayList<String> labels = new ArrayList<>();
		for (SoundSheet soundSheet : soundSheets)
			labels.add(soundSheet.getFragmentTag());
		return labels;
	}

}
