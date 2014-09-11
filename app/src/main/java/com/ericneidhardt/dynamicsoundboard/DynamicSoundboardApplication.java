package com.ericneidhardt.dynamicsoundboard;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.MediaPlayerPool;
import com.ericneidhardt.dynamicsoundboard.misc.Util;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
@ReportsCrashes(
	formKey = "", // This is required for backward compatibility but not used
	mailTo = "eric@neidhardt-erkner.de"
)
public class DynamicSoundboardApplication extends Application
{
	private static final String database_prefix = "db";

	private static Random random;
	private static Context applicationContext;
	private static Map<String, DaoSession> databases;

	@Override
	public void onCreate()
	{
		super.onCreate();
		ACRA.init(this);

		databases = new HashMap<String, DaoSession>();
		random = new Random();
		applicationContext = this;
	}

	public static void storeSoundInDatabase(String id, Uri soundUri, String soundLabel)
	{
		MediaPlayerPool pool = new MediaPlayerPool(id);
		pool.addRawData(EnhancedMediaPlayer.getMediaPlayerData(soundUri, soundLabel));
	}

	public static Context getContext()
	{
		return applicationContext;
	}

	public static DaoSession getDatabase(String id)
	{
		id = database_prefix + id;
		DaoSession database = databases.get(id);
		if (database == null)
		{
			database = Util.setupDatabase(applicationContext, id);
			databases.put(id, database);
		}
		return database;
	}

	public static int getRandomNumber()
	{
		return random.nextInt(Integer.MAX_VALUE);
	}
}
