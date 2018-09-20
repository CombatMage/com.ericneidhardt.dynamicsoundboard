package org.neidhardt.dynamicsoundboard.repositories

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.neidhardt.dynamicsoundboard.model.AppData
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.simplestorage.SimpleStorage
import java .io.File

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */
open class AppDataRepository(context: Context) : SimpleStorage<AppData>(context, AppData::class.java) {

	private val converter = Gson()

	fun save(list: List<SoundLayout>): Observable<AppData> {
		val data = AppData().apply { this.soundLayouts = list }
		this.prepareAppDataForSave(data)
		return super.save(data)
	}

	fun saveToFile(file: File, list: List<SoundLayout>): Observable<File> {
		val data = AppData().apply { this.soundLayouts = list }
		this.prepareAppDataForSave(data)

		return Observable.fromCallable {
			val json = this.converter.toJson(data)
			file.apply { this.writeText(json) }
		}.subscribeOn(Schedulers.io())
	}

	/**
	 * Certain properties should not be persisted, because thez are only temporary.
	 * This is done by this method.
	 */
	private fun prepareAppDataForSave(appData: AppData) {
		appData.soundLayouts?.forEach { soundLayout ->
			soundLayout.playList?.forEach { it.isSelectedForDeletion = false }
			soundLayout.soundSheets?.forEach { soundSheet ->
				soundSheet.isSelectedForDeletion = false
				soundSheet.mediaPlayers?.forEach { it.isSelectedForDeletion = false }
			}
		}
	}

	fun getFromFile(file: File): Observable<AppData?> {
		return Observable.fromCallable {
			val json = file.readText()
			this.converter.fromJson(json, AppData::class.java)
		}.subscribeOn(Schedulers.io())
	}
}