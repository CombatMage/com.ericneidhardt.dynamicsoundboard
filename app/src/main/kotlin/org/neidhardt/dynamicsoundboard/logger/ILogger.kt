package org.neidhardt.dynamicsoundboard.logger

/**
 * Created by neid_ei (eric.neidhardt@dlr.de)
 * on 27.06.2018.
 */
interface ILogger {

	fun e(tag: String, msg: String?)

	fun d(tag: String, msg: String?)

	fun exception(error: Throwable)
}