package org.neidhardt.dynamicsoundboard.dialog.soundsettings;

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
import org.neidhardt.dynamicsoundboard.customview.edittext.CustomEditText;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.dialog.addnewsoundfromintent.CustomSpinner;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.SoundSheetsManagerFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by eric.neidhardt on 23.02.2015.
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

		this.soundSheetName.setText(this.getSoundSheetManagerFragment().getSuggestedSoundSheetName());
		this.soundSheetName.setVisibility(View.GONE);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

	private void setAvailableSoundSheets()
	{
		List<SoundSheet> soundSheets = this.getSoundSheetManagerFragment().getSoundSheets();
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
	public void onClick(View v)
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
					RenameSoundFileDialog.showInstance(this.getFragmentManager(), this.player.getMediaPlayerData());
				break;
		}
	}

	private void deliverResult()
	{
		String soundLabel = this.soundName.getDisplayedText();
		int indexOfSelectedSoundSheet = this.soundSheetSpinner.getSelectedItemPosition();

		boolean addNewSoundSheet = this.addNewSoundSheet.isChecked();
		boolean hasSoundSheetChanged = addNewSoundSheet || indexOfSelectedSoundSheet != this.indexOfCurrentFragment;

		SoundSheetFragment soundSheetFragment = this.getSoundSheetFragment(this.fragmentTag);
		if (!hasSoundSheetChanged)
		{
			this.player.getMediaPlayerData().setLabel(soundLabel);
			soundSheetFragment.notifyDataSetChanged();
		}
		else
		{
			ServiceManagerFragment serviceManagerFragment = this.getServiceManagerFragment();
			serviceManagerFragment.getSoundService().removeSounds(Collections.singletonList(this.player));
			soundSheetFragment.notifyDataSetChanged();

			SoundSheetsManagerFragment soundSheetsManagerFragment = this.getSoundSheetManagerFragment();
			Uri uri = Uri.parse(this.player.getMediaPlayerData().getUri());

			if (addNewSoundSheet)
			{
				String soundSheetName = this.soundSheetName.getDisplayedText();
				soundSheetsManagerFragment.addSoundToSoundSheet(uri, soundLabel, soundSheetName);
			}
			else
				soundSheetsManagerFragment.addSoundToSoundSheet(uri, soundLabel, soundSheetsManagerFragment.get(this.fragmentTag));
		}
	}

}
