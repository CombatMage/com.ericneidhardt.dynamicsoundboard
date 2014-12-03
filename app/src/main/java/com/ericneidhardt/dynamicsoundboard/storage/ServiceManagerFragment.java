package com.ericneidhardt.dynamicsoundboard.storage;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.ericneidhardt.dynamicsoundboard.BaseFragment;
import com.ericneidhardt.dynamicsoundboard.NavigationDrawerFragment;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;

import java.util.Set;

/**
 * File created by eric.neidhardt on 02.12.2014.
 */
public class ServiceManagerFragment extends BaseFragment implements ServiceConnection
{
	public static final String TAG = ServiceManagerFragment.class.getName();

	private ServiceLoadingReceiver receiver;

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

		this.receiver = new ServiceLoadingReceiver();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicService.ACTION_FINISHED_LOADING_PLAYLIST);
		filter.addAction(MusicService.ACTION_FINISHED_LOADING_SOUNDS);
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(this.receiver, filter);

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
		LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(this.receiver);
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

	public void notifyPlaylist()
	{
		NavigationDrawerFragment fragment = (NavigationDrawerFragment)this.getFragmentManager().findFragmentByTag(NavigationDrawerFragment.TAG);
		fragment.getPlaylist().notifyDataSetChanged(true);
	}

	public void notifySoundSheetFragments()
	{
		Set<String> soundSheets = this.service.getSounds().keySet();
		for (String fragmentTag : soundSheets)
			this.notifyFragment(fragmentTag);
	}

	public void notifyFragment(String fragmentTag)
	{
		NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawer();
		SoundSheetFragment fragment = (SoundSheetFragment) this.getFragmentManager().findFragmentByTag(fragmentTag);
		if (fragment != null)
			fragment.notifyDataSetChanged(true);

		navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(false); // updates sound count in sound sheet list
	}

	private class ServiceLoadingReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action.equals(MusicService.ACTION_FINISHED_LOADING_PLAYLIST))
				notifyPlaylist();
			else if (action.equals(MusicService.ACTION_FINISHED_LOADING_SOUNDS))
				notifySoundSheetFragments();
		}
	}
}
