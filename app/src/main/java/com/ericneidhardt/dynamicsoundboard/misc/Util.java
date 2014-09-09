package com.ericneidhardt.dynamicsoundboard.misc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.ericneidhardt.dynamicsoundboard.dao.DaoMaster;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;

/**
 * Created by Eric Neidhardt on 30.08.2014.
 */
public class Util
{
	public static final String MIME_AUDIO = "audio/*|application/ogg|application/x-ogg";

	public static DaoSession setupDatabase(Context context, String dbName)
	{
		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbName, null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}

	public static String getFileNameFromUri(Uri uri)
	{
		return uri.getLastPathSegment().toString();
	}
}
