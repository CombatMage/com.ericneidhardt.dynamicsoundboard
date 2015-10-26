package org.neidhardt.dynamicsoundboard.misc;

import android.Manifest;

/**
 * Project created by Eric Neidhardt on 09.09.2014.
 */
public class IntentRequest
{
	public static final int GET_AUDIO_FILE = 0;
	public static final int NOTIFICATION_OPEN_ACTIVITY = 2;

	public static final int REQUEST_PERMISSION_READ_STORAGE = 3;
	public static final int REQUEST_PERMISSION_WRITE_STORAGE = 3;
	public static final int REQUEST_PERMISSION_READ_PHONE_STATE = 5;

	public static int getRequestIdForPermission(String permission) {
		if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE))
			return REQUEST_PERMISSION_READ_STORAGE;

		if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
			return REQUEST_PERMISSION_WRITE_STORAGE;

		if (permission.equals(Manifest.permission.READ_PHONE_STATE))
			return REQUEST_PERMISSION_READ_PHONE_STATE;

		throw new IllegalArgumentException("No intent request ID found forgiven permission " + permission);
	}
}
