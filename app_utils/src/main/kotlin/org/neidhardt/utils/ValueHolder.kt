@file:Suppress("unused")

package org.neidhardt.utils

import android.support.annotation.CheckResult
import io.reactivex.Observable

/**
 * Created by eric.neidhardt on 24.11.2016.
 */
@Suppress("unused")
class ValueHolder<T>(initialValue: T) {

	var onValueChangedListener: ((T) -> Unit)? = null

	var value: T = initialValue
		set(value) {
			val hasChanged = field != value
			field = value
			if (hasChanged)
				this.onValueChangedListener?.invoke(value)
		}

	fun changes(): Observable<T> {
		return Observable.create({ subscriber ->
			this.onValueChangedListener = { newValue ->
				subscriber.onNext(newValue)
			}
		})
	}
}

@Suppress("unused")
object RxValueHolder {

	@CheckResult
	fun <T>changes(valueHolder: ValueHolder<T>): Observable<T> {
		return Observable.create({ subscriber ->

			subscriber.onNext(valueHolder.value)

			valueHolder.onValueChangedListener = { newValue ->
				subscriber.onNext(newValue)
			}
		})
	}
}