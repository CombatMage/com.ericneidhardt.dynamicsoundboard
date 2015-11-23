package org.neidhardt.util.enhanced_handler

/**
 * File created by eric.neidhardt on 23.11.2015.
 */
interface KillableRunnable : Runnable
{
	@Volatile
	var isKilled: Boolean
}