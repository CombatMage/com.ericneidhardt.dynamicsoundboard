package com.ericneidhardt.dynamicsoundboard.storage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import com.ericneidhardt.dynamicsoundboard.BaseFragment;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;

/**
 * File created by eric.neidhardt on 02.12.2014.
 */
public class ServiceManagerFragment extends BaseFragment implements ServiceConnection
{
	public static final String TAG = ServiceManagerFragment.class.getName();

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

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.startSoundManagerService();
	}

	private void startSoundManagerService()
	{
		this.getActivity().bindService(new Intent(this.getActivity(), MusicService.class), this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		this.getActivity().unbindService(this);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		Logger.d(TAG, "onServiceConnected");
		MusicService.Binder binder = (MusicService.Binder) service;
		this.service = binder.getService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name)
	{
		Logger.d(TAG, "onServiceDisconnected");
	}
}
