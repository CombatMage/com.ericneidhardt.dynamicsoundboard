package org.neidhardt.dynamicsoundboard.notifications.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.notifications.NotificationHandler;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityClosedEvent;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityResumedEvent;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityStateChangedEventListener;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil;

import javax.inject.Inject;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class MediaPlayerService extends Service implements ActivityStateChangedEventListener
{
	public static final String TAG = MediaPlayerService.class.getName();

	@Inject SoundsDataUtil soundSheetsDataUtil;
	@Inject SoundsDataAccess soundSheetsDataAccess;
	@Inject SoundsDataStorage soundsDataStorage;

	private NotificationHandler notificationHandler;

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
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Logger.d(TAG, "onStartCommand");
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		Logger.d(TAG, "onDestroy");

		this.notificationHandler.onServiceDestroyed();
		this.soundSheetsDataUtil.writeCacheBackAndRelease();

		super.onDestroy();
	}

	@Override
	public void onEvent(ActivityClosedEvent event)
	{
		if (this.soundSheetsDataAccess.getCurrentlyPlayingSounds().size() == 0)
			this.stopSelf();
	}

	@Override
	public void onEvent(ActivityResumedEvent event)
	{
		// TODO
	}

}
