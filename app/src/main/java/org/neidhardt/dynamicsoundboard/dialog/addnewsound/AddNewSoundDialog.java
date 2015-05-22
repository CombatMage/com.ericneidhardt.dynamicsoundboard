package org.neidhardt.dynamicsoundboard.dialog.addnewsound;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dialog.BaseDialog;
import org.neidhardt.dynamicsoundboard.dialog.soundsettings.RenameSoundFileDialog;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.IntentRequest;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.Playlist;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlayListLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundLoadedEvent;

import java.util.ArrayList;
import java.util.List;


public class AddNewSoundDialog extends BaseDialog implements View.OnClickListener
{
	private static final String TAG = AddNewSoundDialog.class.getName();

	private static final String KEY_CALLING_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.dialog.addnewsound.AddNewSoundDialog.callingFragmentTag";
	private static final String KEY_SOUNDS_URI = "org.neidhardt.dynamicsoundboard.dialog.addnewsound.AddNewSoundDialog.soundsToAdd";
	private static final String KEY_SOUNDS_LABEL = "org.neidhardt.dynamicsoundboard.dialog.addnewsound.AddNewSoundDialog.soundsToAddLabels";

	private ViewGroup soundsToAddLayout;
	private String callingFragmentTag;
	private List<Uri> soundsToAdd;

	public static void showInstance(FragmentManager manager, String callingFragmentTag)
	{
		AddNewSoundDialog dialog = new AddNewSoundDialog();

		Bundle args = new Bundle();
		args.putString(KEY_CALLING_FRAGMENT_TAG, callingFragmentTag);
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.soundsToAdd = new ArrayList<>();
		Bundle args = this.getArguments();
		if (args != null)
			this.callingFragmentTag = args.getString(KEY_CALLING_FRAGMENT_TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound, null);

		view.findViewById(R.id.b_ok).setOnClickListener(this);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_add_another_sound).setOnClickListener(this);

		this.soundsToAddLayout = (ViewGroup)view.findViewById(R.id.layout_sounds_to_add);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null)
		{
			List<String> labels = savedInstanceState.getStringArrayList(KEY_SOUNDS_LABEL);
			List<String> uris = savedInstanceState.getStringArrayList(KEY_SOUNDS_URI);
			if (labels != null && uris != null)
			{
				int count = labels.size();
				for (int i = 0; i < count; i++)
				{
					this.soundsToAdd.add(i, Uri.parse(uris.get(i)));

					AddSoundListItem item = new AddSoundListItem(this.getActivity());
					item.setPath(this.soundsToAdd.get(i).toString());
					item.setSoundName(labels.get(i));
					this.soundsToAddLayout.addView(item);
				}
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.b_ok:
				if (soundsToAdd.size() > 0)
				{
					this.returnResultsToCallingFragment();
					this.dismiss();
				}
				else
					showInfoToast();
				break;
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_add_another_sound:
				this.startIntentForNewSound();
				break;
		}
	}

	private void startIntentForNewSound()
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(FileUtils.MIME_AUDIO);
		this.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == IntentRequest.GET_AUDIO_FILE)
			{
				Uri soundUri = data.getData();
				this.addNewSoundToLoad(soundUri);
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void addNewSoundToLoad(Uri soundUri)
	{
		this.soundsToAdd.add(soundUri);
		AddSoundListItem item = new AddSoundListItem(this.getActivity());
		item.setPath(soundUri.toString());
		item.setSoundName(FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(this.getActivity(), soundUri)));
		this.soundsToAddLayout.addView(item);
	}

	@Override
	public void onSaveInstanceState(@SuppressWarnings("NullableProblems") Bundle outState)
	{
		super.onSaveInstanceState(outState);

		ArrayList<String> uris = new ArrayList<>(this.soundsToAdd.size());
		ArrayList<String> labels = new ArrayList<>(this.soundsToAdd.size());
		int count = this.soundsToAdd.size();
		for (int i = 0; i < count; i++)
		{
			AddSoundListItem view = (AddSoundListItem)this.soundsToAddLayout.getChildAt(i);
			labels.add(i, view.getSoundName());
			uris.add(i, this.soundsToAdd.get(i).toString());
		}
		outState.putStringArrayList(KEY_SOUNDS_URI, uris);
		outState.putStringArrayList(KEY_SOUNDS_LABEL, labels);
	}

	private void returnResultsToCallingFragment()
	{
		ServiceManagerFragment fragment = this.getServiceManagerFragment();
		if (fragment == null)
			throw new NullPointerException("cannot addSoundSheetAndNotifyFragment sound, SoundManagerFragment is null");

		int count = this.soundsToAdd.size();
		List<MediaPlayerData> playersData = new ArrayList<>(count);
		List<MediaPlayerData> renamedPlayers = new ArrayList<>();
		for (int i = 0; i < count; i++)
		{
			AddSoundListItem item = (AddSoundListItem)this.soundsToAddLayout.getChildAt(i);

			Uri soundUri = this.soundsToAdd.get(i);
			String soundLabel = item.getSoundName();
			MediaPlayerData playerData = EnhancedMediaPlayer.getMediaPlayerData(this.callingFragmentTag, soundUri, soundLabel);
			playersData.add(playerData);

			if (item.wasSoundNameAltered())
				renamedPlayers.add(playerData);
		}

		EventBus bus = EventBus.getDefault();
		if (this.callingFragmentTag.equals(Playlist.TAG))
		{
			for (MediaPlayerData playerData : playersData)
				bus.post(new PlayListLoadedEvent(playerData, false));
		}
		else
		{
			for (MediaPlayerData playerData : playersData)
				bus.post(new SoundLoadedEvent(playerData, false));
		}

		this.showRenameDialog(renamedPlayers); // show the rename dialog for all altered players
	}

	private void showRenameDialog(List<MediaPlayerData> renamedMediaPlayers)
	{
		for (MediaPlayerData data : renamedMediaPlayers)
			RenameSoundFileDialog.showInstance(this.getFragmentManager(), data);
	}

	private void showInfoToast()
	{
		Toast.makeText(this.getActivity(), R.string.dialog_add_new_sound_toast_no_sounds, Toast.LENGTH_SHORT).show();
	}

}
