package org.neidhardt.dynamicsoundboard.soundmanagement.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.Util;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.notifications.NotificationHandler;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.*;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.tasks.LoadPlaylistTask;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.tasks.LoadSoundsTask;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.tasks.UpdateSoundsTask;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil;
import roboguice.util.SafeAsyncTask;

import javax.inject.Inject;
import java.util.*;

/**
 * File created by eric.neidhardt on 15.06.2015.
 */
public class MediaPlayerService extends Service
{
	public static final String TAG = MediaPlayerService.class.getName();

	@Inject SoundsDataUtil soundSheetsDataUtil;
	@Inject SoundsDataAccess soundSheetsDataAccess;
	@Inject SoundsDataStorage soundsDataStorage;

	private EventBus eventBus;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		DynamicSoundboardApplication.getSoundsDataComponent().inject(this);

		this.eventBus = EventBus.getDefault();
	}


}
