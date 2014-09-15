package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import com.ericneidhardt.dynamicsoundboard.R;
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
		this.setHasOptionsMenu(true);

		this.sounds = new HashMap<String,  List<EnhancedMediaPlayer>>();
		this.daoSession = Util.setupDatabase(this.getActivity(), DB_SOUNDS);

		SafeAsyncTask task = new LoadMediaPlayerTask();
		task.execute();
	}

	@Override
	public void onPause()
	{
		super.onPause();

		SafeAsyncTask task = new UpdateMediaPlayersTask(this.sounds);
		task.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		super.onOptionsItemSelected(item);
		switch (item.getItemId())
		{
			case R.id.action_clear_all_sounds:
				for (String fragmentTag : this.sounds.keySet())
					this.remove(fragmentTag);

				return true;
			default:
				return false;
		}
	}

	public List<EnhancedMediaPlayer> get(String fragmentTag)
	{
		return this.sounds.get(fragmentTag);
	}

	public Map<String, List<EnhancedMediaPlayer>> getAll()
	{
		return this.sounds;
	}

	public void addMediaPlayerAndNotifyFragment(String fragmentTag, MediaPlayerData mediaPlayerData)
	{
		this.addMediaPlayersAndNotifyFragment(fragmentTag, asList(mediaPlayerData));
	}

	public void addMediaPlayersAndNotifyFragment(String fragmentTag, List<MediaPlayerData> mediaPlayersData)
	{
		if (fragmentTag == null)
			throw  new NullPointerException("addSoundSheetAndNotifyFragment: cannot addSoundSheetAndNotifyFragment media players to fragment, fragment tag is null");

		if (this.sounds.get(fragmentTag) == null)
			this.sounds.put(fragmentTag, new ArrayList<EnhancedMediaPlayer>());

		List<EnhancedMediaPlayer> players = new ArrayList<EnhancedMediaPlayer>();
		for (MediaPlayerData mediaPlayerData : mediaPlayersData)
			players.add(new EnhancedMediaPlayer(this.getActivity(), mediaPlayerData));

		this.storeMediaPlayerData(fragmentTag, mediaPlayersData);
		this.sounds.get(fragmentTag).addAll(players);
		this.notifyFragment(fragmentTag);
	}

	public void remove(String fragmentTag)
	{
		if (this.sounds.get(fragmentTag) == null)
			return;

		List<MediaPlayerData> mediaPlayersToRemove = new ArrayList<MediaPlayerData>();
		for (EnhancedMediaPlayer player : this.sounds.get(fragmentTag))
		{
			player.destroy();
			mediaPlayersToRemove.add(player.getMediaPlayerData());
		}

		this.sounds.remove(fragmentTag);
		this.notifyFragment(fragmentTag);

		SafeAsyncTask task = new RemoveMediaPlayersTask(mediaPlayersToRemove);
		task.execute();
	}

	private void load(String fragmentTag, List<EnhancedMediaPlayer> loadedMediaPlayers)
	{
		if (fragmentTag == null)
			throw  new NullPointerException("load: cannot addSoundSheetAndNotifyFragment media players to fragment, fragment tag is null");

		if (this.sounds.get(fragmentTag) == null)
			this.sounds.put(fragmentTag, new ArrayList<EnhancedMediaPlayer>());
		this.sounds.get(fragmentTag).addAll(loadedMediaPlayers);
		this.notifyFragment(fragmentTag);
	}

	private void notifyFragment(String fragmentTag)
	{
		SoundSheetFragment fragment = (SoundSheetFragment)this.getFragmentManager().findFragmentByTag(fragmentTag);
		if (fragment != null)
			fragment.notifyDataSetChanged(true);
	}

	private void storeMediaPlayerData(String fragmentId, List<MediaPlayerData> mediaPlayersData)
	{
		SafeAsyncTask task = new StoreMediaPlayerTask(fragmentId, mediaPlayersData);
		task.execute();
	}

	private class UpdateMediaPlayersTask extends SafeAsyncTask<Void>
	{
		private final String TAG = UpdateMediaPlayersTask.class.getSimpleName();
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
		private final String TAG = StoreMediaPlayerTask.class.getSimpleName();
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

	private class RemoveMediaPlayersTask extends SafeAsyncTask<Void>
	{
		private List<MediaPlayerData> mediaPlayersData;

		public RemoveMediaPlayersTask(List<MediaPlayerData> mediaPlayersData)
		{
			this.mediaPlayersData = mediaPlayersData;
		}

		@Override
		public Void call() throws Exception
		{
			daoSession.getMediaPlayerDataDao().deleteInTx(this.mediaPlayersData);
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
		private final String TAG = LoadMediaPlayerTask.class.getSimpleName();

		@Override
		public Map<String, List<EnhancedMediaPlayer>> call() throws Exception
		{
			Map<String, List<EnhancedMediaPlayer>> loadedMediaPlayers = new HashMap<String, List<EnhancedMediaPlayer>>();

			List<MediaPlayerData> storedMediaPlayersData = daoSession.getMediaPlayerDataDao().queryBuilder().list();
			for (MediaPlayerData storedMediaPlayerData : storedMediaPlayersData)
			{
				String fragmentTag = storedMediaPlayerData.getFragmentTag();
				if (fragmentTag == null)
				{
					Logger.e(TAG, "cannot load media player, fragment tag is null " + storedMediaPlayerData);
					continue;
				}
				if (loadedMediaPlayers.get(fragmentTag) == null)
					loadedMediaPlayers.put(fragmentTag, new ArrayList<EnhancedMediaPlayer>());

				loadedMediaPlayers.get(fragmentTag).add(new EnhancedMediaPlayer(getActivity(), storedMediaPlayerData));
			}

			return loadedMediaPlayers;
		}

		@Override
		protected void onSuccess(Map<String, List<EnhancedMediaPlayer>> loadedMediaPlayers) throws Exception
		{
			Logger.d(TAG, "onSuccess: with " + loadedMediaPlayers.keySet().size() + " fragments");
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
