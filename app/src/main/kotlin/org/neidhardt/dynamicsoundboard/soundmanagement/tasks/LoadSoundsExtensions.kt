package org.neidhardt.dynamicsoundboard.soundmanagement.tasks

import android.net.Uri
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.longtermtask.LoadListTask
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import rx.Observable
import rx.schedulers.Schedulers
import java.io.File

/**
 * Created by eric.neidhardt@sevenval.com on 23.11.2016.
 */
fun DaoSession.loadSoundsFromDatabase(): Observable<List<MediaPlayerData>> {
	return Observable.fromCallable({
		this.mediaPlayerDataDao.queryBuilder().list().sortedBy { it.sortOrder }
	}).subscribeOn(Schedulers.computation())
}

fun DaoSession.loadPlaylistFromDatabase(): Observable<List<MediaPlayerData>> {
	return Observable.fromCallable({
		this.mediaPlayerDataDao.queryBuilder().list().sortedBy { it.sortOrder }
	}).subscribeOn(Schedulers.computation())
}

fun List<File>.loadSounds(fragmentTag: String): Observable<List<MediaPlayerData>> {
	return Observable.fromCallable {
		this.map { getMediaPlayerDataFromFile(it, fragmentTag) }
	}.subscribeOn(Schedulers.computation())
}

private fun getMediaPlayerDataFromFile(file: File, fragmentTag: String): MediaPlayerData
{
	val soundUri = Uri.parse(file.absolutePath)
	val soundLabel = FileUtils.stripFileTypeFromName(
			FileUtils.getFileNameFromUri(SoundboardApplication.context, soundUri))
	return MediaPlayerData.getNewMediaPlayerData(fragmentTag, soundUri, soundLabel)
}
