package com.ericneidhardt.dynamicsoundboard;

import android.app.Application;
import android.content.Context;

import java.util.Random;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class DynamicSoundboardApplication extends Application
{
	private static Context applicationContext;

	private static Random random;

	@Override
	public void onCreate()
	{
		super.onCreate();

		random = new Random();
		applicationContext = this;
	}

	public static Context getContext()
	{
		return applicationContext;
	}

	public static int getRandomNumber()
	{
		return random.nextInt(Integer.MAX_VALUE);
	}
}
