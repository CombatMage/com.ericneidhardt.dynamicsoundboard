package com.sevenval.simplestorage

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.annotation.CheckResult
import android.text.TextUtils
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by eric.neidhardt on 28.11.2016.
 */
class SimpleListStorage<T>(context: Context, private val classOfT: Class<T>) {

	private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
	private val converter = Gson()

	val storageKey = "SimpleStorage_List_${classOfT.name}"

	@CheckResult
	fun save(data: List<T>): Observable<List<T>> {
		return Observable.fromCallable {
			data.letThis {
				val listOfJson = data.map { item -> this.converter.toJson(item, this.classOfT) }
				this.sharedPreferences.putListString(this.storageKey, listOfJson)
			}
		}.subscribeOn(Schedulers.computation())
	}

	@CheckResult
	fun get(): Observable<List<T>> {
		return Observable.create<List<T>> { subscriber ->
			subscriber.onNext(this.getSync())
			subscriber.onComplete()
		}
	}

	fun clear() {
		this.sharedPreferences.edit()
				.remove(this.storageKey)
				.apply()
	}

	private fun getSync(): List<T> {
		val listOfJson = this.sharedPreferences.getListString(this.storageKey)
		return listOfJson.map { json -> this.converter.fromJson(json, this.classOfT) }
	}
}

private fun SharedPreferences.putListString(key: String, stringList: List<String>) {
	val myStringList = stringList.toTypedArray()
	this.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply()
}

private fun SharedPreferences.getListString(key: String): ArrayList<String> {
	return ArrayList(Arrays.asList(*TextUtils.split(this.getString(key, ""), "‚‗‚")))
}