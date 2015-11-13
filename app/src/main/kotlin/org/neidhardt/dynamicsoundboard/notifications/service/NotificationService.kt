package org.neidhardt.dynamicsoundboard.notifications.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.notifications.NotificationHandler
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEventListener

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class NotificationService : Service(), ActivityStateChangedEventListener
{
	private val TAG: String = javaClass.name

	private val soundsDataUtil = DynamicSoundboardApplication.getSoundsDataUtil()
	private val soundsDataAccess = DynamicSoundboardApplication.getSoundsDataAccess()
	private val soundSheetsDataUtil = DynamicSoundboardApplication.getSoundSheetsDataUtil()

	private val eventBus = EventBus.getDefault()

	private var notificationHandler: NotificationHandler? = null

    var isActivityVisible: Boolean = false
		private set

	override fun onBind(intent: Intent): IBinder? = null

	override fun onCreate()
	{
		super.onCreate()
		this.notificationHandler = NotificationHandler(this, this.soundsDataAccess, this.soundsDataUtil, this.soundSheetsDataUtil)

		if (!this.eventBus.isRegistered(this))
			this.eventBus.registerSticky(this)
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

		this.eventBus.unregister(this)
		this.notificationHandler!!.onServiceDestroyed()
		this.soundsDataUtil.releaseAll()

		super.onDestroy()
	}

	override fun onEvent(event: ActivityStateChangedEvent)
	{
		if (event.isActivityClosed)
		{
			this.isActivityVisible = false
			if (this.soundsDataAccess.currentlyPlayingSounds.size == 0)
				this.stopSelf()
		}
		else if (event.isActivityResumed)
		{
			this.isActivityVisible = true
			this.notificationHandler!!.removeNotificationsForPausedSounds()
		}
	}
}
