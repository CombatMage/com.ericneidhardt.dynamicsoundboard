package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.*;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.AddSoundListItem;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.IntentRequest;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.playlist.Playlist;
import com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment;

import java.util.ArrayList;
import java.util.List;


public class AddNewSoundDialog extends BaseDialog implements View.OnClickListener
{
	public static final String TAG = AddNewSoundDialog.class.getSimpleName();

	private static final String KEY_CALLING_FRAGMENT_TAG = "com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundDialog.callingFragmentTag";
	private static final String KEY_SOUNDS_URI = "com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundDialog.soundsToAdd";
	private static final String KEY_SOUNDS_LABEL = "com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundDialog.soundsToAddLabels";

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

		this.soundsToAdd = new ArrayList<Uri>();
		Bundle args = this.getArguments();
		if (args != null)
			this.callingFragmentTag = args.getString(KEY_CALLING_FRAGMENT_TAG);
	}

	@SuppressWarnings("Annotator")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound, null);

		view.findViewById(R.id.b_add).setOnClickListener(this);
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
			case R.id.b_add:
				this.returnResultsToCallingFragment();
				this.dismiss();
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
		intent.setType(Util.MIME_AUDIO);
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
		item.setSoundName(Util.getFileNameFromUri(this.getActivity(), soundUri));
		this.soundsToAddLayout.addView(item);
	}

	@Override
	public void onSaveInstanceState(@SuppressWarnings("NullableProblems") Bundle outState)
	{
		super.onSaveInstanceState(outState);

		ArrayList<String> uris = new ArrayList<String>(this.soundsToAdd.size());
		ArrayList<String> labels = new ArrayList<String>(this.soundsToAdd.size());
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
		SoundManagerFragment fragment = this.getSoundManagerFragment();
		if (fragment == null)
			throw new NullPointerException("cannot addSoundSheetAndNotifyFragment sound, SoundManagerFragment is null");

		int count = this.soundsToAdd.size();
		List<MediaPlayerData> playersData = new ArrayList<MediaPlayerData>(count);
		for (int i = 0; i < count; i++)
		{
			Uri soundUri = this.soundsToAdd.get(i);
			String soundLabel = ((AddSoundListItem)this.soundsToAddLayout.getChildAt(i)).getSoundName();
			MediaPlayerData playerData = EnhancedMediaPlayer.getMediaPlayerData(this.callingFragmentTag, soundUri, soundLabel);
			playersData.add(playerData);
		}

		if (this.callingFragmentTag.equals(Playlist.TAG))
		{
			for (MediaPlayerData playerData : playersData)
				fragment.addSoundToPlaylist(playerData);
			fragment.notifyPlaylist();
		}
		else
		{
			for (MediaPlayerData playerData : playersData)
				fragment.addSound(playerData);
			fragment.notifyFragment(this.callingFragmentTag);
		}
	}

}
