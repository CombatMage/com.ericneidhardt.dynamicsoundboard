package org.neidhardt.dynamicsoundboard.soundmanagement.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment;

/**
 * File created by eric.neidhardt on 02.12.2014.
 */
public class ServiceManagerFragment
		extends
			BaseFragment
		implements
			ServiceConnection
{
	public static final String TAG = ServiceManagerFragment.class.getName();

	private boolean isServiceBound = false;

	private MediaPlayerService service;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
	}

	private void startSoundManagerService()
	{
		this.getActivity().bindService(new Intent(this.getActivity(), MediaPlayerService.class), this, Context.BIND_AUTO_CREATE);
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
		Binder binder = (Binder) service;
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

}
