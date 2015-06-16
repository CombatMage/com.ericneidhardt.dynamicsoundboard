package org.neidhardt.dynamicsoundboard.soundmanagement.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.notifications.NotificationHandler;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil;

import javax.inject.Inject;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class MediaPlayerService extends Service
{
	public static final String TAG = MediaPlayerService.class.getName();

	@Inject SoundsDataUtil soundSheetsDataUtil;
	@Inject SoundsDataAccess soundSheetsDataAccess;
	@Inject SoundsDataStorage soundsDataStorage;

	private Binder binder;
	private NotificationHandler notificationHandler;

	private boolean isServiceBound = false;
	public boolean isServiceBound()
	{
		return this.isServiceBound;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		this.isServiceBound = true;
		return this.binder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		this.isServiceBound = false;
		return true; // this is necessary to ensure onRebind is called
	}

	@Override
	public void onRebind(Intent intent)
	{
		this.isServiceBound = true;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		DynamicSoundboardApplication.getApplicationComponent().inject(this);

		this.binder = new Binder(this);
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

	public void onActivityClosed()
	{
		Logger.d(TAG, "onActivityClosed");
		if (this.soundSheetsDataAccess.getCurrentlyPlayingSounds().size() == 0)
			this.stopSelf();
	}
}
