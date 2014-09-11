package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.app.Fragment;
import android.os.Bundle;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Created by eric.neidhardt on 11.09.2014.
 */
public class SoundManagerFragment extends Fragment
{
	public static final String TAG = SoundManagerFragment.class.getSimpleName();

	private static final String DB_SOUNDS = "com.ericneidhardt.dynamicsoundboard.SoundManagerFragment.db_sounds";

	private Map<String, List<EnhancedMediaPlayer>> sounds;
	private DaoSession daoSession;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.sounds = new HashMap<String,  List<EnhancedMediaPlayer>>();
		this.daoSession = Util.setupDatabase(this.getActivity(), DB_SOUNDS);

		SafeAsyncTask task = new LoadMediaPlayerTask();
		task.execute();
	}

	@Override
	public void onPause() {
		super.onPause();

		SafeAsyncTask task = new UpdateMediaPlayersTask(this.sounds);
		task.execute();
	}

	public List<EnhancedMediaPlayer> get(String fragmentTag)
	{
		return this.sounds.get(fragmentTag);
	}

	public void add(String fragmentTag, MediaPlayerData mediaPlayerData)
	{
		this.add(fragmentTag, asList(mediaPlayerData));
	}

	public void add(String fragmentTag, List<MediaPlayerData> mediaPlayersData)
	{
		if (this.sounds.get(fragmentTag) == null)
			this.sounds.put(fragmentTag, new ArrayList<EnhancedMediaPlayer>());

		List<EnhancedMediaPlayer> players = new ArrayList<EnhancedMediaPlayer>();
		for (MediaPlayerData mediaPlayerData : mediaPlayersData)
			players.add(new EnhancedMediaPlayer(mediaPlayerData));

		this.storeMediaPlayerData(fragmentTag, mediaPlayersData);
		this.sounds.get(fragmentTag).addAll(players);
		this.notifyFragment(fragmentTag, players);
	}

	private void load(String fragmentTag, List<EnhancedMediaPlayer> loadedMediaPlayers)
	{
		if (this.sounds.get(fragmentTag) == null)
			this.sounds.put(fragmentTag, new ArrayList<EnhancedMediaPlayer>());
		this.sounds.get(fragmentTag).addAll(loadedMediaPlayers);
		this.notifyFragment(fragmentTag, loadedMediaPlayers);
	}

	private void notifyFragment(String fragmentTag,List<EnhancedMediaPlayer> loadedMediaPlayers)
	{
		SoundSheetFragment fragment = (SoundSheetFragment)this.getFragmentManager().findFragmentByTag(fragmentTag);
		if (fragment != null)
			fragment.notifyDataSetAdded(loadedMediaPlayers);
	}

	private void storeMediaPlayerData(String fragmentId, List<MediaPlayerData> mediaPlayersData)
	{
		SafeAsyncTask task = new StoreMediaPlayerTask(fragmentId, mediaPlayersData);
		task.execute();
	}

	private class UpdateMediaPlayersTask extends SafeAsyncTask<Void>
	{
		private List<MediaPlayerData> mediaPlayers;

		public UpdateMediaPlayersTask(Map<String, List<EnhancedMediaPlayer>> mediaPlayers)
		{
			this.mediaPlayers = new ArrayList<MediaPlayerData>();
			for (String fragmentTag : mediaPlayers.keySet())
			{
				List<EnhancedMediaPlayer> playersOfFragment = mediaPlayers.get(fragmentTag);
				for (EnhancedMediaPlayer player : playersOfFragment)
					this.mediaPlayers.add(player.getMediaPlayerData());
			}
		}

		@Override
		public Void call() throws Exception
		{
			daoSession.runInTx(new Runnable() {
				@Override
				public void run() {
					daoSession.getMediaPlayerDataDao().deleteAll();
					daoSession.getMediaPlayerDataDao().insertInTx(mediaPlayers);
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

	private class StoreMediaPlayerTask extends SafeAsyncTask<Void>
	{
		private List<MediaPlayerData> mediaPlayersData;

		public StoreMediaPlayerTask(String fragmentId, List<MediaPlayerData> mediaPlayersData)
		{
			this.mediaPlayersData = mediaPlayersData;
			for (MediaPlayerData mediaPlayerData : this.mediaPlayersData)
				mediaPlayerData.setFragmentTag(fragmentId);
		}

		@Override
		public Void call() throws Exception
		{
			daoSession.getMediaPlayerDataDao().insertInTx(this.mediaPlayersData);
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

	private class LoadMediaPlayerTask extends SafeAsyncTask<Map<String, List<EnhancedMediaPlayer>>>
	{
		@Override
		public Map<String, List<EnhancedMediaPlayer>> call() throws Exception
		{
			Map<String, List<EnhancedMediaPlayer>> loadedMediaPlayers = new HashMap<String, List<EnhancedMediaPlayer>>();

			List<MediaPlayerData> storedMediaPlayersData = daoSession.getMediaPlayerDataDao().queryBuilder().list();
			for (MediaPlayerData storedMediaPlayerData : storedMediaPlayersData)
			{
				String fragmentTag = storedMediaPlayerData.getFragmentTag();
				if (loadedMediaPlayers.get(fragmentTag) == null)
					loadedMediaPlayers.put(fragmentTag, new ArrayList<EnhancedMediaPlayer>());

				loadedMediaPlayers.get(fragmentTag).add(new EnhancedMediaPlayer(storedMediaPlayerData));
			}

			return loadedMediaPlayers;
		}

		@Override
		protected void onSuccess(Map<String, List<EnhancedMediaPlayer>> loadedMediaPlayers) throws Exception {
			super.onSuccess(loadedMediaPlayers);
			for (String fragmentTag : loadedMediaPlayers.keySet())
				load(fragmentTag, loadedMediaPlayers.get(fragmentTag));
		}

		@Override
		protected void onException(Exception e) throws RuntimeException
		{
			super.onException(e);
			Logger.e(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
