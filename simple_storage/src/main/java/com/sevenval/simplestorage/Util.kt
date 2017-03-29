package com.sevenval.simplestorage

/**
 * Created by eric.neidhardt@gmail.com on 12.12.2016.
 */

/**
 * Calls the specified function [block] with `this` value as its argument and returns `this`
 */
inline fun <T, R> T.letThis(block: (T) -> R): T {
	block(this)
	return this
}