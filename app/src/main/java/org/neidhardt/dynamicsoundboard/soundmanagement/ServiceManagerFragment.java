package org.neidhardt.dynamicsoundboard.soundmanagement;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import org.neidhardt.dynamicsoundboard.BaseFragment;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File created by eric.neidhardt on 02.12.2014.
 */
public class ServiceManagerFragment extends BaseFragment implements ServiceConnection
{
	public static final String TAG = ServiceManagerFragment.class.getName();

	private boolean isServiceBound = false;

	private MusicService service;
	public MusicService getSoundService()
	{
		return this.service;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
	}

	private void startSoundManagerService()
	{
		this.getActivity().bindService(new Intent(this.getActivity(), MusicService.class), this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		this.startSoundManagerService();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (this.isServiceBound)
			this.getActivity().unbindService(this);
	}

	public boolean isServiceBound()
	{
		return this.isServiceBound;
	}

	public void onUserLeaveHint()
	{
		if (this.service != null)
			this.service.onActivityClosed();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		Logger.d(TAG, "onServiceConnected: " + (name != null ? name : "null"));
		MusicService.Binder binder = (MusicService.Binder) service;
		if (binder != null)
		{
			this.service = binder.getService();
			this.isServiceBound = true;
		}
		else
			this.isServiceBound = false;
	}

	@Override
	public void onServiceDisconnected(ComponentName name)
	{
		Logger.d(TAG, "onServiceDisconnected");
		this.isServiceBound = false;
	}

	public List<EnhancedMediaPlayer> getPlayList()
	{
		if (this.service == null)
			return new ArrayList<>();
		return this.service.getPlaylist();
	}

	public Map<String, List<EnhancedMediaPlayer>> getSounds()
	{
		if (this.service == null)
			return new HashMap<>();
		return this.service.getSounds();
	}

	public void notifyPlaylist()
	{
		NavigationDrawerFragment fragment = this.getNavigationDrawerFragment();
		fragment.getPlaylist().notifyDataSetChanged();
	}

	public void notifyFragment(String fragmentTag)
	{
		NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawerFragment();
		SoundSheetFragment fragment = (SoundSheetFragment) this.getFragmentManager().findFragmentByTag(fragmentTag);
		if (fragment != null)
			fragment.notifyDataSetChanged();

		navigationDrawerFragment.getSoundSheetsAdapter().notifyDataSetChanged(); // updates sound count in sound sheet list
	}
}
