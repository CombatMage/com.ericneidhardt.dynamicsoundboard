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
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundmanagement.dialog.RenameSoundFileDialog;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundChangedEvent;
import org.neidhardt.dynamicsoundboard.views.edittext.CustomEditText;
import org.neidhardt.dynamicsoundboard.views.spinner.CustomSpinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * File created by eric.neidhardt on 23.02.2015.
 */
public class SoundSettingsDialog extends SoundSettingsBaseDialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
	private static final String TAG = SoundSettingsDialog.class.getName();

	private CustomEditText soundName;
	private CustomEditText soundSheetName;
	private CustomSpinner soundSheetSpinner;
	private CheckBox addNewSoundSheet;

	private int indexOfCurrentFragment = -1;

	public static void showInstance(FragmentManager manager, MediaPlayerData playerData)
	{
		SoundSettingsDialog dialog = new SoundSettingsDialog();
		addArguments(dialog, playerData.getPlayerId(), playerData.getFragmentTag());
		dialog.show(manager, TAG);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_sound_settings_layout, null);

		this.soundName = (CustomEditText)view.findViewById(R.id.et_name_file);
		this.soundSheetName = (CustomEditText)view.findViewById(R.id.et_name_new_sound_sheet);
		this.soundSheetSpinner = (CustomSpinner)view.findViewById(R.id.s_sound_sheets);
		this.addNewSoundSheet = (CheckBox)view.findViewById(R.id.cb_add_new_sound_sheet);

		this.addNewSoundSheet.setOnCheckedChangeListener(this);

		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_ok).setOnClickListener(this);

		this.soundName.setText(this.player.getMediaPlayerData().getLabel());

		this.setAvailableSoundSheets();

		this.soundSheetName.setText(this.getSoundSheetsDataUtil().getSuggestedName());
		this.soundSheetName.setVisibility(View.GONE);

		AppCompatDialog dialog = new AppCompatDialog(this.getActivity(), R.style.DialogTheme);
		dialog.setContentView(view);
		dialog.setTitle(R.string.dialog_sound_settings_title);

		return dialog;
	}

	private void setAvailableSoundSheets()
	{
		List<SoundSheet> soundSheets = this.getSoundSheetsDataAccess().getSoundSheets();
		ArrayList<String> labels = new ArrayList<>();
		for (int i = 0; i < soundSheets.size(); i++)
		{
			if (soundSheets.get(i).getFragmentTag().equals(this.fragmentTag))
				this.indexOfCurrentFragment = i;
			labels.add(soundSheets.get(i).getLabel());
		}
		if (this.indexOfCurrentFragment == -1)
			throw new IllegalStateException(TAG + " Current fragment of sound " + this.player.getMediaPlayerData() + " is not found in list of sound sheets " + soundSheets);

		this.soundSheetSpinner.setItems(labels);
		this.soundSheetSpinner.setSelectedItem(this.indexOfCurrentFragment);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (isChecked)
		{
			this.soundSheetSpinner.setVisibility(View.GONE);
			this.soundSheetName.setVisibility(View.VISIBLE);
		}
		else
		{
			this.soundSheetName.setVisibility(View.GONE);
			this.soundSheetSpinner.setVisibility(View.VISIBLE);
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
				boolean hasLabelChanged = !this.player.getMediaPlayerData().getLabel().equals(this.soundName.getDisplayedText());
				this.deliverResult();
				this.dismiss();
				if (hasLabelChanged)
					new RenameSoundFileDialog(this.getFragmentManager(), this.player.getMediaPlayerData());
				break;
		}
	}

	private void deliverResult()
	{
		String soundLabel = this.soundName.getDisplayedText();
		int indexOfSelectedSoundSheet = this.soundSheetSpinner.getSelectedItemPosition();

		boolean addNewSoundSheet = this.addNewSoundSheet.isChecked();
		boolean hasSoundSheetChanged = addNewSoundSheet || indexOfSelectedSoundSheet != this.indexOfCurrentFragment;

		if (!hasSoundSheetChanged)
		{
			this.player.getMediaPlayerData().setLabel(soundLabel);
			this.player.getMediaPlayerData().updateItemInDatabaseAsync();
			EventBus.getDefault().post(new SoundChangedEvent(this.player));
		}
		else
		{
			this.getSoundsDataStorage().removeSounds(Collections.singletonList(this.player));

			Uri uri = Uri.parse(this.player.getMediaPlayerData().getUri());

			MediaPlayerData mediaPlayerData;

			if (addNewSoundSheet)
			{
				String soundSheetName = this.soundSheetName.getDisplayedText();
				SoundSheet soundSheet = this.getSoundSheetsDataUtil().getNewSoundSheet(soundSheetName);

				this.getSoundSheetsDataStorage().addSoundSheetToManager(soundSheet);

				String fragmentTag = soundSheet.getFragmentTag();
				mediaPlayerData = EnhancedMediaPlayer.getMediaPlayerData(fragmentTag, uri, soundLabel);
			}
			else
				mediaPlayerData = EnhancedMediaPlayer.getMediaPlayerData(this.fragmentTag, uri, soundLabel);

			this.getSoundsDataStorage().createSoundAndAddToManager(mediaPlayerData);
		}
	}

}
