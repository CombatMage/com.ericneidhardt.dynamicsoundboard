package org.neidhardt.dynamicsoundboard.soundmanagement_old;

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
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.events.PlaylistChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.events.PlaylistRemovedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.model.SoundDataModel;
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
	public void removeSounds(List<EnhancedMediaPlayer> soundsToRemove)
	{
		if (this.service == null)
			return;

		this.service.removeSounds(soundsToRemove);
	}

	@Override
	public void removeSoundsFromPlaylist(List<EnhancedMediaPlayer> soundsToRemove)
	{
		if (this.service == null)
			return;

		this.service.removeFromPlaylist(soundsToRemove);
		EventBus.getDefault().post(new PlaylistRemovedEvent(soundsToRemove));
	}

	@Override
	public boolean toggleSoundInPlaylist(String playerId, boolean addToPlayList)
	{
		if (this.service == null)
			return false;

		this.service.toggleSoundInPlaylist(playerId, addToPlayList);
		EventBus.getDefault().post(new PlaylistChangedEvent());

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
	public void moveSoundInFragment(String fragmentTag, int from, int to)
	{
		if (this.service == null)
			return;
		this.service.moveSoundInFragment(fragmentTag, from, to);
	}

	@Override
	public EnhancedMediaPlayer getSoundById(String fragmentTag, String playerId) {
		if (this.service == null)
			return null;
		return this.service.searchForId(fragmentTag, playerId);
	}

	@Override
	public void writeCachBack()
	{
		if (this.service == null)
			return;
		this.service.clearAndStoreSoundsAndPlayList();
	}

	@Override
	public void init()
	{
		if (this.service == null)
			return;
		this.service.initSoundsAndPlayList();
	}

	@Override
	public void onEvent(SoundSheetsFromFileLoadedEvent event)
	{
		List<SoundSheet> oldSoundSheets = event.getOldSoundSheetList();

		List<EnhancedMediaPlayer> playersToRemove = new ArrayList<>();
		for (SoundSheet soundSheet : oldSoundSheets)
			playersToRemove.addAll(this.getSoundsInFragment(soundSheet.getFragmentTag()));

		this.service.removeSounds(playersToRemove);
	}
}