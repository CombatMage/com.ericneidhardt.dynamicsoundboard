package org.neidhardt.dynamicsoundboard.soundmanagement;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import org.neidhardt.dynamicsoundboard.BaseFragment;
import org.neidhardt.dynamicsoundboard.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;

import java.util.*;

/**
 * File created by eric.neidhardt on 02.12.2014.
 */
public class ServiceManagerFragment extends BaseFragment implements ServiceConnection
{
	public static final String TAG = ServiceManagerFragment.class.getName();

	private ServiceLoadingReceiver receiver;

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

		this.receiver = new ServiceLoadingReceiver();
	}

	private void startSoundManagerService()
	{
		this.getActivity().bindService(new Intent(this.getActivity(), MusicService.class), this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		this.registerReceiver();
		this.startSoundManagerService();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (this.isServiceBound)
			this.getActivity().unbindService(this);
		LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(this.receiver);
	}

	public void onUserLeaveHint()
	{
		if (this.service != null)
			this.service.onActivityClosed();
	}

	private void registerReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicService.ACTION_FINISHED_LOADING_PLAYLIST);
		filter.addAction(MusicService.ACTION_FINISHED_LOADING_SOUNDS);
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(this.receiver, filter);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		Logger.d(TAG, "onServiceConnected: " + name);
		MusicService.Binder binder = (MusicService.Binder) service;
		this.service = binder.getService();
		this.isServiceBound = true;
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
		NavigationDrawerFragment fragment = (NavigationDrawerFragment)this.getFragmentManager().findFragmentByTag(NavigationDrawerFragment.TAG);
		fragment.getPlaylist().notifyDataSetChanged();
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
			fragment.notifyDataSetChanged();

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
