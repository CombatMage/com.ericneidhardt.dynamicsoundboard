package org.neidhardt.androidutils.recyclerview_utils.adapter

/**
 * File created by eric.neidhardt on 10.06.2015.
 */
interface ListAdapter<in T> {
	fun notifyItemChanged(data: T)
}
