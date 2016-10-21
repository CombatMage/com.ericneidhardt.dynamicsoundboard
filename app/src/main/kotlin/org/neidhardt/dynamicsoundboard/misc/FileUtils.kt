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
private val TAG = FileUtils::class.java.name

private val AUDIO = "audio"
private val MIME_AUDIO_TYPES = arrayOf("audio/*", "application/ogg", "application/x-ogg")
private val SCHEME_CONTENT_URI = "content"

fun File.getFilesInDirectory(): MutableList<File> {
	val content = this.listFiles()
	if (content == null || content.size == 0)
		return ArrayList()

	val files = ArrayList<File>(content.size)
	for (file in content)
		files.add(file)

	return files
}

fun Uri.getFileForUri(): File? {
	val pathUri = FileUtils.getPathUriFromGenericUri(SoundboardApplication.context, this) ?: return null

	val file = File(pathUri.path)
	if (!file.exists())
		return null

	return file
}

val File.isAudioFile: Boolean
	get() {
		val mime = this.getMimeType ?: return false
		if (mime.startsWith(AUDIO))
			return true
		for (audioMime in MIME_AUDIO_TYPES) {
			if (mime == audioMime)
				return true
		}
		return false
	}

val File.getMimeType: String?
	get() {
		var type: String? = null
		val extension = FileUtils.getFileExtension(this.absolutePath)
		if (extension != null) {
			val mime = MimeTypeMap.getSingleton()
			type = mime.getMimeTypeFromExtension(extension)
		}
		return type
	}

val File.containsAudioFiles: Boolean
	get() {
		val filesInDirectory = this.listFiles() ?: return false
		for (file in filesInDirectory) {
			if (file.isDirectory)
				continue
			if (file.isAudioFile)
				return true
		}
		return false
	}

object FileUtils {
	val MIME_AUDIO = "audio/*|application/ogg|application/x-ogg"

	fun stripFileTypeFromName(fileName: String?): String {
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

	fun getFileNameFromUri(context: Context, uriString: String): String {
		return getFileNameFromUri(context, Uri.parse(uriString))
	}

	fun getFileNameFromUri(context: Context, uri: Uri): String {
		var fileName: String = "" // default fileName
		var filePathUri = uri
		if (uri.scheme != null && uri.scheme == SCHEME_CONTENT_URI) {
			context.contentResolver.query(uri, null, null, null, null).use { cursor ->
				if (cursor.moveToFirst()) {
					val column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
					filePathUri = Uri.parse(cursor.getString(column_index))
					fileName = filePathUri.lastPathSegment
				}
			}
		} else
			fileName = filePathUri.lastPathSegment

		return fileName
	}

	internal fun getPathUriFromGenericUri(context: Context, uri: Uri): Uri? {
		var filePathUri = uri
		if (uri.scheme != null && uri.scheme == SCHEME_CONTENT_URI) {
			context.contentResolver.query(uri, null, null, null, null).use { cursor ->
				if (cursor.moveToFirst()) {
					val column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
					filePathUri = Uri.parse(cursor.getString(column_index))
				}
			}
		}
		return filePathUri
	}

	internal fun getFileExtension(filePath: String): String? {
		var extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
		if (extension.isEmpty()) {
			val uri = Uri.parse(filePath)

			val fileName = uri.lastPathSegment
			extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length)
		}
		return extension
	}

}
