package org.neidhardt.dynamicsoundboard.soundmanagement;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.BaseFragment;
import org.neidhardt.dynamicsoundboard.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlayListLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsLoadedEvent;

import java.util.*;

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

		EventBus.getDefault().register(this);

		this.startSoundManagerService();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (this.isServiceBound)
			this.getActivity().unbindService(this);

		EventBus.getDefault().unregister(this);
	}

	/**
	 * This is called by greenDao EventBus in case loading the playlist from MusicService has finished
	 * @param event delivered PlayListLoadedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(PlayListLoadedEvent event)
	{
		this.notifyPlaylist();
	}

	/**
	 * This is called by greenDao EventBus in case sound loading from MusicService has finished
	 * @param event delivered SoundsLoadedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(SoundsLoadedEvent event)
	{
		this.notifySoundSheetFragments();
	}

	public void onUserLeaveHint()
	{
		if (this.service != null)
			this.service.onActivityClosed();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		Logger.d(TAG, "onServiceConnected: " + name);
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

		navigationDrawerFragment.getSoundSheetsAdapter().notifyDataSetChanged(); // updates sound count in sound sheet list
	}
}
