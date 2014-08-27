package com.ericneidhardt.dynamicsoundboard.misc;

import android.util.Log;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class Logger {

	private static final boolean ENABLE_LOGGING = true;

	public static void e(String TAG, String msg)
	{
		if (ENABLE_LOGGING)
			Log.e(TAG, msg);
	}

	public static void d(String TAG, String msg)
	{
		if (ENABLE_LOGGING)
			Log.d(TAG, msg);
	}

}
