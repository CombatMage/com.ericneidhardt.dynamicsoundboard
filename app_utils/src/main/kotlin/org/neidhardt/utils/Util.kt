package org.neidhardt.utils

import android.os.Build
import de.greenrobot.common.hash.Murmur3A
import org.greenrobot.eventbus.EventBus

/**
 * Project created by Eric Neidhardt on 30.08.2014.
 */
val String.longHash: Long
	get()
	{
		val generator = Murmur3A()
		generator.update(this.toByteArray())
		return generator.value
	}

/**
 * Calls the specified function [block] with `this` value as its argument and returns `this`
 */
inline fun <T, R> T.letThis(block: (T) -> R): T {
	block(this)
	return this
}

inline fun <T1,T2,R>T1.letWithParam(p: T2?, block: (T1, T2) -> R?): R? {
	return if (p != null)
		block(this, p)
	else
		null
}
