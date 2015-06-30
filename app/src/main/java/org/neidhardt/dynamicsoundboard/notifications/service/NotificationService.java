package org.neidhardt.dynamicsoundboard.notifications.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.notifications.NotificationHandler;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEvent;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEventListener;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil;

import javax.inject.Inject;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class NotificationService extends Service implements ActivityStateChangedEventListener
{
	public static final String TAG = NotificationService.class.getName();

	@Inject SoundsDataUtil soundSheetsDataUtil;
	@Inject SoundsDataAccess soundSheetsDataAccess;
	@Inject SoundsDataStorage soundsDataStorage;

	private NotificationHandler notificationHandler;
	private EventBus eventBus;
	private boolean isActivityVisible;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		DynamicSoundboardApplication.getApplicationComponent().inject(this);

		this.notificationHandler = new NotificationHandler(this, this.soundSheetsDataAccess);

		this.eventBus = EventBus.getDefault();
		if (!this.eventBus.isRegistered(this))
			this.eventBus.registerSticky(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Logger.d(TAG, "onStartCommand");
		this.isActivityVisible = true;
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		Logger.d(TAG, "onDestroy");

		this.eventBus.unregister(this);
		this.notificationHandler.onServiceDestroyed();
		this.soundSheetsDataUtil.release();

		super.onDestroy();
	}

	@Override
	public void onEvent(ActivityStateChangedEvent event)
	{
		if (event.isActivityClosed())
		{
			this.isActivityVisible = false;
			if (this.soundSheetsDataAccess.getCurrentlyPlayingSounds().size() == 0)
				this.stopSelf();
		}
		else if (event.isActivityResumed())
		{
			this.isActivityVisible = true;
			this.notificationHandler.removeNotificationsForPausedSounds();
		}
	}

	public boolean isActivityVisible()
	{
		return isActivityVisible;
	}
}
