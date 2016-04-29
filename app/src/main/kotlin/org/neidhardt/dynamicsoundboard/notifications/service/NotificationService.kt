package org.neidhardt.dynamicsoundboard.notifications.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.notifications.NotificationHandler
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEventListener
import org.neidhardt.dynamicsoundboard.soundcontrol.PauseSoundOnCallListener
import org.neidhardt.dynamicsoundboard.soundcontrol.registerPauseSoundOnCallListener
import org.neidhardt.dynamicsoundboard.soundcontrol.unregisterPauseSoundOnCallListener

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
class NotificationService : Service(), ActivityStateChangedEventListener
{
	private val TAG: String = javaClass.name

	private val soundsDataUtil = SoundboardApplication.soundsDataUtil
	private val soundsDataAccess = SoundboardApplication.soundsDataAccess
	private val soundSheetsDataUtil = SoundboardApplication.soundSheetsDataUtil

	private val eventBus = EventBus.getDefault()
	private val phoneStateListener: PauseSoundOnCallListener = PauseSoundOnCallListener()

	private var notificationHandler: NotificationHandler? = null

	var isActivityVisible: Boolean = false
		private set

	override fun onBind(intent: Intent): IBinder? = null

	override fun onCreate()
	{
		super.onCreate()
		this.notificationHandler = NotificationHandler(this, this.soundsDataAccess, this.soundsDataUtil, this.soundSheetsDataUtil)

		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		Logger.d(TAG, "onStartCommand")
		this.isActivityVisible = true
		return Service.START_STICKY
	}

	override fun onDestroy()
	{
		Logger.d(TAG, "onDestroy")

		this.unregisterPauseSoundOnCallListener(this.phoneStateListener)

		this.eventBus.unregister(this)
		this.notificationHandler!!.onServiceDestroyed()
		this.soundsDataUtil.releaseAll()

		super.onDestroy()
	}

	@Subscribe(sticky = true)
	override fun onEvent(event: ActivityStateChangedEvent)
	{
		if (event.isActivityClosed)
		{
			this.isActivityVisible = false

			this.registerPauseSoundOnCallListener(this.phoneStateListener)
			if (this.soundsDataAccess.currentlyPlayingSounds.size == 0)
				this.stopSelf()
		}
		else if (event.isActivityResumed)
		{
			this.unregisterPauseSoundOnCallListener(this.phoneStateListener)

			this.isActivityVisible = true
			this.notificationHandler!!.removeNotificationsForPausedSounds()
		}
	}
}
