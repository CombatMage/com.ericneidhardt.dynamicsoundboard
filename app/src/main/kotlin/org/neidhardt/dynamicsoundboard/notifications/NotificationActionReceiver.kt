package org.neidhardt.dynamicsoundboard.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * @author eric.neidhardt on 15.06.2016.
 */
class NotificationActionReceiver(
		private val onActionReceived: (action: String, playerId: String, notificationId: Int) -> Unit
) : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		val action = intent.action ?: return
		val playerId = intent.getStringExtra(NotificationExtra.PLAYER_ID) ?: return
		val notificationId = intent.getIntExtra(NotificationExtra.NOTIFICATION_ID, 0)

		this.onActionReceived(action, playerId, notificationId)
	}
}