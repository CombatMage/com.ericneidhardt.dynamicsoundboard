package org.neidhardt.utils

import java.util.*

/**
 * Created by eric.neidhardt@gmail.com on 20.12.2016.
 */

fun <T> Collection<T>.getCopyList(): MutableCollection<T> {
	val copyOf = ArrayList<T>(this.size)
	copyOf.addAll(this)
	return copyOf
}
