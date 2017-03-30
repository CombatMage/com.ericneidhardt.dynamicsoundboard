package com.sevenval.simplestorage

import android.content.Context
import android.preference.PreferenceManager
import android.support.annotation.CheckResult
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by eric.neidhardt on 28.11.2016.
 */
open class SimpleStorage<T>(context: Context, private val classOfT: Class<T>) {

	private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
	private val converter = Gson()

	private var cachedData: T? = null

	val storageKey = "SimpleStorage_${classOfT.name}"

	@CheckResult
	fun save(data: T): Observable<T> {
		return Observable.fromCallable {
			this.cachedData = data
			val json = converter.toJson(data)
			this.sharedPreferences.edit()
					.putString(storageKey, json)
					.apply()
			data
		}.subscribeOn(Schedulers.computation())
	}

	@CheckResult
	fun get(): Observable<T?> {
		this.cachedData?.let { cachedData ->
			return Observable.just(cachedData)
		}

		return Observable.create<T?> { subscriber ->
			val data = this.getSync()
			subscriber.onNext(data)
			subscriber.onComplete()
		}
	}

	fun clear() {
		this.cachedData = null
		this.sharedPreferences.edit()
				.remove(storageKey)
				.apply()
	}

	private fun getSync(): T? =
			this.converter.fromJson(this.sharedPreferences.getString(storageKey, null), this.classOfT)
}