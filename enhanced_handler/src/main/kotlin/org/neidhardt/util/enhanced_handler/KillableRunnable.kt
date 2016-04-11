package org.neidhardt.util.enhanced_handler

/**
 * File created by eric.neidhardt on 23.11.2015.
 */
abstract class KillableRunnable : Runnable
{
	@Volatile
	var isKilled: Boolean = false
}