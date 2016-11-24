package org.neidhardt.utils

import rx.Observable

/**
 * Created by eric.neidhardt on 24.11.2016.
 */
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

@Suppress("unused") // for convenience reason
object RxValueHolder {
	fun <T>changes(value: ValueHolder<T>): Observable<T> {
		return Observable.create({ subscriber ->
			value.onValueChangedListener = { newValue ->
				subscriber.onNext(newValue)
			}
		})
	}
}