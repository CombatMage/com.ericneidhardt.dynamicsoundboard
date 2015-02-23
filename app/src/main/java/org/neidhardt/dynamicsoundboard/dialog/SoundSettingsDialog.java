package org.neidhardt.dynamicsoundboard.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.customview.edittext.CustomEditText;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.dialog.addnewsoundfromintent.CustomSpinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 23.02.2015.
 */
public class SoundSettingsDialog extends BaseDialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
	private static final String TAG = SoundSettingsDialog.class.getName();

	private static final String KEY_PLAYER_ID = "org.neidhardt.dynamicsoundboard.dialog.SoundSettingsDialog.playerId";
	private static final String KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.dialog.SoundSettingsDialog.fragmentTag";

	private CustomEditText soundName;
	private CustomEditText soundSheetName;
	private CustomSpinner soundSheetSpinner;
	private CheckBox addNewSoundSheet;

	private String playerId;
	private String fragmentTag;
	private MediaPlayerData playerData;

	public static void showInstance(FragmentManager manager, MediaPlayerData playerData)
	{
		SoundSettingsDialog dialog = new SoundSettingsDialog();

		Bundle args = new Bundle();
		args.putString(KEY_PLAYER_ID, playerData.getPlayerId());
		args.putString(KEY_FRAGMENT_TAG, playerData.getFragmentTag());

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
			this.playerId = args.getString(KEY_PLAYER_ID);
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG);
			this.playerData = this.getServiceManagerFragment().getSoundService().searchForId(this.fragmentTag, this.playerId).getMediaPlayerData();
		}
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
		view.findViewById(R.id.b_save).setOnClickListener(this);

		this.soundName.setText(this.playerData.getLabel());

		this.setAvailableSoundSheets();

		this.soundSheetName.setText(this.getSoundSheetManagerFragment().getSuggestedSoundSheetName());
		this.soundSheetName.setVisibility(View.GONE);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

	private void setAvailableSoundSheets()
	{
		List<SoundSheet> soundSheets = this.getSoundSheetManagerFragment().getAll();
		ArrayList<String> labels = new ArrayList<>();
		int indexOfCurrentFragment = -1;
		for (int i = 0; i < soundSheets.size(); i++)
		{
			if (soundSheets.get(i).getFragmentTag().equals(this.fragmentTag))
				indexOfCurrentFragment = i;
			labels.add(soundSheets.get(i).getLabel());
		}
		if (indexOfCurrentFragment == -1)
			throw new IllegalStateException(TAG + " Current fragment of sound " + this.playerData + " is not found in list of sound sheets " + soundSheets);

		this.soundSheetSpinner.setItems(labels);
		this.soundSheetSpinner.setSelectedItem(indexOfCurrentFragment);
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
			case R.id.b_save:
				this.deliverResult();
				this.dismiss();
				break;
		}
	}

	private void deliverResult()
	{
		// TODO
	}

}
