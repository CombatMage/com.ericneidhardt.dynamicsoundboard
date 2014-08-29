package com.ericneidhardt.dynamicsoundboard.sound_layout;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.MediaPlayerPool;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class SoundFragment extends Fragment
{
	private static final String KEY_FRAGMENT_ID = "SoundFragment.fragment_id";

	private String fragmentId;
	private MediaPlayerPool mediaPlayerPool;

	public static SoundFragment getNewInstance(String fragmentId)
	{
		SoundFragment fragment = new SoundFragment();
		Bundle args = new Bundle();
		args.putString(KEY_FRAGMENT_ID, fragmentId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.fragmentId = this.getArguments().getString(KEY_FRAGMENT_ID);
		this.mediaPlayerPool = new MediaPlayerPool(this.fragmentId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			return null;

		View fragmentView = inflater.inflate(R.layout.fragment_sound, container, false);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		fragmentView.setLayoutParams(params);

		return fragmentView;
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}
}
