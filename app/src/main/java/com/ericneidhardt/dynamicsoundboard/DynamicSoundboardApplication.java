package com.ericneidhardt.dynamicsoundboard;

import android.app.Application;

import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.storage.Storage;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class DynamicSoundboardApplication extends Application
{

	private DaoSession daoSession;

	@Override
	public void onCreate()
	{
		super.onCreate();
		this.daoSession = Storage.setupDatabase(this);
	}

	public DaoSession getDaoSession()
	{
		return this.daoSession;
	}
}
