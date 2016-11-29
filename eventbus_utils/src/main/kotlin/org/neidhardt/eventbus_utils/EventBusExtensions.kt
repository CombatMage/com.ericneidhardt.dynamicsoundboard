package org.neidhardt.eventbus_utils

import org.greenrobot.eventbus.EventBus

/**
 * Project created by Eric Neidhardt on 30.08.2014.
 */
fun EventBus.registerIfRequired(any: Any) {
	if (!this.isRegistered(any))
		this.register(any)
}
