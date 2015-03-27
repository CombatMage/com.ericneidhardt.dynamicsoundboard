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

	public static final String MIME_AUDIO = "audio/*|application/ogg|application/x-ogg";
	private static final String AUDIO = "audio";
	private static final String[] MIME_AUDIO_TYPES = {"audio/*", "application/ogg", "application/x-ogg"};

	private static final String SCHEME_CONTENT_URI = "content";
	private static final String SCHEME_FILE_URI = "file";

	public static DaoSession setupDatabase(Context context, String dbName)
	{
		DaoMaster.OpenHelper helper = new SoundboardDaoOpenHelper(context, dbName, null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		return daoMaster.newSession();
	}

	public static String getFileNameFromUri(Context context, Uri uri)
	{
		String fileName = null; // default fileName
		Uri filePathUri = uri;
		if (uri.getScheme() != null && uri.getScheme().equals(SCHEME_CONTENT_URI))
		{
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor.moveToFirst())
			{
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
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
			return new ArrayList<>();

		List<File> files = new ArrayList<>(content.length);
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
