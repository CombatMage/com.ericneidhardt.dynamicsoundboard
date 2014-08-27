package com.ericneidhardt.dynamicsoundboard.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ericneidhardt.dynamicsoundboard.dao.DaoMaster;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class Storage
{
	private static final String DB_NAME = "dynamic_soundboard.db";

	public static DaoSession setupDatabase(Context context)
	{
		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}
}
