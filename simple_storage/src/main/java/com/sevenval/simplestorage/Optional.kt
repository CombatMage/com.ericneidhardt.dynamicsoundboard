package com.sevenval.simplestorage

/**
 * Created by eric.neidhardt@gmail.com on 20.04.2017.
 */
class Optional<T> {

	var item: T? = null

	val isEmpty: Boolean get() = item == null

	companion object {

		fun <T>of(item: T): Optional<T> {
			return Optional<T>().apply {
				this.item = item
			}
		}

		fun <T>empty(): Optional<T> {
			return Optional<T>()
		}
	}
}