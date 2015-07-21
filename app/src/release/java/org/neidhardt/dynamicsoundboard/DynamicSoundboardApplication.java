package org.neidhardt.dynamicsoundboard;

import android.app.Application;
import android.content.Context;

import java.util.Random;


public class DynamicSoundboardApplication extends Application
{
	private static Random random;
	private static Context applicationContext;

	private static Storage storage;

	@Override
	public void onCreate()
	{
		super.onCreate();
		random = new Random();

		applicationContext = this.getApplicationContext();
		storage = new Storage();
	}

	public static Context getSoundboardContext()
	{
		return applicationContext;
	}

	public static int getRandomNumber()
	{
		return random.nextInt(Integer.MAX_VALUE);
	}

	public static Storage getStorage()
	{
		return storage;
	}

}
