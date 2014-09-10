package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundDialog;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.MediaPlayerPool;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.OnMediaPlayersRetrievedCallback;

import java.util.List;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class SoundSheetFragment extends Fragment implements View.OnClickListener, OnMediaPlayersRetrievedCallback
{
	private static final String KEY_FRAGMENT_TAG = "com.ericneidhardt.dynamicsoundboard.SoundSheetFragment.fragmentTag";

	private String fragmentTag;
	private MediaPlayerPool mediaPlayerPool;

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
		this.fragmentTag = this.getArguments().getString(KEY_FRAGMENT_TAG);
		this.mediaPlayerPool = new MediaPlayerPool(this.fragmentTag);
		this.mediaPlayerPool.getMediaPlayersAsync(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		((BaseActivity)this.getActivity()).setSoundSheetActionsEnable(true);
		this.getActivity().findViewById(R.id.action_add_sound).setOnClickListener(this);
	}

	@Override
	public void onMediaPlayersRetrieved(List<MediaPlayer> mediaPlayers)
	{
		((TextView)this.getView().findViewById(R.id.tv_count)).setText(Integer.toString(mediaPlayers.size()));
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.action_add_sound:
				AddNewSoundDialog.showInstance(this.getFragmentManager(), this.fragmentTag);
				break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			return null;

		View fragmentView = inflater.inflate(R.layout.fragment_soundsheet, container, false);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		fragmentView.setLayoutParams(params);

		((TextView)fragmentView.findViewById(R.id.tv_label)).setText(this.fragmentTag);

		this.showMediaPlayers(fragmentView);

		return fragmentView;
	}

	public void showMediaPlayers(View fragmentView)
	{
		// TODO load mediaplayers from pool and add to ui
		TextView tv = (TextView)fragmentView.findViewById(R.id.tv_count);
		List<MediaPlayer> mediaPlayers = this.mediaPlayerPool.getMediaPlayers();
		tv.setText(Integer.toString(mediaPlayers == null ? 0 : mediaPlayers.size()));
	}

	public void addMediaPlayer(EnhancedMediaPlayer mediaPlayer)
	{
		this.mediaPlayerPool.add(mediaPlayer);
		TextView tv = (TextView)this.getView().findViewById(R.id.tv_count);
		tv.setText(Integer.toString(this.mediaPlayerPool.getMediaPlayers().size()));
	}

}
