package org.neidhardt.dynamicsoundboard.misc

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import java.io.File
import java.util.*

/**
 * File created by eric.neidhardt on 09.04.2015.
 */
object FileUtils
{
	private val TAG = FileUtils::class.java.name

	val MIME_AUDIO = "audio/*|application/ogg|application/x-ogg"

	private val AUDIO = "audio"
	private val MIME_AUDIO_TYPES = arrayOf("audio/*", "application/ogg", "application/x-ogg")
	private val SCHEME_CONTENT_URI = "content"

	fun getFilesInDirectory(directory: File): MutableList<File>
	{
		val content = directory.listFiles()
		if (content == null || content.size == 0)
			return ArrayList()

		val files = ArrayList<File>(content.size)
		for (file in content) {
			if (file.isDirectory)
				files.add(file)
		}

		for (file in content) {
			if (!file.isDirectory)
				files.add(file)
		}

		return files
	}

	fun getFileForUri(uri: Uri): File?
	{
		val pathUri = getPathUriFromGenericUri(SoundboardApplication.context, uri) ?: return null

		val file = File(pathUri.path)
		if (!file.exists())
			return null

		return file
	}

	fun stripFileTypeFromName(fileName: String?): String
	{
		if (fileName == null)
			throw NullPointerException(TAG + ": cannot create new file name, either old name or new name is null")

		val segments = fileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		if (segments.size > 1) {
			var strippedName = segments[0]
			for (i in 1..segments.size - 1 - 1)
				strippedName += "." + segments[i]
			return strippedName
		} else
			return segments[0]
	}

	fun getFileNameFromUri(context: Context, uriString: String): String
	{
		return getFileNameFromUri(context, Uri.parse(uriString))
	}

	fun getFileNameFromUri(context: Context, uri: Uri): String {
		var fileName: String = "" // default fileName
		var filePathUri = uri
		if (uri.scheme != null && uri.scheme == SCHEME_CONTENT_URI) {
			val cursor = context.contentResolver.query(uri, null, null, null, null)
			if (cursor.moveToFirst()) {
				val column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
				filePathUri = Uri.parse(cursor.getString(column_index))
				fileName = filePathUri.lastPathSegment
			}
			cursor.close()
		} else
			fileName = filePathUri.lastPathSegment

		return fileName
	}

	private fun getPathUriFromGenericUri(context: Context, uri: Uri): Uri?
	{
		var filePathUri = uri
		if (uri.scheme != null && uri.scheme == SCHEME_CONTENT_URI) {
			val cursor = context.contentResolver.query(uri, null, null, null, null)
			if (cursor.moveToFirst()) {
				val column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
				filePathUri = Uri.parse(cursor.getString(column_index))
			}
			cursor.close()
		}
		return filePathUri
	}

	fun isAudioFile(file: File): Boolean
	{
		val mime = getMimeType(file.absolutePath) ?: return false
		if (mime.startsWith(AUDIO))
			return true
		for (audioMime in MIME_AUDIO_TYPES) {
			if (mime == audioMime)
				return true
		}
		return false
	}

	fun containsAudioFiles(directory: File): Boolean
	{
		val filesInDirectory = directory.listFiles() ?: return false
		for (file in filesInDirectory) {
			if (file.isDirectory)
				continue
			if (isAudioFile(file))
				return true
		}
		return false
	}

	fun getFileExtension(filePath: String): String?
	{
		var extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
		if (extension.isEmpty()) {
			val uri = Uri.parse(filePath)

			val fileName = uri.lastPathSegment
			extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length)
		}
		return extension
	}

	fun getMimeType(filePath: String): String?
	{
		var type: String? = null
		val extension = getFileExtension(filePath)
		if (extension != null) {
			val mime = MimeTypeMap.getSingleton()
			type = mime.getMimeTypeFromExtension(extension)
		}
		return type
	}

}
