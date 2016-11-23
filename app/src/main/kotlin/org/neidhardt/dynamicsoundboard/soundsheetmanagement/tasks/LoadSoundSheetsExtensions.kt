package org.neidhardt.dynamicsoundboard.soundsheetmanagement.tasks

import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import rx.Observable
import rx.Scheduler
import rx.schedulers.Schedulers

/**
 * Created by eric.neidhardt on 23.11.2016.
 */
fun DaoSession.loadSoundSheets(): Observable<List<SoundSheet>> {
	return Observable.fromCallable {
		this.soundSheetDao.queryBuilder().list()
	}.subscribeOn(Schedulers.computation())
}