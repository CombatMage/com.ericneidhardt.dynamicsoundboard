package com.ericneidhardt.dynamicsoundboard.storage;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import com.ericneidhardt.dynamicsoundboard.NavigationDrawerFragment;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class SoundManagerFragment extends Fragment
{
	public static final String TAG = SoundManagerFragment.class.getName();

	public static final String ACTION_SOUND_CONTROL = "com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment.ACTION_SOUND_CONTROL";
	public static final String KEY_SOUND_ID = "com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment.KEY_SOUND_ID";

	private static final String DB_SOUNDS = "com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds";
	private static final String DB_SOUNDS_PLAYLIST = "com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment.db_sounds_playlist";

	private BroadcastReceiver receiver = new SoundControlReceiver();

	private DaoSession dbPlaylist;
	private List<EnhancedMediaPlayer> playList;
	public List<EnhancedMediaPlayer> getPlayList()
	{
		return playList;
	}

	private DaoSession dbSounds;
	private Map<String, List<EnhancedMediaPlayer>> sounds;
	public Map<String, List<EnhancedMediaPlayer>> getSounds()
	{
		return sounds;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.playList = new ArrayList<EnhancedMediaPlayer>();
		this.sounds = new HashMap<String, List<EnhancedMediaPlayer>>();

		this.dbPlaylist = Util.setupDatabase(this.getActivity(), DB_SOUNDS_PLAYLIST);
		this.dbSounds = Util.setupDatabase(this.getActivity(), DB_SOUNDS);

		SafeAsyncTask task = new LoadSoundsTask();
		task.execute();

		task = new LoadPlaylistTask();
		task.execute();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(this.receiver, new IntentFilter(ACTION_SOUND_CONTROL));
	}

	@Override
	public void onPause()
	{
		super.onPause();

		SafeAsyncTask task = new UpdateSoundsTask(this.sounds, dbSounds);
		task.execute();

		task = new UpdateSoundsTask(this.playList, dbPlaylist);
		task.execute();
	}

	@Override
	public void onDestroy()
	{
		LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(this.receiver);
		super.onDestroy();
	}

	public void addSound(MediaPlayerData playerData)
	{
		try
		{
			EnhancedMediaPlayer player = new EnhancedMediaPlayer(this.getActivity(), playerData);
			if (this.sounds.get(playerData.getFragmentTag()) == null)
				this.sounds.put(playerData.getFragmentTag(), new ArrayList<EnhancedMediaPlayer>(asList(player)));
			else
				this.sounds.get(playerData.getFragmentTag()).add(player);
		}
		catch (IOException e)
		{
			Logger.d(TAG, e.getMessage());
		}
	}

	public void removeSounds(String fragmentTag)
	{
		this.removeSounds(this.sounds.get(fragmentTag));
	}

	public void removeSounds(List<EnhancedMediaPlayer> soundsToRemove)
	{
		if (soundsToRemove == null || soundsToRemove.size() == 0)
			return;

		List<EnhancedMediaPlayer> copyList = new ArrayList<EnhancedMediaPlayer>(soundsToRemove.size());
		copyList.addAll(soundsToRemove); // this is done to prevent concurrent modification exception

		for (EnhancedMediaPlayer playerToRemove : copyList)
		{
			MediaPlayerData data = playerToRemove.getMediaPlayerData();
			this.sounds.get(data.getFragmentTag()).remove(playerToRemove);

			if (data.getIsInPlaylist())
			{
				EnhancedMediaPlayer correspondingPlayerInPlaylist = this.findInPlaylist(data.getPlayerId());
				this.playList.remove(correspondingPlayerInPlaylist);

				correspondingPlayerInPlaylist.destroy();
			}
			playerToRemove.destroy();
		}
	}

	public void addSoundToPlaylist(MediaPlayerData playerData)
	{
		try
		{
			EnhancedMediaPlayer player = EnhancedMediaPlayer.getInstanceForPlayList(this.getActivity(), playerData);
			this.playList.add(player);
		}
		catch (IOException e)
		{
			Logger.d(TAG, e.getMessage());
		}
	}

	public void toggleSoundInPlaylist(String playerId, boolean addToPlayList)
	{
		try
		{
			EnhancedMediaPlayer player = this.findInSounds(playerId);
			EnhancedMediaPlayer playerInPlaylist = this.findInPlaylist(playerId);

			if (addToPlayList)
			{
				if (playerInPlaylist != null)
					return;

				player.setIsInPlaylist(true);
				playerInPlaylist = EnhancedMediaPlayer.getInstanceForPlayList(this.getActivity(), player.getMediaPlayerData());
				this.playList.add(playerInPlaylist);
			}
			else
			{
				if (playerInPlaylist == null)
					return;

				player.setIsInPlaylist(false);
				this.playList.remove(playerInPlaylist);
				playerInPlaylist.destroy();
			}
		}
		catch (IOException e)
		{
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public void removeFromPlaylist(List<EnhancedMediaPlayer> playersToRemove)
	{
		for (EnhancedMediaPlayer player : playersToRemove)
			this.toggleSoundInPlaylist(player.getMediaPlayerData().getPlayerId(), false);
	}

	private EnhancedMediaPlayer findInPlaylist(String playerId)
	{
		for (EnhancedMediaPlayer player : this.playList)
		{
			if (player.getMediaPlayerData().getPlayerId().equals(playerId))
				return player;
		}
		return null;
	}

	private EnhancedMediaPlayer findInSounds(String playerId)
	{
		for (String fragmentTag : this.sounds.keySet())
		{
			if (this.sounds.get(fragmentTag) == null)
				continue;
			for (EnhancedMediaPlayer player : this.sounds.get(fragmentTag))
			{
				if (player.getMediaPlayerData().getPlayerId().equals(playerId))
					return player;
			}
		}
		return null;
	}

	public void notifySoundSheetFragments()
	{
		for (String fragmentTag : this.sounds.keySet())
			this.notifyFragment(fragmentTag);
	}

	public void notifyPlaylist()
	{
		NavigationDrawerFragment fragment = (NavigationDrawerFragment)this.getFragmentManager().findFragmentByTag(NavigationDrawerFragment.TAG);
		fragment.getPlaylist().notifyDataSetChanged(true);
	}

	public void notifySoundSheetList()
	{
		NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)this.getFragmentManager()
				.findFragmentByTag(NavigationDrawerFragment.TAG);
		navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(false); // updates sound count in sound sheet list
	}

	public void notifyFragment(String fragmentTag)
	{
		NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)this.getFragmentManager()
				.findFragmentByTag(NavigationDrawerFragment.TAG);

		SoundSheetFragment fragment = (SoundSheetFragment) this.getFragmentManager().findFragmentByTag(fragmentTag);
		if (fragment != null)
			fragment.notifyDataSetChanged(true);

		navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(false); // updates sound count in sound sheet list
	}

	private class LoadSoundsTask extends LoadTask<MediaPlayerData>
{
	@Override
	public List<MediaPlayerData> call() throws Exception
	{
		return dbSounds.getMediaPlayerDataDao().queryBuilder().list();
	}

	@Override
	protected void onSuccess(List<MediaPlayerData> mediaPlayersData) throws Exception
	{
		super.onSuccess(mediaPlayersData);
		for (MediaPlayerData mediaPlayerData : mediaPlayersData)
			addSound(mediaPlayerData);
		notifySoundSheetFragments();
	}
}

	private class LoadPlaylistTask extends LoadTask<MediaPlayerData>
	{
		@Override
		public List<MediaPlayerData> call() throws Exception
		{
			return dbPlaylist.getMediaPlayerDataDao().queryBuilder().list();
		}

		@Override
		protected void onSuccess(List<MediaPlayerData> mediaPlayersData) throws Exception
		{
			super.onSuccess(mediaPlayersData);
			for (MediaPlayerData mediaPlayerData : mediaPlayersData)
				addSoundToPlaylist(mediaPlayerData);
			notifyPlaylist();
		}
	}

	private abstract class LoadTask<T> extends SafeAsyncTask<List<T>>
	{
		@Override
		protected void onSuccess(List<T> ts) throws Exception
		{
			super.onSuccess(ts);
			Logger.d(TAG, "onSuccess: with " + ts.size());
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private class UpdateSoundsTask extends SafeAsyncTask<Void>
	{
		private List<MediaPlayerData> mediaPlayers;
		private DaoSession database;

		public UpdateSoundsTask(Map<String, List<EnhancedMediaPlayer>> mediaPlayers, DaoSession database)
		{
			this.database = database;
			this.mediaPlayers = new ArrayList<MediaPlayerData>();
			for (String fragmentTag : mediaPlayers.keySet())
			{
				List<EnhancedMediaPlayer> playersOfFragment = mediaPlayers.get(fragmentTag);
				for (EnhancedMediaPlayer player : playersOfFragment)
					this.mediaPlayers.add(player.getMediaPlayerData());
			}
		}

		public UpdateSoundsTask(List<EnhancedMediaPlayer> mediaPlayers, DaoSession database)
		{
			this.database = database;
			this.mediaPlayers = new ArrayList<MediaPlayerData>();
			for (EnhancedMediaPlayer player : mediaPlayers)
				this.mediaPlayers.add(player.getMediaPlayerData());
		}

		@Override
		public Void call() throws Exception
		{
			this.database.runInTx(new Runnable() {
				@Override
				public void run() {
					database.getMediaPlayerDataDao().deleteAll();
					database.getMediaPlayerDataDao().insertInTx(mediaPlayers);
				}
			});
			return null;
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private class SoundControlReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			String mediaPlayerId = intent.getStringExtra(KEY_SOUND_ID);

			if (action.equals(ACTION_SOUND_CONTROL))
			{
				Toast.makeText(context, action + " playerId " + mediaPlayerId, Toast.LENGTH_SHORT).show();
				// TODO
			}
		}
	}
}
