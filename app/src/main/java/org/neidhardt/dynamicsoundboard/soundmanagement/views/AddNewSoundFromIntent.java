package org.neidhardt.dynamicsoundboard.soundmanagement.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.views.BaseDialog;
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

	private EditText soundName;
	private EditText soundSheetName;
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

	@NonNull
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
		this.soundName = (EditText)view.findViewById(R.id.et_name_file);
		this.soundSheetName = (EditText)view.findViewById(R.id.et_name_new_sound_sheet);

		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_ok).setOnClickListener(this);

		AppCompatDialog dialog = new AppCompatDialog(this.getActivity(), R.style.DialogTheme);
		dialog.setContentView(view);
		dialog.setTitle(R.string.dialog_add_new_sound_from_intent_title);

		return dialog;
	}

	private Dialog createDialogToSelectSoundSheet()
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound_from_intent_to_sound_sheet, null);

		this.soundName = (EditText)view.findViewById(R.id.et_name_file);
		this.soundSheetName = (EditText)view.findViewById(R.id.et_name_new_sound_sheet);
		this.soundSheetSpinner = (CustomSpinner)view.findViewById(R.id.s_sound_sheets);
		this.addNewSoundSheet = (CheckBox)view.findViewById(R.id.cb_add_new_sound_sheet);

		this.addNewSoundSheet.setOnCheckedChangeListener(this);

		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_ok).setOnClickListener(this);

		AppCompatDialog dialog = new AppCompatDialog(this.getActivity(), R.style.DialogTheme);
		dialog.setContentView(view);
		dialog.setTitle(R.string.dialog_add_new_sound_from_intent_title);

		return dialog;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.soundName.setText(FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(this.getActivity(), this.uri)));
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
	public void onClick(@NonNull View v)
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
		String newSoundSheetLabel = soundSheetName.getText().toString();
		if (newSoundSheetLabel.length() == 0)
			newSoundSheetLabel = this.suggestedName;

		String soundSheetFragmentTag;
		if (!this.soundSheetsAlreadyExists || this.addNewSoundSheet.isChecked())
			soundSheetFragmentTag = this.addNewSoundSheet(newSoundSheetLabel);
		else
			soundSheetFragmentTag = this.availableSoundSheetIds.get(this.soundSheetSpinner.getSelectedItemPosition());

		String soundLabel = this.soundName.getText().toString();
		Uri soundUri = this.uri;

		MediaPlayerData mediaPlayerData = EnhancedMediaPlayer.getMediaPlayerData(soundSheetFragmentTag, soundUri, soundLabel);
		this.getSoundsDataStorage().createSoundAndAddToManager(mediaPlayerData);
	}

	private String addNewSoundSheet(String label)
	{
		SoundSheet newSoundSheet = this.getSoundSheetsDataUtil().getNewSoundSheet(label);
		newSoundSheet.setIsSelected(true);
		this.getSoundSheetsDataStorage().addSoundSheetToManager(newSoundSheet);
		return newSoundSheet.getFragmentTag();
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
