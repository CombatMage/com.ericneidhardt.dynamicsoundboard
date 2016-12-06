package org.neidhardt.dynamicsoundboard.daohelper

import android.content.Context
import android.support.annotation.CheckResult
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.*
import org.neidhardt.dynamicsoundboard.misc.Logger
import rx.Observable
import rx.schedulers.Schedulers

/**
 * File created by eric.neidhardt on 30.06.2015.
 */
object GreenDaoHelper {
	fun setupDatabase(context: Context, dbName: String): DaoSession {
		val helper = SoundboardDaoOpenHelper(context, dbName)
		val db = helper.writableDatabase
		val daoMaster = DaoMaster(db)
		return daoMaster.newSession()
	}
}

@CheckResult
fun MediaPlayerData.insertAsync(): Observable<Unit> {
	val soundsDataUtil = SoundboardApplication.soundsDataUtil
	val soundsDataStorage = SoundboardApplication.soundsDataStorage
	val db =
			if (soundsDataUtil.isPlaylistPlayer(this))
				soundsDataStorage.getDbPlaylist()
			else
				soundsDataStorage.getDbSounds()

	return Observable.fromCallable {
		db.runInTx {
			val isInDatabase = db.mediaPlayerDataDao.queryBuilder()
					.where(MediaPlayerDataDao.Properties.PlayerId.eq(this.playerId))
					.list()
					.isNotEmpty()

			if (!isInDatabase)
				db.mediaPlayerDataDao.insert(this)
		}
	}.doOnError { error -> Logger.e(this.toString(), error.toString()) }.subscribeOn(Schedulers.computation())
}

@CheckResult
fun MediaPlayerData.updateAsync(): Observable<Unit> {
	val soundsDataUtil = SoundboardApplication.soundsDataUtil
	val soundsDataStorage = SoundboardApplication.soundsDataStorage
	val db =
			if (soundsDataUtil.isPlaylistPlayer(this))
				soundsDataStorage.getDbPlaylist()
			else
				soundsDataStorage.getDbSounds()

	return Observable.fromCallable {
		db.runInTx {
			val isInDatabase = db.mediaPlayerDataDao.queryBuilder()
					.where(MediaPlayerDataDao.Properties.PlayerId.eq(this.playerId))
					.list()
					.isNotEmpty()

			if (isInDatabase)
				db.mediaPlayerDataDao.update(this) // do not update if item was not added before
		}
	}.doOnError { error -> Logger.e(this.toString(), error.toString()) }.subscribeOn(Schedulers.computation())
}

@CheckResult
fun SoundSheet.insertAsync(): Observable<Unit> {
	val soundSheetsDataStorage = SoundboardApplication.soundSheetsDataStorage
	val db = soundSheetsDataStorage.getDbSoundSheets()

	return Observable.fromCallable {
		val isInDatabase = db.soundSheetDao.queryBuilder()
				.where(SoundSheetDao.Properties.FragmentTag.eq(this.fragmentTag))
				.list()
				.isNotEmpty()

		if (!isInDatabase)
			db.insert(this)
	}.doOnError { error -> Logger.e(this.toString(), error.toString()) }.subscribeOn(Schedulers.computation())
}

@CheckResult
fun SoundSheet.updateAsync(): Observable<Unit> {
	val soundSheetsDataStorage = SoundboardApplication.soundSheetsDataStorage
	val db = soundSheetsDataStorage.getDbSoundSheets()

	return Observable.fromCallable {
		val isInDatabase = db.soundSheetDao.queryBuilder()
				.where(SoundSheetDao.Properties.FragmentTag.eq(this.fragmentTag))
				.list()
				.isNotEmpty()

		if (isInDatabase)
			db.update(this)
	}.doOnError { error -> Logger.e(this.toString(), error.toString()) }.subscribeOn(Schedulers.computation())
}