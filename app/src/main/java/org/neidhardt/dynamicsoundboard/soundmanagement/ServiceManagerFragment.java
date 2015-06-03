package org.neidhardt.dynamicsoundboard.soundmanagement;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundDataModel;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.OnSoundSheetsFromFileLoadedEventListener;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.events.SoundSheetsFromFileLoadedEvent;

import java.util.*;

/**
 * File created by eric.neidhardt on 02.12.2014.
 */
public class ServiceManagerFragment
		extends
			BaseFragment
		implements
			ServiceConnection,
			SoundDataModel,
			OnSoundSheetsFromFileLoadedEventListener
{
	public static final String TAG = ServiceManagerFragment.class.getName();

	private static SoundDataModel soundDataModel = null;
	public static SoundDataModel getSoundDataModel()
	{
		return soundDataModel;
	}

	private boolean isServiceBound = false;

	private MusicService service;
	public MusicService getSoundService()
	{
		return this.service;
	}

	public ServiceManagerFragment()
	{
		soundDataModel = this;
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

	@Override
	public boolean toggleSoundInPlaylist(String playerId, boolean addToPlayList)
	{
		if (this.service == null)
			return false;

		this.service.toggleSoundInPlaylist(playerId, addToPlayList);
		return true;
	}

	@Override
	public List<EnhancedMediaPlayer> getPlayList()
	{
		if (this.service == null || this.service.getPlaylist() == null)
			return new ArrayList<>();
		return this.service.getPlaylist();
	}

	@Override
	public Map<String, List<EnhancedMediaPlayer>> getSounds()
	{
		if (this.service == null || this.service.getSounds() == null)
			return new HashMap<>();
		return this.service.getSounds();
	}

	@Override
	public List<EnhancedMediaPlayer> getSoundsInFragment(String fragmentTag)
	{
		if (this.service == null || this.service.getSounds() == null || this.service.getSounds().get(fragmentTag) == null)
			return new ArrayList<>();
		return this.getSounds().get(fragmentTag);
	}

	@Override
	public Set<EnhancedMediaPlayer> getCurrentlyPlayingSounds()
	{
		if (this.service == null || this.service.getCurrentlyPlayingSounds() == null)
			return new HashSet<>();
		return this.service.getCurrentlyPlayingSounds();
	}

	@Override
	public void onEvent(SoundSheetsFromFileLoadedEvent event)
	{
		List<SoundSheet> newSoundSheets = event.getSoundSheetList();
		Set<String> fragmentTagsForSounds = this.service.getSounds().keySet();
		List<EnhancedMediaPlayer> playersToRemove = new ArrayList<>();

		for (String fragmentTag : fragmentTagsForSounds)
		{
			if (!doesSoundSheetsContainFragmentTag(newSoundSheets, fragmentTag))
				playersToRemove.addAll(this.getSoundsInFragment(fragmentTag));
		}

		this.service.removeSounds(playersToRemove);
	}

	private boolean doesSoundSheetsContainFragmentTag(List<SoundSheet> searchedSoundSheets, String fragmentTag)
	{
		for (SoundSheet soundSheet : searchedSoundSheets)
		{
			if (soundSheet.getFragmentTag().equals(fragmentTag))
				return true;
		}
		return false;
	}

	public void notifyPlaylist()
	{
		EventBus.getDefault().post(new PlaylistChangedEvent());
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
