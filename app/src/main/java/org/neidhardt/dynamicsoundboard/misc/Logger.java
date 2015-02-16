package org.neidhardt.dynamicsoundboard.misc;

import android.util.Log;
import org.neidhardt.dynamicsoundboard.Configuration;

/**
 * Project created by eric.neidhardt on 27.08.2014.
 */
public class Logger
{
	public static void e(String TAG, String msg)
	{
		if (Configuration.ENABLE_LOGGING)
			Log.e(TAG, msg);
	}

	public static void d(String TAG, String msg)
	{
		if (Configuration.ENABLE_LOGGING)
			Log.d(TAG, msg);
	}

}
