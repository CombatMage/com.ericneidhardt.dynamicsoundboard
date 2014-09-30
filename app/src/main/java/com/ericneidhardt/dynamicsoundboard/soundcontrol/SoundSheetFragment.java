package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.BaseFragment;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundDialog;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundFromDirectory;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.IntentRequest;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment;

import java.util.List;

import static java.util.Arrays.asList;


public class SoundSheetFragment
		extends
			BaseFragment
		implements
			View.OnClickListener,
			SoundAdapter.OnItemDeleteListener
{
	private static final String KEY_FRAGMENT_TAG = "com.ericneidhardt.dynamicsoundboard.SoundSheetFragment.fragmentTag";

	private String fragmentTag;
	private SoundAdapter soundAdapter;


	public static SoundSheetFragment getNewInstance(SoundSheet soundSheet)
	{
		SoundSheetFragment fragment = new SoundSheetFragment();
		Bundle args = new Bundle();
		args.putString(KEY_FRAGMENT_TAG, soundSheet.getFragmentTag());
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);

		this.fragmentTag = this.getArguments().getString(KEY_FRAGMENT_TAG);
		this.soundAdapter = new SoundAdapter(this);
		this.soundAdapter.setOnItemDeleteListener(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		((BaseActivity)this.getActivity()).setSoundSheetActionsEnable(true);
		this.getActivity().findViewById(R.id.action_add_sound).setOnClickListener(this);
		this.getActivity().findViewById(R.id.action_add_sound_dir).setOnClickListener(this);

		this.soundAdapter.startProgressUpdateTimer();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		this.soundAdapter.stopProgressUpdateTimer();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch (item.getItemId())
		{
			case R.id.action_clear_sounds_in_sheet:
				SoundManagerFragment soundManagerFragment = this.getSoundManagerFragment();
				soundManagerFragment.removeSounds(this.fragmentTag);
				this.soundAdapter.notifyDataSetChanged();
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == IntentRequest.GET_AUDIO_FILE)
			{
				Uri soundUri = data.getData();
				String soundLabel = Util.getFileNameFromUri(this.getActivity(), soundUri);
				SoundManagerFragment fragment = this.getSoundManagerFragment();
				fragment.addSound(EnhancedMediaPlayer.getMediaPlayerData(this.fragmentTag, soundUri, soundLabel));
				fragment.notifyFragments();
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.action_add_sound:
				AddNewSoundDialog.showInstance(this.getFragmentManager(), this.fragmentTag);
				break;
			case R.id.action_add_sound_dir:
				AddNewSoundFromDirectory.showInstance(this.getFragmentManager(), this.fragmentTag);
				break;
			case R.id.b_add_sound:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType(Util.MIME_AUDIO);
				this.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE);
				break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			return null;

		View fragmentView = inflater.inflate(R.layout.fragment_soundsheet, container, false);

		fragmentView.findViewById(R.id.b_add_sound).setOnClickListener(this);

		RecyclerView listSounds = (RecyclerView)fragmentView.findViewById(R.id.rv_sounds);
		listSounds.addItemDecoration(new DividerItemDecoration(this.getActivity(),
				DividerItemDecoration.VERTICAL_LIST, null));
		listSounds.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		listSounds.setItemAnimator(new DefaultItemAnimator());
		listSounds.setAdapter(this.soundAdapter);

		SoundManagerFragment fragment = this.getSoundManagerFragment();
		List<EnhancedMediaPlayer> enhancedMediaPlayers = fragment.getSounds().get(this.fragmentTag);

		this.soundAdapter.clear();
		this.soundAdapter.addAll(enhancedMediaPlayers);
		this.soundAdapter.notifyDataSetChanged();

		return fragmentView;
	}

	@Override
	public void onItemDelete(EnhancedMediaPlayer player, int position)
	{
		this.soundAdapter.remove(position);
		this.soundAdapter.notifyItemRemoved(position);

		SoundManagerFragment fragment = this.getSoundManagerFragment();
		fragment.removeSounds(asList(player));
		fragment.notifyPlaylist();
		fragment.notifySoundSheetList();
	}

	public void notifyDataSetChanged(boolean newSoundsAvailable)
	{
		SoundManagerFragment fragment = this.getSoundManagerFragment();

		if (newSoundsAvailable)
		{
			this.soundAdapter.clear();
			this.soundAdapter.addAll(fragment.getSounds().get(this.fragmentTag));
			this.soundAdapter.notifyDataSetChanged();
		}
		else
			this.soundAdapter.notifyDataSetChanged();
	}

}
