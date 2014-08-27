package com.ericneidhardt.dynamicsoundboard;

import android.app.Application;
import android.content.Context;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class DynamicSoundboardApplication extends Application
{
	private static Context applicationContext;

	@Override
	public void onCreate()
	{
		super.onCreate();
		this.applicationContext = this;
	}

	public static Context getContext()
	{
		return applicationContext;
	}
}
