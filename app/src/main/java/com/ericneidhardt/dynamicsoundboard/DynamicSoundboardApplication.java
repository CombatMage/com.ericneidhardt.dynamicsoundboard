package com.ericneidhardt.dynamicsoundboard;

import android.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.util.Random;


@ReportsCrashes(
	formKey = "", // This is required for backward compatibility but not used
	mailTo = "eric@neidhardt-erkner.de"
)
public class DynamicSoundboardApplication extends Application
{
	private static Random random;
	@Override
	public void onCreate()
	{
		super.onCreate();
		ACRA.init(this);

		random = new Random();
	}

	public static int getRandomNumber()
	{
		return random.nextInt(Integer.MAX_VALUE);
	}
}
