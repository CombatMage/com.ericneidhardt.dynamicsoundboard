package org.neidhardt.dynamicsoundboard.misc

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import java.io.File
import kotlin.comparisons.compareByDescending
import kotlin.comparisons.thenBy
import kotlin.comparisons.thenByDescending

/**
 * File created by eric.neidhardt on 09.04.2015.
 */
private val TAG = FileUtils::class.java.name

private val AUDIO = "audio"
private val MIME_AUDIO_TYPES = arrayOf("audio/*", "application/ogg", "application/x-ogg")
private val SCHEME_CONTENT_URI = "content"

fun File.getFilesInDirectorySortedAsync(): Observable<List<File>> {
	val content = this.listFiles()
	if (content == null || content.isEmpty()) return Observable.just(emptyList())

	return Observable.fromCallable {
		content.sortedWith(
				compareByDescending<File> { it.isDirectory }
						.thenByDescending { it.containsAudioFiles }
						.thenByDescending { it.isAudioFile }
						.thenBy { it.name }
		)
	}.subscribeOn(Schedulers.computation())
}

fun File.getFilesInDirectorySorted(): List<File> {
	val content = this.listFiles()
	if (content == null || content.isEmpty()) return emptyList()

	return content.sortedWith(
			compareByDescending<File> { it.isDirectory }
					.thenByDescending { it.containsAudioFiles }
					.thenByDescending { it.isAudioFile }
					.thenBy { it.name }
	)
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
		if (this.isDirectory) return false
		val mime = this.mimeType ?: return false
		if (mime.startsWith(AUDIO))
			return true
		return MIME_AUDIO_TYPES.contains(mime)
	}

val File.mimeType: String?
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
		if (!this.isDirectory) return false
		val filesInDirectory = this.listFiles() ?: return false
		return filesInDirectory.any { !it.isDirectory && it.isAudioFile }
	}

object FileUtils {
	val MIME_AUDIO = "audio/*|application/ogg|application/x-ogg"

	fun stripFileTypeFromName(fileName: String?): String {
		if (fileName == null)
			throw NullPointerException(TAG + ": cannot create new file name, either old name or new name is null")

		val segments = fileName.split("\\.".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
		return if (segments.size > 1) {
			var strippedName = segments[0]
			for (i in 1 until segments.size - 1)
				strippedName += "." + segments[i]
			strippedName
		} else {
			segments[0]
		}
	}

	fun getFileNameFromUri(context: Context, uriString: String): String =
			getFileNameFromUri(context, Uri.parse(uriString))

	fun getFileNameFromUri(context: Context, uri: Uri): String {
		var fileName = "" // default fileName
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
