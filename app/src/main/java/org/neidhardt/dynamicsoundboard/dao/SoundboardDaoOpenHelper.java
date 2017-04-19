package org.neidhardt.dynamicsoundboard.dao;

import android.content.Context;
import org.greenrobot.greendao.database.Database;
import org.neidhardt.dynamicsoundboard.misc.Logger;

/**
 * File created by eric.neidhardt on 27.03.2015.
 */
public class SoundboardDaoOpenHelper extends DaoMaster.OpenHelper
{
	private static final String TAG = SoundboardDaoOpenHelper.class.getName();

	public SoundboardDaoOpenHelper(Context context, String name) {
		super(context, name);
	}

	@Override
	public void onUpgrade(Database db, int oldVersion, int newVersion) {
		Logger.INSTANCE.d(TAG, "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");

		DaoMaster.dropAllTables(db, true);
		onCreate(db);
	}
}
