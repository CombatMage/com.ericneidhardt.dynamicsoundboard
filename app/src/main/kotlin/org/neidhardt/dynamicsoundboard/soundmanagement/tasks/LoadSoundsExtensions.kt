package org.neidhardt.dynamicsoundboard.soundmanagement.tasks

import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import rx.Observable
import rx.schedulers.Schedulers

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
