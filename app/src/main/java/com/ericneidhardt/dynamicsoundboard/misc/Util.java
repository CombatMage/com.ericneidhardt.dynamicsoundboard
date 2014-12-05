package com.ericneidhardt.dynamicsoundboard.misc;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import com.ericneidhardt.dynamicsoundboard.dao.DaoMaster;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Project created by Eric Neidhardt on 30.08.2014.
 */
public class Util
{
	public static final String MIME_AUDIO = "audio/*|application/ogg|application/x-ogg";
	private static final String AUDIO = "audio";
	private static final String[] MIME_AUDIO_TYPES = {"audio/*", "application/ogg", "application/x-ogg"};

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
		if (uri.getScheme() != null && uri.getScheme().equals(SCHEME_CONTENT_URI))
		{
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor.moveToFirst())
			{
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
				filePathUri = Uri.parse(cursor.getString(column_index));
				fileName = filePathUri.getLastPathSegment();
			}
			cursor.close();
		}
		else if (uri.getScheme() != null && uri.getScheme().equals(SCHEME_FILE_URI))
			fileName = filePathUri.getLastPathSegment();
		else
			fileName = "_" + filePathUri.getLastPathSegment();

		return fileName;
	}

	public static boolean isAudioFile(File file)
	{
		String mime = getMimeType(file.getAbsolutePath());
		if (mime == null)
			return false;
		if (mime.startsWith(AUDIO))
			return true;
		for (String audioMime : MIME_AUDIO_TYPES)
		{
			if (mime.equals(audioMime))
				return true;
		}
		return false;
	}

	public static boolean containsAudioFiles(File directory)
	{
		File[] filesInDirectory = directory.listFiles();
		if (filesInDirectory == null)
			return false;
		for (File file : filesInDirectory)
		{
			if (file.isDirectory())
				continue;
			if (isAudioFile(file))
				return true;
		}
		return false;
	}

	public static String getFileExtension(String filePath)
	{
		String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
		if (extension.isEmpty())
		{
			Uri uri = Uri.parse(filePath);

			String fileName = uri.getLastPathSegment();
			extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
		}
		return extension;
	}

	public static String getMimeType(String filePath)
	{
		String type = null;
		String extension = getFileExtension(filePath);
		if (extension != null)
		{
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		}
		return type;
	}

	public static List<File> getFilesInDirectory(File directory)
	{
		File[] content = directory.listFiles();
		if (content == null || content.length == 0)
			return new ArrayList<File>();

		List<File> files = new ArrayList<File>(content.length);
		for (File file : content)
		{
			if (file.isDirectory())
				files.add(file);
		}

		for (File file : content)
		{
			if (!file.isDirectory())
				files.add(file);
		}

		return files;
	}

	public static Bitmap getBitmap(Context context, int drawableId)
	{
		return BitmapFactory.decodeResource(context.getResources(), drawableId);
	}
}
