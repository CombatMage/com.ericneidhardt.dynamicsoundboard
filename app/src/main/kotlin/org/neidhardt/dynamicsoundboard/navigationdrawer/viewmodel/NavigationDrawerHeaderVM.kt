package org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel

import android.databinding.BaseObservable
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.OpenSoundLayoutsRequestedEvent

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

	var isOpenSoundLayoutRequested = true
		set(value) {
			field = value
			this.notifyChange()
		}

	fun onChangeLayoutClicked() {
		this.eventBus.post(OpenSoundLayoutsRequestedEvent(isOpenSoundLayoutRequested))
		this.isOpenSoundLayoutRequested = !this.isOpenSoundLayoutRequested
	}
}