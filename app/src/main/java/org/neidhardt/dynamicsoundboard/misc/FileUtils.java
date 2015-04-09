package org.neidhardt.dynamicsoundboard.misc;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 09.04.2015.
 */
public class FileUtils
{
	public static final String MIME_AUDIO = "audio/*|application/ogg|application/x-ogg";
	private static final String AUDIO = "audio";
	private static final String[] MIME_AUDIO_TYPES = {"audio/*", "application/ogg", "application/x-ogg"};
	private static final String SCHEME_CONTENT_URI = "content";
	private static final String SCHEME_FILE_URI = "file";

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

	public static String getFileNameFromUri(Context context, String uriString)
	{
		return getFileNameFromUri(context, Uri.parse(uriString));
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

}
