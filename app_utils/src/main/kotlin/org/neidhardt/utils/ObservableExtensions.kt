package org.neidhardt.utils

import rx.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by eric.neidhardt@sevenval.com on 17.11.2016.
 */
private val STD_DEBOUNCE_TIME = 1000.toLong()

fun Observable<Void>.stdDebounce(): Observable<Void> {
	return this.debounce(org.neidhardt.utils.STD_DEBOUNCE_TIME, java.util.concurrent.TimeUnit.MILLISECONDS)
}
