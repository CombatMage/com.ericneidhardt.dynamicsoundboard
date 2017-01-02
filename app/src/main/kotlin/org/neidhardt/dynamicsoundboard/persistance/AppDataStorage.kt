package org.neidhardt.dynamicsoundboard.persistance

import android.content.Context
import com.google.gson.Gson
import com.sevenval.simplestorage.SimpleStorage
import org.neidhardt.dynamicsoundboard.persistance.model.AppData
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundLayout
import rx.Observable
import rx.schedulers.Schedulers
import java.io.File

/**
 * Created by eric.neidhardt@gmail.com on 19.12.2016.
 */
class AppDataStorage(context: Context) : SimpleStorage<AppData>(context, AppData::class.java) {

	private val converter = Gson()

	fun save(list: List<NewSoundLayout>): Observable<AppData> {
		val data = AppData().apply {
			this.soundLayouts = list
		}
		return super.save(data)
	}

	fun saveToFile(file: File, list: List<NewSoundLayout>): Observable<File> {
		val data = AppData().apply {
			this.soundLayouts = list
		}

		return Observable.fromCallable {
			val json = this.converter.toJson(data)
			file.apply { this.writeText(json) }
		}.subscribeOn(Schedulers.io())
	}

	fun loadFromFile(file: File): Observable<List<NewSoundLayout>> {
		return Observable.fromCallable {
			val json = file.readText()
			val appData = this.converter.fromJson(json, AppData::class.java)
			appData.soundLayouts
		}
	}
}