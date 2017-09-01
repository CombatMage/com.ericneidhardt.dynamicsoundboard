package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.viewmodel

import android.databinding.BaseObservable
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.navigationdrawerfragment.events.OpenSoundLayoutsRequestedEvent

/**
* Created by Eric.Neidhardt@GMail.com on 17.06.2016.
*/
class NavigationDrawerHeaderVM(
		private val eventBus: EventBus,
		title: String?
) : BaseObservable() {

	var title: String? = title
		set(value) {
			field = value
			this.notifyChange()
		}

	var isSoundLayoutOpen = false
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onChangeLayoutClicked() {
		this.eventBus.post(OpenSoundLayoutsRequestedEvent(!isSoundLayoutOpen))
		this.isSoundLayoutOpen = !this.isSoundLayoutOpen
	}
}