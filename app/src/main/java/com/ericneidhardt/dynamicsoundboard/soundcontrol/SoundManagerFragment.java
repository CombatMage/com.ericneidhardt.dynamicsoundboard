package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.misc.safeasyncTask.SafeAsyncTask;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetFragment;

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

	private static final String database_prefix = "db";

	private Map<String, List<EnhancedMediaPlayer>> sounds;
	private Map<String, DaoSession> soundDatabases;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.sounds = new HashMap<String,  List<EnhancedMediaPlayer>>();
		this.soundDatabases = new HashMap<String, DaoSession>();

		// TODO load data
	}

	@Override
	public void onPause() {
		super.onPause();

		// TODO store data
	}

	public DaoSession getDatabase(String id)
	{
		Context context = this.getActivity();
		if (context == null)
			return null;

		id = database_prefix + id;
		DaoSession database = this.soundDatabases.get(id);
		if (database == null)
		{
			database = Util.setupDatabase(context, id);
			this.soundDatabases.put(id, database);
		}
		return database;
	}

	public List<EnhancedMediaPlayer> get(String fragmentTag)
	{
		return this.sounds.get(fragmentTag);
	}

	public void add(String fragmentTag, MediaPlayerData mediaPlayerData)
	{
		EnhancedMediaPlayer player = new EnhancedMediaPlayer(mediaPlayerData);

		this.storeMediaPlayerData(fragmentTag, asList(mediaPlayerData));
		this.sounds.put(fragmentTag, asList(player));

		SoundSheetFragment fragment = (SoundSheetFragment)this.getFragmentManager().findFragmentByTag(fragmentTag);
		if (fragment != null)
			fragment.notifyDataSetAdded(asList(player));
	}

	private void storeMediaPlayerData(String fragmentId, List<MediaPlayerData> mediaPlayersData)
	{
		SafeAsyncTask task = new StoreMediaPlayerTask(fragmentId, mediaPlayersData);
		task.execute();
	}

	private class StoreMediaPlayerTask extends SafeAsyncTask<Void>
	{
		private String fragmentId;
		private List<MediaPlayerData> mediaPlayersData;

		public StoreMediaPlayerTask(String fragmentId, List<MediaPlayerData> mediaPlayersData)
		{
			this.fragmentId = fragmentId;
			this.mediaPlayersData = mediaPlayersData;
		}

		@Override
		public Void call() throws Exception
		{
			final DaoSession daoSession = getDatabase(this.fragmentId);
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

}
