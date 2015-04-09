package org.neidhardt.dynamicsoundboard.misc;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import org.neidhardt.dynamicsoundboard.dao.DaoMaster;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundboardDaoOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Project created by Eric Neidhardt on 30.08.2014.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class Util
{
	public static final boolean IS_LOLLIPOP_AVAILABLE = isLollipopAvailable();
	public static final boolean IS_KITKAT_AVAILABLE = isKitKatAvailable();

	public static final int SYSTEM_UI_FULL_IMMERSE = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
			| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
			| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

	public static final int SYSTEM_UI_NON_IMMERSE = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

	public static DaoSession setupDatabase(Context context, String dbName)
	{
		DaoMaster.OpenHelper helper = new SoundboardDaoOpenHelper(context, dbName, null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}

	private static boolean isLollipopAvailable()
	{
		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		return currentApiVersion >= Build.VERSION_CODES.LOLLIPOP;
	}

	private static boolean isKitKatAvailable()
	{
		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		return currentApiVersion >= Build.VERSION_CODES.KITKAT;
	}
}
