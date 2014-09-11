package com.ericneidhardt.dynamicsoundboard.misc;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import com.ericneidhardt.dynamicsoundboard.dao.DaoMaster;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;

/**
 * Created by Eric Neidhardt on 30.08.2014.
 */
public class Util
{
	public static final String MIME_AUDIO = "audio/*|application/ogg|application/x-ogg";
	private static final String SCHEME_CONTENT_URI = "content";
	private static final String SCHEME_FILE_URI = "file";

	public static DaoSession setupDatabase(Context context, String dbName)
	{
		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbName, null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}

	public static String getFileNameFromUri(Context context, Uri uri)
	{
		String fileName = null;//default fileName
		Uri filePathUri = uri;
		if (uri.getScheme().toString().equals(SCHEME_CONTENT_URI))
		{
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor.moveToFirst())
			{
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
				filePathUri = Uri.parse(cursor.getString(column_index));
				fileName = filePathUri.getLastPathSegment().toString();
			}
		}
		else if (uri.getScheme().equals(SCHEME_FILE_URI))
			fileName = filePathUri.getLastPathSegment().toString();
		else
			fileName = fileName+"_"+filePathUri.getLastPathSegment();

		return fileName;
	}

}
