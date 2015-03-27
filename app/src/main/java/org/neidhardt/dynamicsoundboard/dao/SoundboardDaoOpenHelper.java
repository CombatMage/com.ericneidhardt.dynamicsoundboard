package org.neidhardt.dynamicsoundboard.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by eric.neidhardt on 27.03.2015.
 */
public class SoundboardDaoOpenHelper extends DaoMaster.OpenHelper
{
	public SoundboardDaoOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory)
	{
		super(context, name, factory);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");

		if (oldVersion <= 9)
			SoundLayoutDao.createTable(db, false);
		else {
			DaoMaster.dropAllTables(db, true);
			onCreate(db);
		}
	}
}
