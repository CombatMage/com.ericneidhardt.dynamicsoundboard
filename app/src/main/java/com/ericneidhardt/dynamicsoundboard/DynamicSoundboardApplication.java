package com.ericneidhardt.dynamicsoundboard;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.util.Random;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
@ReportsCrashes(
	formKey = "", // This is required for backward compatibility but not used
	mailTo = "reports@yourdomain.com"
)
public class DynamicSoundboardApplication extends Application
{
	private static Context applicationContext;

	private static Random random;

	@Override
	public void onCreate()
	{
		super.onCreate();
		ACRA.init(this);

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
